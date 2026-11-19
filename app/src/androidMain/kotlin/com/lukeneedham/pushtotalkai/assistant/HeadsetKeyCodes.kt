package com.lukeneedham.pushtotalkai.assistant

import android.view.KeyEvent

/** Tells the keys of the centre button of a headset from the others. The volume keys are never touched. */
object HeadsetKeyCodes {
    private val centreButton = setOf(KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    fun isCentreButton(event: KeyEvent): Boolean = event.keyCode in centreButton
}
