package com.lukeneedham.pushtotalkai.assistant

import android.media.AudioManager
import android.media.ToneGenerator
import com.lukeneedham.common.logger.Logger

/** Short beeps in the earphones: the user cannot look at a screen in a pocket. */
class AudioCues {

    fun listening() = beep(ToneGenerator.TONE_PROP_BEEP)

    fun failed() = beep(ToneGenerator.TONE_PROP_NACK)

    private fun beep(tone: Int) {
        try {
            val generator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            generator.startTone(tone, 150)
            // The generator has to live until the tone has played
            Thread {
                Thread.sleep(400)
                generator.release()
            }.start()
        } catch (e: RuntimeException) {
            Logger.w("Could not play a cue", e)
        }
    }
}
