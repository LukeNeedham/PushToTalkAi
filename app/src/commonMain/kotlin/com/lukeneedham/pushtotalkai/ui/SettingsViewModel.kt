package com.lukeneedham.pushtotalkai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukeneedham.pushtotalkai.assistant.AssistantSettings
import com.lukeneedham.pushtotalkai.assistant.AssistantState
import com.lukeneedham.pushtotalkai.assistant.AssistantStatusStore
import com.lukeneedham.pushtotalkai.assistant.SettingsStore
import com.lukeneedham.pushtotalkai.assistant.SystemSetup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    isDebugEnabled: Boolean,
    private val settingsStore: SettingsStore,
    private val systemSetup: SystemSetup,
    statusStore: AssistantStatusStore,
) : ViewModel() {

    // What the text fields show: kept here, so typing never waits for the disk
    private val draft = MutableStateFlow(AssistantSettings())
    private val setup = MutableStateFlow(systemSetup.readStatus())

    val uiState: StateFlow<SettingsUiState> = combine(draft, setup, statusStore.state) { settings, setupStatus, assistant ->
        SettingsUiState(
            isDebugEnabled = isDebugEnabled,
            apiKey = settings.apiKey,
            model = settings.model,
            setup = setupStatus,
            assistant = assistant,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            isDebugEnabled = isDebugEnabled,
            apiKey = draft.value.apiKey,
            model = draft.value.model,
            setup = setup.value,
            assistant = statusStore.state.value,
        ),
    )

    init {
        viewModelScope.launch { draft.value = settingsStore.settings.first() }
    }

    fun onApiKeyChange(apiKey: String) = update(draft.value.copy(apiKey = apiKey.trim()))

    fun onModelChange(model: String) = update(draft.value.copy(model = model))

    /** Called when the page comes back to the front: the user may have switched something on in Android. */
    fun refreshSetup() {
        setup.value = systemSetup.readStatus()
    }

    fun onToggleAssistant() {
        if (uiState.value.assistant is AssistantState.Stopped) systemSetup.startAssistant() else systemSetup.stopAssistant()
    }

    fun onOpenAccessibility() = systemSetup.openAccessibilitySettings()

    fun onOpenNotificationAccess() = systemSetup.openNotificationAccessSettings()

    fun onOpenBattery() = systemSetup.openBatterySettings()

    private fun update(settings: AssistantSettings) {
        draft.value = settings
        viewModelScope.launch { settingsStore.save(settings) }
    }
}
