package com.lukeneedham.pushtotalkai.assistant

/** What the user sets: the Google AI Studio key, and the Gemini Live model that answers. */
data class AssistantSettings(
    val apiKey: String = "",
    val model: String = defaultModel,
) {
    companion object {
        /** Preview models come and go: the user can type another one on the settings page. */
        const val defaultModel = "gemini-3.1-flash-live-preview"
    }
}
