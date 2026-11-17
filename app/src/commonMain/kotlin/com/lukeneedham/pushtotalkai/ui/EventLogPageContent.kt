package com.lukeneedham.pushtotalkai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukeneedham.pushtotalkai.generated.resources.Res
import com.lukeneedham.pushtotalkai.generated.resources.event_log_back
import com.lukeneedham.pushtotalkai.generated.resources.event_log_clear
import com.lukeneedham.pushtotalkai.generated.resources.event_log_empty
import com.lukeneedham.pushtotalkai.generated.resources.event_log_title
import com.lukeneedham.pushtotalkai.ui.component.AppButton
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** All of the page's UI. `internal`: the screenshots of the app use it too. */
@Composable
internal fun EventLogPageContent(
    state: EventLogUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    val theme = AppTheme.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(
            text = stringResource(Res.string.event_log_title),
            style = TextStyle(
                color = theme.onBackground,
                fontFamily = theme.fontFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = stringResource(Res.string.event_log_back), onClick = onBack)
            AppButton(text = stringResource(Res.string.event_log_clear), onClick = onClear)
        }
        if (state.lines.isEmpty()) {
            BasicText(
                text = stringResource(Res.string.event_log_empty),
                style = TextStyle(color = theme.onBackground, fontFamily = theme.fontFamily, fontSize = 14.sp, lineHeight = 20.sp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.lines) { line ->
                    BasicText(
                        text = line,
                        style = TextStyle(color = theme.onBackground, fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp),
                    )
                }
            }
        }
    }
}

/** For Android Studio while building the page. */
@Preview
@Composable
private fun Preview() {
    AppTheme {
        EventLogPageContent(
            state = EventLogUiState(
                lines = listOf(
                    "12:03:04.512  KEYCODE_HEADSETHOOK  UP  dev=headset repeat=0 consumed",
                    "12:03:04.101  KEYCODE_HEADSETHOOK  DOWN  dev=headset repeat=0 consumed",
                ),
            ),
            onBack = {},
            onClear = {},
        )
    }
}
