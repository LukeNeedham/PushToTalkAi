package com.lukeneedham.pushtotalkai.assistant

/** What the assistant is doing, shown on the settings page. */
sealed interface AssistantState {
    /** The foreground service is off, so the headset button keeps its normal job. */
    data object Stopped : AssistantState

    /** Ready: waiting for the headset button to be held. */
    data object Idle : AssistantState

    /** The button is held and the microphone is open. */
    data object Listening : AssistantState

    /** The button was released: Gemini is thinking or talking. */
    data object Responding : AssistantState

    data class Error(val message: String) : AssistantState
}
