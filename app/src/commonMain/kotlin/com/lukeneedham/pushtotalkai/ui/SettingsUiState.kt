package com.lukeneedham.pushtotalkai.ui

import com.lukeneedham.pushtotalkai.assistant.AssistantState
import com.lukeneedham.pushtotalkai.assistant.SetupStatus

data class SettingsUiState(
    /** Whether the debug pages are on (debug builds): the page shows the way to them only then. */
    val isDebugEnabled: Boolean,
    val apiKey: String,
    val model: String,
    val setup: SetupStatus,
    val assistant: AssistantState,
)
