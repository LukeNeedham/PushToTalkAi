package com.lukeneedham.pushtotalkai.ui

import androidx.compose.runtime.Composable
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme
import com.lukeneedham.pushtotalkai.ui.theme.toDebugTheme
import com.lukeneedham.common.ui.ProvideDebugSettings

/**
 * The one composable `MainActivity` calls: everything the UI sits in, then the navigation. [onRequestPermissions] is what only the
 * activity can do: ask for the microphone and notification permissions.
 */
@Composable
fun Root(onRequestPermissions: () -> Unit) {
    AppTheme {
        ProvideDebugSettings(theme = AppTheme.current.toDebugTheme()) {
            AppNavigation(onRequestPermissions = onRequestPermissions)
        }
    }
}
