package com.leochat.core.designsystem.theme

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
    primaryContainer = LeoColors.primarySoft,
    onPrimaryContainer = LeoColors.primaryHover,
    surface = LeoColors.surface,
    onSurface = LeoColors.textPrimary,
    surfaceVariant = LeoColors.surfaceSubtle,
    onSurfaceVariant = LeoColors.textSecondary,
    background = LeoColors.background,
    onBackground = LeoColors.textPrimary,
    error = LeoColors.destructive,
    onError = Color.White,
    outline = LeoColors.border,
    outlineVariant = LeoColors.borderSubtle,
)

private val DarkScheme = darkColorScheme(
    primary = LeoColors.primary,
    onPrimary = Color.White,
    primaryContainer = LeoColors.primarySoftDark,
    onPrimaryContainer = LeoColors.primaryLight,
    surface = LeoColors.darkSurface,
    onSurface = LeoColors.darkTextPrimary,
    surfaceVariant = LeoColors.darkSurfaceElevated,
    onSurfaceVariant = LeoColors.darkTextSecondary,
    background = LeoColors.darkBackground,
    onBackground = LeoColors.darkTextPrimary,
    error = LeoColors.destructive,
    onError = Color.White,
    outline = LeoColors.darkBorder,
    outlineVariant = LeoColors.darkBorderSubtle,
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
    val palette = if (dark) LeoPalette.Dark else LeoPalette.Light
    CompositionLocalProvider(
        LocalLeoPalette provides palette,
        androidx.compose.material3.LocalContentColor provides palette.textPrimary,
    ) {
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
