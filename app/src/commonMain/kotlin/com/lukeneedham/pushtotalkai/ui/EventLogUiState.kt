package com.lukeneedham.pushtotalkai.ui

data class EventLogUiState(
    /** One line per key event, newest first. */
    val lines: List<String>,
)
