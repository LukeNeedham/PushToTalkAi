package com.lukeneedham.pushtotalkai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** One thing the user switches on in Android: what it is for, whether it is on ([status]) and the button to the system page. */
@Composable
fun SetupRow(
    title: String,
    description: String,
    status: String,
    isDone: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AppTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = title,
                style = TextStyle(color = theme.onSurface, fontFamily = theme.fontFamily, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
            )
            BasicText(
                text = description,
                style = TextStyle(color = theme.onSurface, fontFamily = theme.fontFamily, fontSize = 13.sp, lineHeight = 18.sp),
            )
            BasicText(
                text = status,
                style = TextStyle(
                    color = if (isDone) theme.primary else theme.warning,
                    fontFamily = theme.fontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        if (!isDone) AppButton(text = actionLabel, onClick = onAction)
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        SetupRow(
            title = "Accessibility",
            description = "Reads the headset button",
            status = "Needed",
            isDone = false,
            actionLabel = "Open",
            onAction = {},
        )
    }
}
