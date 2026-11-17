package com.lukeneedham.pushtotalkai.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukeneedham.pushtotalkai.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** A single-line text field with a [label] above it. [isSecret] hides what is typed. */
@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
) {
    val theme = AppTheme.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(
            text = label,
            style = TextStyle(color = theme.onBackground, fontFamily = theme.fontFamily, fontSize = 13.sp, lineHeight = 18.sp),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = theme.onSurface, fontFamily = theme.fontFamily, fontSize = 16.sp, lineHeight = 22.sp),
            cursorBrush = SolidColor(theme.onSurface),
            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surface)
                .heightIn(min = 48.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme { AppTextField(label = "API key", value = "secret", onValueChange = {}, isSecret = true) }
}
