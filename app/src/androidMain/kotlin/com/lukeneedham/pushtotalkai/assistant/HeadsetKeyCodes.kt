package com.lukeneedham.pushtotalkai.assistant

import android.view.KeyEvent

/** Tells the keys of the centre button of a headset from the others. The volume keys are never touched. */
object HeadsetKeyCodes {
    private val centreButton = setOf(KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    /**
     * Some headsets (the USB-C EarPods, perhaps) send the button as a key that Android has no name for, `KEYCODE_UNKNOWN`:
     * nothing else uses that code, so it is taken as the centre button too. The log shows the scan code of each key.
     */
    fun isCentreButton(event: KeyEvent): Boolean = event.keyCode in centreButton || event.keyCode == KeyEvent.KEYCODE_UNKNOWN
}
