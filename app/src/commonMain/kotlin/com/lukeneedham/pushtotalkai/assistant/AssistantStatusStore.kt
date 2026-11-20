package com.lukeneedham.pushtotalkai.assistant

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The one [AssistantState] of the app: the services write it, the settings page reads it. */
class AssistantStatusStore {
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Stopped)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    fun set(state: AssistantState) {
        _state.value = state
    }
}
