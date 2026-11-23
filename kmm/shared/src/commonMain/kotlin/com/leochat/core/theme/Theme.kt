package com.leochat.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class LeoThemeMode { Light, Dark, System }

val LocalLeoPalette = staticCompositionLocalOf { LeoPalette.Light }

private val LightScheme = lightColorScheme(
    primary = LeoColors.primary,
    onPrimary = Color.White,
    surface = LeoColors.surface,
    onSurface = LeoColors.textPrimary,
    background = LeoColors.background,
    onBackground = LeoColors.textPrimary,
    error = LeoColors.destructive,
    outline = LeoColors.border,
    outlineVariant = LeoColors.border,
)

private val DarkScheme = darkColorScheme(
    primary = LeoColors.primary,
    onPrimary = Color.White,
    surface = LeoColors.darkSurface,
    onSurface = LeoColors.darkTextPrimary,
    background = LeoColors.darkBackground,
    onBackground = LeoColors.darkTextPrimary,
    error = LeoColors.destructive,
    outline = LeoColors.darkBorder,
    outlineVariant = LeoColors.darkBorder,
)

/** App-wide theme: Material 3 + Poppins + LeoChat palette. Mirrors `LeoTheme` in Flutter. */
@Composable
fun LeoTheme(
    mode: LeoThemeMode = LeoThemeMode.Light,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        LeoThemeMode.Light -> false
        LeoThemeMode.Dark -> true
        LeoThemeMode.System -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalLeoPalette provides if (dark) LeoPalette.Dark else LeoPalette.Light) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = leoTypography(),
            content = content,
        )
    }
}

object LeoThemeTokens {
    val palette: LeoPalette
        @Composable @ReadOnlyComposable get() = LocalLeoPalette.current
}
