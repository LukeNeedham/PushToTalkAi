package com.lukeneedham.pushtotalkai.assistant

import com.lukeneedham.common.logger.Logger
import com.lukeneedham.pushtotalkai.gemini.GeminiLiveClient
import com.lukeneedham.pushtotalkai.gemini.GeminiLiveSession
import com.lukeneedham.pushtotalkai.gemini.LiveEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Push to talk: what happens when the headset button is held and released, and when it is clicked.
 * Everything runs on the one thread of [AssistantScope], so none of the fields needs a lock.
 */
class AssistantController(
    private val settingsStore: SettingsStore,
    private val liveClient: GeminiLiveClient,
    private val recorder: MicRecorder,
    private val player: SpeechPlayer,
    private val focus: AudioFocusHolder,
    private val cues: AudioCues,
    private val tools: AssistantToolRunner,
    private val music: MusicControls,
    private val status: AssistantStatusStore,
    private val scope: AssistantScope,
) {
    private val responseTimeoutMillis = 20_000L
    private val idleCloseMillis = 60_000L

    private var session: GeminiLiveSession? = null
    private var isHolding = false
    private var recordJob: Job? = null
    private var sendJob: Job? = null
    private var eventsJob: Job? = null
    private var watchdogJob: Job? = null
    private var idleJob: Job? = null
    private var sendChannel: Channel<ByteArray>? = null

    fun onHoldStart() {
        scope.launch { startHold() }
    }

    fun onHoldEnd() {
        scope.launch { endHold() }
    }

    fun onClicks(count: Int) {
        scope.launch {
            Logger.i("Assistant: $count click(s)")
            val command = when (count) {
                1 -> MusicCommand.PlayPause
                2 -> MusicCommand.Next
                else -> MusicCommand.Previous
            }
            music.sendCommand(command)
        }
    }

    /** The service is stopping: let go of the microphone, the speaker, the audio focus and the connection. */
    fun shutdown() {
        scope.launch {
            isHolding = false
            cancelWork()
            player.shutdown()
            focus.release()
            closeSession()
        }
    }

    private suspend fun startHold() {
        if (isHolding || status.state.value is AssistantState.Stopped) {
            Logger.w("Assistant: hold ignored (holding=$isHolding, state=${status.state.value})")
            return
        }
        Logger.i("Assistant: hold started")
        val settings = settingsStore.settings.first()
        if (settings.apiKey.isBlank()) {
            fail("No API key: paste it into the app")
            return
        }
        isHolding = true
        idleJob?.cancel()
        watchdogJob?.cancel()
        player.flush()
        focus.acquire()
        cues.listening()
        status.set(AssistantState.Listening)

        // The microphone starts at once and its audio waits in the channel until the connection is ready: no word is lost
        val chunks = Channel<ByteArray>(Channel.UNLIMITED)
        sendChannel = chunks
        recordJob = scope.launch {
            try {
                recorder.record().collect { chunks.send(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e("Recording failed", e)
                fail(e.message ?: "The microphone failed")
            }
        }
        sendJob = scope.launch { stream(settings, chunks) }
    }

    private suspend fun endHold() {
        if (!isHolding) return
        Logger.i("Assistant: hold ended")
        isHolding = false
        recordJob?.cancelAndJoin()
        recordJob = null
        // Closing the channel ends the sending, once everything recorded has been sent
        sendChannel?.close()
    }

    private suspend fun stream(settings: AssistantSettings, chunks: Channel<ByteArray>) {
        try {
            val live = openSession(settings)
            live.startActivity()
            for (chunk in chunks) live.sendAudio(chunk)
            live.endActivity()
            if (status.state.value is AssistantState.Listening) status.set(AssistantState.Responding)
            armWatchdog()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("Talking to Gemini failed", e)
            fail(e.message ?: "Could not talk to Gemini")
        }
    }

    private suspend fun openSession(settings: AssistantSettings): GeminiLiveSession {
        session?.takeIf { it.isOpen }?.let { return it }
        Logger.i("Assistant: connecting to Gemini, model ${settings.model}")
        val live = liveClient.connect(settings, tools.declarations, scope)
        Logger.i("Assistant: connected to Gemini")
        session = live
        eventsJob = scope.launch { live.events.collect { handle(live, it) } }
        return live
    }

    private suspend fun handle(live: GeminiLiveSession, event: LiveEvent) {
        if (event !is LiveEvent.Audio) Logger.d("Assistant: Gemini sent $event")
        when (event) {
            is LiveEvent.Audio -> {
                player.enqueue(event.pcm)
                armWatchdog()
            }
            LiveEvent.Interrupted -> player.flush()
            LiveEvent.TurnComplete -> {
                watchdogJob?.cancel()
                player.awaitDrained()
                finishTurn()
            }
            is LiveEvent.ToolCalls -> {
                val results = event.calls.map { call ->
                    val result = tools.run(call)
                    Logger.i("Assistant: ${call.name}(${call.args}) -> $result")
                    call to result
                }
                live.sendToolResponses(results)
                armWatchdog()
            }
            is LiveEvent.Closed -> {
                if (session === live) session = null
                if (status.state.value is AssistantState.Listening || status.state.value is AssistantState.Responding) {
                    fail(event.reason ?: "Gemini closed the connection")
                }
            }
            LiveEvent.SetupComplete -> Unit
        }
    }

    /** Gemini finished talking: give the audio back to Spotify, and close the connection if nobody talks for a while. */
    private fun finishTurn() {
        if (isHolding) return
        focus.release()
        if (status.state.value !is AssistantState.Stopped) status.set(AssistantState.Idle)
        idleJob?.cancel()
        idleJob = scope.launch {
            delay(idleCloseMillis)
            closeSession()
            player.shutdown()
        }
    }

    private fun armWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(responseTimeoutMillis)
            fail("Gemini did not answer")
        }
    }

    private fun fail(message: String) {
        Logger.e("Assistant: failed: $message")
        cancelWork()
        isHolding = false
        player.flush()
        focus.release()
        cues.failed()
        status.set(AssistantState.Error(message))
    }

    private fun cancelWork() {
        recordJob?.cancel()
        sendJob?.cancel()
        watchdogJob?.cancel()
        idleJob?.cancel()
        sendChannel?.close()
        sendChannel = null
    }

    private suspend fun closeSession() {
        eventsJob?.cancel()
        eventsJob = null
        session?.close()
        session = null
    }
}
