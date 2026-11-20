package com.lukeneedham.pushtotalkai.assistant

import kotlinx.serialization.json.JsonObject

/** A function Gemini asked the app to run. */
data class ToolCall(
    val id: String,
    val name: String,
    val args: JsonObject,
)
