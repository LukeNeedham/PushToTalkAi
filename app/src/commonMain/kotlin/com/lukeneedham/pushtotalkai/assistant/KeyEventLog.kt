package com.lukeneedham.pushtotalkai.assistant

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The newest key events, newest first. It answers the first question of the project: does the headset button arrive, with the screen off? */
class KeyEventLog {
    private val _entries = MutableStateFlow<List<KeyLogEntry>>(emptyList())
    val entries: StateFlow<List<KeyLogEntry>> = _entries.asStateFlow()

    fun add(entry: KeyLogEntry) {
        _entries.update { (listOf(entry) + it).take(maxEntries) }
    }

    fun clear() {
        _entries.value = emptyList()
    }

    private val maxEntries = 300
}
