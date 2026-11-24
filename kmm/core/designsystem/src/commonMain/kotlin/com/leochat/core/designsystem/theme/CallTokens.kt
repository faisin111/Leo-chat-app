package com.leochat.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Call-screen surfaces from `active_call_page.dart` (slate night palette). */
object CallTokens {
    val background = Color(0xFF0F172A)
    val videoSurface = Color(0xFF1E293B)
    val ringing = Color(0xFFFFC107)
    val endGlow = Color(0x66EF4444)
    val chip = Color.Black.copy(alpha = 0.4f)
    val bannerShadow = Color(0x44000000)
    val bannerShape = RoundedCornerShape(24.dp)
}
