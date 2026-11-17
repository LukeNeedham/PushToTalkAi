package com.lukeneedham.pushtotalkai.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lukeneedham.pushtotalkai.assistant.AssistantState
import com.lukeneedham.pushtotalkai.assistant.SetupStatus
import com.lukeneedham.pushtotalkai.ui.SettingsPageContent
import com.lukeneedham.pushtotalkai.ui.SettingsUiState
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-SettingsPage.png. */
@Preview
@Composable
private fun SettingsPageScreenshot() {
    val state = SettingsUiState(
        isDebugEnabled = false,
        apiKey = "AIzaSyExampleKey",
        model = "gemini-3.1-flash-live-preview",
        setup = SetupStatus(
            isAccessibilityOn = true,
            isNotificationAccessOn = false,
            isMicrophoneGranted = true,
            isBatteryUnrestricted = false,
        ),
        assistant = AssistantState.Idle,
    )
    AppTheme {
        SettingsPageContent(
            state = state,
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
