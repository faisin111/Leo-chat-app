package com.leochat.core.designsystem.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Full-screen page surface that applies safe-area and keyboard insets (Flutter `Scaffold` + `SafeArea`). */
@Composable
fun LeoScreen(
    modifier: Modifier = Modifier,
    background: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val resolvedBg = if (background != Color.Unspecified) background else LeoThemeTokens.palette.background
    Box(modifier = modifier.fillMaxSize().background(resolvedBg)) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding(), content = content)
    }
}
