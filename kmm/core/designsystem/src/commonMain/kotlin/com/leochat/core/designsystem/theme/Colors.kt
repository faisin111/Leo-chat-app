package com.leochat.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** LeoChat color tokens. Mirrors `lib/core/theme/colors.dart`. */
object LeoColors {
    // Brand orange accent
    val primary = Color(0xFFE64A19)
    val primaryHover = Color(0xFFD84315)
    val primaryLight = Color(0xFFFF5722)
    val primarySoft = Color(0xFFFEF0EA)
    val primarySoftDark = Color(0xFF2B1610)

    // Light theme (warm web aesthetic)
    val background = Color(0xFFFAF7F4)
    val surface = Color(0xFFFFFFFF)
    val surfaceSubtle = Color(0xFFFBF9F7)
    val rail = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF1C1917)
    val textSecondary = Color(0xFF78716C)
    val textMuted = Color(0xFFA8A19B)
    val border = Color(0xFFE7DFD8)
    val borderSubtle = Color(0xFFF0EAE4)
    val input = Color(0xFFF3ECE5)
    val incomingBubble = Color(0xFFF3ECE5)

    // Dark theme (pure OLED black aesthetic)
    val darkBackground = Color(0xFF000000)
    val darkSurface = Color(0xFF000000)
    val darkSurfaceSubtle = Color(0xFF08080A)
    val darkSurfaceElevated = Color(0xFF101014)
    val darkRail = Color(0xFF000000)
    val darkTextPrimary = Color(0xFFFFFFFF)
    val darkTextSecondary = Color(0xFFA1A1AA)
    val darkTextMuted = Color(0xFF71717A)
    val darkBorder = Color(0xFF1E1E22)
    val darkBorderSubtle = Color(0xFF141417)
    val darkInput = Color(0xFF0D0D10)
    val darkIncomingBubble = Color(0xFF121216)

    // Status and utility
    val success = Color(0xFF10B981)
    val destructive = Color(0xFFEF4444)
    val warning = Color(0xFFF59E0B)
    val info = Color(0xFF3B82F6)
    val online = Color(0xFF2E8763)
    val avatarBackground = Color(0xFFFFD8C3)

    // Snack bar surface
    val snackSurface = Color(0xFF241B18)
    val snackSuccess = Color(0xFF34D399)
    val snackError = Color(0xFFF87171)

    // Skeleton and shimmer
    val skeletonFrom = Color(0xFFF3ECE5)
    val skeletonTo = Color(0xFFE5DDD5)
    val shimmerBase = Color(0xFFE8E0D8)
    val shimmerHighlight = Color(0xFFF7F2EC)
    val shimmerCore = Color(0xFFFFF8F2)
}

/** Theme-dependent semantic palette, resolved from light or dark tokens. */
@Immutable
data class LeoPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceElevated: Color,
    val rail: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderSubtle: Color,
    val input: Color,
    val incomingBubble: Color,
    val primarySoft: Color,
) {
    companion object {
        val Light = LeoPalette(
            isDark = false,
            background = LeoColors.background,
            surface = LeoColors.surface,
            surfaceSubtle = LeoColors.surfaceSubtle,
            surfaceElevated = LeoColors.surface,
            rail = LeoColors.rail,
            textPrimary = LeoColors.textPrimary,
            textSecondary = LeoColors.textSecondary,
            textMuted = LeoColors.textMuted,
            border = LeoColors.border,
            borderSubtle = LeoColors.borderSubtle,
            input = LeoColors.input,
            incomingBubble = LeoColors.incomingBubble,
            primarySoft = LeoColors.primarySoft,
        )

        val Dark = LeoPalette(
            isDark = true,
            background = LeoColors.darkBackground,
            surface = LeoColors.darkSurface,
            surfaceSubtle = LeoColors.darkSurfaceSubtle,
            surfaceElevated = LeoColors.darkSurfaceElevated,
            rail = LeoColors.darkRail,
            textPrimary = LeoColors.darkTextPrimary,
            textSecondary = LeoColors.darkTextSecondary,
            textMuted = LeoColors.darkTextMuted,
            border = LeoColors.darkBorder,
            borderSubtle = LeoColors.darkBorderSubtle,
            input = LeoColors.darkInput,
            incomingBubble = LeoColors.darkIncomingBubble,
            primarySoft = LeoColors.primarySoftDark,
        )
    }
}
