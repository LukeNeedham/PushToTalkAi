package com.lukeneedham.pushtotalkai.assistant

/** What the centre button of the headset did. */
sealed interface HeadsetGesture {
    /** The button has been held long enough: the user starts talking. */
    data object HoldStart : HeadsetGesture

    /** The held button was released: the user is done talking. */
    data object HoldEnd : HeadsetGesture

    /** Short presses, counted over the multi-click window: 1, 2, 3... */
    data class Clicks(val count: Int) : HeadsetGesture
}
