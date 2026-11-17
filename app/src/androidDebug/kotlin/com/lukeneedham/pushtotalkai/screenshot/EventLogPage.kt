package com.lukeneedham.pushtotalkai.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lukeneedham.pushtotalkai.ui.EventLogPageContent
import com.lukeneedham.pushtotalkai.ui.EventLogUiState
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme

/** Rendered by CI to docs/screenshots/<position in order.txt>-EventLogPage.png. */
@Preview
@Composable
private fun EventLogPageScreenshot() {
    val state = EventLogUiState(
        lines = listOf(
            "21:14:08.912  KEYCODE_HEADSETHOOK  UP  dev=Headset repeat=0 consumed",
            "21:14:07.304  KEYCODE_HEADSETHOOK  DOWN  dev=Headset repeat=0 consumed",
            "21:13:52.077  KEYCODE_VOLUME_UP  UP  dev=Headset repeat=0",
            "21:13:51.801  KEYCODE_VOLUME_UP  DOWN  dev=Headset repeat=0",
        ),
    )
    AppTheme { EventLogPageContent(state = state, onBack = {}, onClear = {}) }
}
