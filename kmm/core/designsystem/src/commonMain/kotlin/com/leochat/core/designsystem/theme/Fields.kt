package com.leochat.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared input geometry and colors. Mirrors `lib/core/theme/fields.dart`. */
object LeoFields {
    val shape = RoundedCornerShape(16.dp)
    val borderWidth = 1.2.dp
    val focusedBorderWidth = 1.5.dp
    const val iconSize = 22

    val authTextStyle: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textPrimary,
        )

    val authHintStyle: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = LeoThemeTokens.palette.textMuted,
        )

    val authLabelStyle: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LeoThemeTokens.palette.textSecondary,
        )

    @Composable
    fun authColors(isDark: Boolean = LeoThemeTokens.palette.isDark): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) LeoColors.darkSurface else LeoColors.surface,
        unfocusedContainerColor = if (isDark) LeoColors.darkSurface else LeoColors.surface,
        focusedBorderColor = LeoColors.primary,
        unfocusedBorderColor = if (isDark) LeoColors.darkBorder else LeoColors.border,
        focusedPlaceholderColor = if (isDark) LeoColors.darkTextMuted else LeoColors.textMuted,
        unfocusedPlaceholderColor = if (isDark) LeoColors.darkTextMuted else LeoColors.textMuted,
        focusedTrailingIconColor = if (isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary,
        unfocusedTrailingIconColor = if (isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary,
        focusedTextColor = if (isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary,
        unfocusedTextColor = if (isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary,
        cursorColor = LeoColors.primary,
    )
}
