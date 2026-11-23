package com.leochat.core.widgets.common.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.leochat.core.theme.LeoColors

/** Full-screen page surface that applies safe-area and keyboard insets (Flutter `Scaffold` + `SafeArea`). */
@Composable
fun LeoScreen(
    modifier: Modifier = Modifier,
    background: Color = LeoColors.background,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(background)) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding(), content = content)
    }
}
