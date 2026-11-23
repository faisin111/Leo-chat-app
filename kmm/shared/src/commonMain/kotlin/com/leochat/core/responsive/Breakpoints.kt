package com.leochat.core.responsive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Mirrors `lib/core/responsive/breakpoints.dart`. */
enum class LeoLayout { Mobile, Tablet, Desktop }

fun layoutForWidth(width: Dp): LeoLayout = when {
    width < 600.dp -> LeoLayout.Mobile
    width < 1024.dp -> LeoLayout.Tablet
    else -> LeoLayout.Desktop
}

val LocalLeoLayout = staticCompositionLocalOf { LeoLayout.Mobile }

/** Measures the window once and exposes the layout class to descendants. */
@Composable
fun ProvideLeoLayout(content: @Composable () -> Unit) {
    BoxWithConstraints {
        CompositionLocalProvider(LocalLeoLayout provides layoutForWidth(maxWidth)) { content() }
    }
}
