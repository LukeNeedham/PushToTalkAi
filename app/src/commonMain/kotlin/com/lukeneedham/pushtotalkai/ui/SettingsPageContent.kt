package com.lukeneedham.pushtotalkai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukeneedham.common.ui.DebugButton
import com.lukeneedham.pushtotalkai.assistant.AssistantState
import com.lukeneedham.pushtotalkai.assistant.SetupStatus
import com.lukeneedham.pushtotalkai.generated.resources.Res
import com.lukeneedham.pushtotalkai.generated.resources.settings_accessibility_description
import com.lukeneedham.pushtotalkai.generated.resources.settings_accessibility_title
import com.lukeneedham.pushtotalkai.generated.resources.settings_api_key
import com.lukeneedham.pushtotalkai.generated.resources.settings_battery_description
import com.lukeneedham.pushtotalkai.generated.resources.settings_battery_title
import com.lukeneedham.pushtotalkai.generated.resources.settings_logs
import com.lukeneedham.pushtotalkai.generated.resources.settings_microphone_description
import com.lukeneedham.pushtotalkai.generated.resources.settings_microphone_title
import com.lukeneedham.pushtotalkai.generated.resources.settings_model
import com.lukeneedham.pushtotalkai.generated.resources.settings_notification_access_description
import com.lukeneedham.pushtotalkai.generated.resources.settings_notification_access_title
import com.lukeneedham.pushtotalkai.generated.resources.settings_open
import com.lukeneedham.pushtotalkai.generated.resources.settings_setup_done
import com.lukeneedham.pushtotalkai.generated.resources.settings_setup_needed
import com.lukeneedham.pushtotalkai.generated.resources.settings_start
import com.lukeneedham.pushtotalkai.generated.resources.settings_state_error
import com.lukeneedham.pushtotalkai.generated.resources.settings_state_idle
import com.lukeneedham.pushtotalkai.generated.resources.settings_state_listening
import com.lukeneedham.pushtotalkai.generated.resources.settings_state_responding
import com.lukeneedham.pushtotalkai.generated.resources.settings_state_stopped
import com.lukeneedham.pushtotalkai.generated.resources.settings_stop
import com.lukeneedham.pushtotalkai.generated.resources.settings_title
import com.lukeneedham.pushtotalkai.generated.resources.settings_grant
import com.lukeneedham.pushtotalkai.ui.component.AppButton
import com.lukeneedham.pushtotalkai.ui.component.AppTextField
import com.lukeneedham.pushtotalkai.ui.component.SetupRow
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** All of the page's UI. `internal`: the screenshots of the app use it too. */
@Composable
internal fun SettingsPageContent(
    state: SettingsUiState,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onToggleAssistant: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val theme = AppTheme.current
    val isRunning = state.assistant !is AssistantState.Stopped
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText(
            text = stringResource(Res.string.settings_title),
            style = TextStyle(
                color = theme.onBackground,
                fontFamily = theme.fontFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        BasicText(
            text = stateText(state.assistant),
            style = TextStyle(
                color = if (state.assistant is AssistantState.Error) theme.error else theme.onBackground,
                fontFamily = theme.fontFamily,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        )
        AppButton(
            text = stringResource(if (isRunning) Res.string.settings_stop else Res.string.settings_start),
            onClick = onToggleAssistant,
            isEnabled = isRunning || state.setup.isMicrophoneGranted,
        )
        AppTextField(
            label = stringResource(Res.string.settings_api_key),
            value = state.apiKey,
            onValueChange = onApiKeyChange,
            isSecret = true,
        )
        AppTextField(
            label = stringResource(Res.string.settings_model),
            value = state.model,
            onValueChange = onModelChange,
        )
        SetupRow(
            title = stringResource(Res.string.settings_microphone_title),
            description = stringResource(Res.string.settings_microphone_description),
            status = setupStatusText(state.setup.isMicrophoneGranted),
            isDone = state.setup.isMicrophoneGranted,
            actionLabel = stringResource(Res.string.settings_grant),
            onAction = onRequestPermissions,
        )
        SetupRow(
            title = stringResource(Res.string.settings_accessibility_title),
            description = stringResource(Res.string.settings_accessibility_description),
            status = setupStatusText(state.setup.isAccessibilityOn),
            isDone = state.setup.isAccessibilityOn,
            actionLabel = stringResource(Res.string.settings_open),
            onAction = onOpenAccessibility,
        )
        SetupRow(
            title = stringResource(Res.string.settings_notification_access_title),
            description = stringResource(Res.string.settings_notification_access_description),
            status = setupStatusText(state.setup.isNotificationAccessOn),
            isDone = state.setup.isNotificationAccessOn,
            actionLabel = stringResource(Res.string.settings_open),
            onAction = onOpenNotificationAccess,
        )
        SetupRow(
            title = stringResource(Res.string.settings_battery_title),
            description = stringResource(Res.string.settings_battery_description),
            status = setupStatusText(state.setup.isBatteryUnrestricted),
            isDone = state.setup.isBatteryUnrestricted,
            actionLabel = stringResource(Res.string.settings_open),
            onAction = onOpenBattery,
        )
        AppButton(text = stringResource(Res.string.settings_logs), onClick = onOpenLogs)
        // Draws nothing unless debugging is on, so it stays in the layout for release builds
        if (state.isDebugEnabled) DebugButton(onClick = onOpenDebug)
    }
}

@Composable
private fun stateText(state: AssistantState): String = when (state) {
    AssistantState.Stopped -> stringResource(Res.string.settings_state_stopped)
    AssistantState.Idle -> stringResource(Res.string.settings_state_idle)
    AssistantState.Listening -> stringResource(Res.string.settings_state_listening)
    AssistantState.Responding -> stringResource(Res.string.settings_state_responding)
    is AssistantState.Error -> stringResource(Res.string.settings_state_error, state.message)
}

@Composable
private fun setupStatusText(isDone: Boolean): String =
    stringResource(if (isDone) Res.string.settings_setup_done else Res.string.settings_setup_needed)

/** For Android Studio while building the page: ready, debugging on. */
@Preview
@Composable
private fun Preview() {
    AppTheme {
        SettingsPageContent(
            state = SettingsUiState(
                isDebugEnabled = true,
                apiKey = "AIza-example",
                model = "gemini-3.1-flash-live-preview",
                setup = SetupStatus(
                    isAccessibilityOn = true,
                    isNotificationAccessOn = false,
                    isMicrophoneGranted = true,
                    isBatteryUnrestricted = false,
                ),
                assistant = AssistantState.Idle,
            ),
            onApiKeyChange = {},
            onModelChange = {},
            onToggleAssistant = {},
            onRequestPermissions = {},
            onOpenAccessibility = {},
            onOpenNotificationAccess = {},
            onOpenBattery = {},
            onOpenLogs = {},
            onOpenDebug = {},
        )
    }
}
