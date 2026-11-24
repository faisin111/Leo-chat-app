package com.leochat.features.settings.presentation.widgets

import androidx.compose.ui.graphics.Color
import com.leochat.core.theme.LeoColors

/**
 * Settings-only accent literals that the Flutter settings pages use and `LeoColors` does not define.
 * Brand, surface, text, and border colors always come from `LeoColors` / `LeoPalette`.
 */
internal object SettingsColors {
    val switchTrackOff = Color(0xFFD6D3D1)
    val signOut = Color(0xFFDC2626)
    val fieldLabel = Color(0xFF57534E)
    val cardShadow = Color(0x0A000000)
    val avatarBadgeShadow = Color(0x33E64A19)

    // Edit profile "photo checked" banner
    val bannerBackground = Color(0xFFE9F7EF)
    val bannerBorder = Color(0xFFD4EFDF)
    val bannerTitle = Color(0xFF1E8449)
    val bannerBody = Color(0xFF27AE60)

    // Sessions security tip
    val tipBackgroundLight = Color(0xFFFEF3C7)
    val tipBackgroundDark = Color(0xFF451A03).copy(alpha = 0.4f)
    val tipBorderLight = Color(0xFFFDE68A)
    val tipBorderDark = Color(0xFF78350F)
    val tipIcon = Color(0xFFD97706)
    val tipTextLight = Color(0xFF92400E)
    val tipTextDark = Color(0xFFFDE68A)

    // Neutral slate tones from the sessions and appearance pages
    val slateSurface = Color(0xFFF1F5F9)
    val slateIcon = Color(0xFF475569)
    val slateIconMuted = Color(0xFF64748B)
    val slateText = Color(0xFF1E293B)

    /** Appearance accent swatches: brand, rose, sky blue, emerald, purple. */
    val accents = listOf(
        LeoColors.primary,
        Color(0xFFF43F5E),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFF8B5CF6),
    )
}
