package com.lukeneedham.pushtotalkai.assistant

/** One key event the accessibility service saw, as text for the event log page. */
data class KeyLogEntry(
    val time: String,
    val key: String,
    val action: String,
    val detail: String,
)
