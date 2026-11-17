package com.lukeneedham.pushtotalkai.assistant

import android.view.KeyEvent

/** The keys that the centre button of a headset sends. The volume keys are never touched. */
object HeadsetKeyCodes {
    val centreButton = setOf(KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
}
