package com.lukeneedham.pushtotalkai.assistant

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Plays the speech of Gemini (16-bit mono PCM at 24 kHz) through whatever the audio is routed to: the earphones. */
class SpeechPlayer {

    private sealed interface Item {
        class Chunk(val pcm: ByteArray) : Item
        class Marker(val done: CompletableDeferred<Unit>) : Item
    }

    private val sampleRate = 24_000
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queue = Channel<Item>(Channel.UNLIMITED)
    private var track: AudioTrack? = null
    private var worker: Job? = null

    fun enqueue(pcm: ByteArray) {
        startWorker()
        queue.trySend(Item.Chunk(pcm))
    }

    /** Suspends until everything queued so far has been played. */
    suspend fun awaitDrained() {
        startWorker()
        val done = CompletableDeferred<Unit>()
        queue.trySend(Item.Marker(done))
        done.await()
        // The last samples are still in the buffer of the track
        delay(400)
    }

    /** Drops what has not been played yet, because the user spoke over Gemini. */
    fun flush() {
        while (true) {
            val item = queue.tryReceive().getOrNull() ?: break
            if (item is Item.Marker) item.done.complete(Unit)
        }
        track?.let {
            it.pause()
            it.flush()
            it.play()
        }
    }

    /** Frees the audio output. The next [enqueue] opens it again. */
    fun shutdown() {
        flush()
        worker?.cancel()
        worker = null
    }

    private fun startWorker() {
        if (worker?.isActive == true) return
        worker = scope.launch {
            val audioTrack = createTrack()
            track = audioTrack
            audioTrack.play()
            try {
                for (item in queue) {
                    when (item) {
                        is Item.Chunk -> audioTrack.write(item.pcm, 0, item.pcm.size)
                        is Item.Marker -> item.done.complete(Unit)
                    }
                }
            } finally {
                track = null
                audioTrack.release()
            }
        }
    }

    private fun createTrack(): AudioTrack {
        val minBuffer = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(minBuffer, sampleRate))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }
}
