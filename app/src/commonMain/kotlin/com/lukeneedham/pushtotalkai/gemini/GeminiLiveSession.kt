package com.lukeneedham.pushtotalkai.gemini

import com.lukeneedham.common.logger.Logger
import com.lukeneedham.pushtotalkai.assistant.ToolCall
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * One open WebSocket to Gemini Live. The activity (speech) of the user is marked by hand with [startActivity] and [endActivity],
 * which is push to talk: the server never guesses when the user stopped.
 */
@OptIn(ExperimentalEncodingApi::class)
class GeminiLiveSession internal constructor(
    private val socket: DefaultClientWebSocketSession,
    scope: CoroutineScope,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true; explicitNulls = false }
    private val channel = Channel<LiveEvent>(Channel.UNLIMITED)
    private val setupDone = CompletableDeferred<Unit>()
    private val reader: Job = scope.launch { read() }

    val events: Flow<LiveEvent> = channel.receiveAsFlow()

    val isOpen: Boolean get() = reader.isActive

    internal suspend fun send(message: JsonObject) {
        socket.send(Frame.Text(message.toString()))
    }

    /** Suspends until the server accepted the setup, and throws if it did not. */
    internal suspend fun awaitSetup() = setupDone.await()

    suspend fun startActivity() = send(buildJsonObject { putJsonObject("realtimeInput") { putJsonObject("activityStart") {} } })

    suspend fun endActivity() = send(buildJsonObject { putJsonObject("realtimeInput") { putJsonObject("activityEnd") {} } })

    /** [pcm] is 16-bit mono PCM at 16 kHz. */
    suspend fun sendAudio(pcm: ByteArray) = send(
        buildJsonObject {
            putJsonObject("realtimeInput") {
                putJsonObject("audio") {
                    put("data", Base64.Default.encode(pcm))
                    put("mimeType", "audio/pcm;rate=16000")
                }
            }
        },
    )

    suspend fun sendToolResponses(results: List<Pair<ToolCall, JsonObject>>) = send(
        buildJsonObject {
            putJsonObject("toolResponse") {
                putJsonArray("functionResponses") {
                    results.forEach { (call, response) ->
                        addJsonObject {
                            put("id", call.id)
                            put("name", call.name)
                            put("response", response)
                        }
                    }
                }
            }
        },
    )

    suspend fun close() {
        try {
            socket.close(CloseReason(CloseReason.Codes.NORMAL, "done"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w("Closing the Gemini session failed", e)
        }
    }

    private suspend fun read() {
        try {
            for (frame in socket.incoming) {
                val text = when (frame) {
                    is Frame.Text -> frame.readText()
                    is Frame.Binary -> frame.readBytes().decodeToString()
                    else -> continue
                }
                val dto = try {
                    json.decodeFromString<LiveServerMessageDto>(text)
                } catch (e: SerializationException) {
                    Logger.w("Unreadable Gemini message", e)
                    continue
                }
                for (event in dto.toEvents()) {
                    if (event is LiveEvent.SetupComplete) setupDone.complete(Unit) else channel.trySend(event)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("Reading from Gemini failed", e)
        } finally {
            withContext(NonCancellable) {
                val reason = withTimeoutOrNull(1_000) { socket.closeReason.await()?.message }
                setupDone.completeExceptionally(IllegalStateException(reason ?: "Gemini closed the connection"))
                channel.trySend(LiveEvent.Closed(reason))
                channel.close()
            }
        }
    }
}
