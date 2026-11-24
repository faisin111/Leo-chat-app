package com.leochat.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Neutral values the Flutter chat and contacts screens use beside the LeoColors tokens. */
object ChatTokens {
    val chevronMuted = Color(0xFFCBD5E1)
    val imagePlaceholder = Color(0xFFE2E8F0)
    val imageIcon = Color(0xFF94A3B8)
    val fileTileBackground = Color(0xFFEFF6FF)
    val ownerBadgeBackground = Color(0xFFFFEEE3)
    val ownerBadgeText = Color(0xFFC05621)
    val adminBadgeBackground = Color(0xFFF1F5F9)
    val adminBadgeText = Color(0xFF475569)
    val removeText = Color(0xFFDC2626)
    val filterTrack = Color(0xFFEFE8E1)
    val cardShadow = Color(0x06000000)
    val pillShadow = Color(0x0A000000)
    val heroGlow = Color(0x33E64A19)
    val heroGradient = Brush.verticalGradient(listOf(Color(0xFFFE9642), Color(0xFFDD3F3A)))
    val cardShape = RoundedCornerShape(22.dp)

    /** Glow under the brand send / compose buttons. */
    val sendGlow = Color(0x33E64A19)

    /** Glow under the selected filter bubble. */
    val pillGlow = Color(0x38E64A19)

    /** Group avatar fallback gradient. */
    val groupGradient = Brush.linearGradient(listOf(Color(0xFFFF7A1A), Color(0xFFE53926)), Offset.Zero, Offset.Infinite)

    /** Leo AI header squircle gradient. */
    val aiGradient = Brush.linearGradient(listOf(Color(0xFFFF6D00), LeoColors.primary), Offset.Zero, Offset.Infinite)

    // Message bubbles.
    val deletedGrey = Color(0xFF94A3B8)
    val incomingReplyBackground = Color(0xFFFFEEE3)
    val reactionBorder = Color(0xFFE2E8F0)

    // Typing indicator.
    val typingShadow = Color(0x0D000000)
    val typingBorder = Color(0xFFEFE8E2)

    // Message list.
    val dayLabel = Color(0xFF8D827B)
    val latestPillShadow = Color(0x1F000000)
    val reconnectingBackground = Color(0xFFFFEEE3)

    // Recording pill surfaces from `chat_composer.dart`.
    val recordingPill = Color(0xFFFFEEE3)
    val recordingPillDark = Color(0xFF3B1E12)
    val recordingBorder = Color(0xFFFFD7C2)
    val recordingBorderDark = Color(0xFF7A3416)
    val recordingCancel = Color(0xFFFFD7C2)
    val recordingCancelDark = Color(0xFF5A2414)
    val recordingRed = Color(0xFFDC2626)
}
