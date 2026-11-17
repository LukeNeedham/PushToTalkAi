package com.lukeneedham.pushtotalkai.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lukeneedham.pushtotalkai.ui.SettingsPageContent
import com.lukeneedham.pushtotalkai.ui.SettingsUiState
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-SettingsPage.png. */
@Preview
@Composable
private fun SettingsPageScreenshot() {
    val state = SettingsUiState(isDebugEnabled = true)
    AppTheme { SettingsPageContent(state = state, onOpenDebug = {}) }
}
