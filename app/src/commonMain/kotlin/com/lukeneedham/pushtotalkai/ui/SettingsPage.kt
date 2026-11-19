package com.lukeneedham.pushtotalkai.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/** The page: connects to the ViewModel and the navigation, and delegates all UI. No preview. */
@Composable
fun SettingsPage(
    onOpenLogs: () -> Unit,
    onOpenDebug: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshSetup() }
    SettingsPageContent(
        state = state,
        onApiKeyChange = viewModel::onApiKeyChange,
        onModelChange = viewModel::onModelChange,
        onToggleAssistant = viewModel::onToggleAssistant,
        onRequestPermissions = onRequestPermissions,
        onOpenAccessibility = viewModel::onOpenAccessibility,
        onOpenNotificationAccess = viewModel::onOpenNotificationAccess,
        onOpenBattery = viewModel::onOpenBattery,
        onOpenAssistantSettings = viewModel::onOpenAssistantSettings,
        onOpenLogs = onOpenLogs,
        onOpenDebug = onOpenDebug,
    )
}
