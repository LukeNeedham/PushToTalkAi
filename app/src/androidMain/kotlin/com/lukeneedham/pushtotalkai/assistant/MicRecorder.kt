package com.lukeneedham.pushtotalkai.assistant

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Records the microphone while the flow is collected: 16-bit mono PCM at 16 kHz, in chunks of about 100 ms. */
class MicRecorder {

    private val sampleRate = 16_000
    private val chunkBytes = 3_200

    @SuppressLint("MissingPermission") // the permission is checked by the settings page before the service starts
    fun record(): Flow<ByteArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer, chunkBytes * 2),
        )
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "The microphone is not available" }
            recorder.startRecording()
            val buffer = ByteArray(chunkBytes)
            while (true) {
                currentCoroutineContext().ensureActive()
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) emit(buffer.copyOf(read)) else if (read < 0) error("The microphone failed (code $read)")
            }
        } finally {
            recorder.release()
        }
    }.flowOn(Dispatchers.IO)
}
