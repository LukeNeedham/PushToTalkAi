package com.lukeneedham.pushtotalkai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukeneedham.pushtotalkai.assistant.KeyEventLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EventLogViewModel(private val keyEventLog: KeyEventLog) : ViewModel() {

    val uiState: StateFlow<EventLogUiState> = keyEventLog.entries
        .map { entries -> EventLogUiState(lines = entries.map { "${it.time}  ${it.key}  ${it.action}  ${it.detail}" }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventLogUiState(lines = emptyList()))

    fun onClear() = keyEventLog.clear()
}
