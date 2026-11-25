package com.leochat.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Modern OLED glassmorphic palette and shapes for calling screens and incoming call banners. */
object CallTokens {
    val background = Color(0xFF04060B)
    val cardSurface = Color(0xFF0B0F18)
    val videoSurface = Color(0xFF0E131F)
    val glassSurface = Color.White.copy(alpha = 0.08f)
    val glassBorder = Color.White.copy(alpha = 0.13f)
    val ringing = Color(0xFFF59E0B)
    val outgoingAura = Color(0xFF38BDF8)
    val videoAura = Color(0xFF8B5CF6)
    val endGlow = Color(0x77EF4444)
    val acceptGlow = Color(0x6610B981)
    val chip = Color.White.copy(alpha = 0.09f)
    val bannerShadow = Color(0x88000000)
    val bannerShape = RoundedCornerShape(28.dp)
    val dockShape = RoundedCornerShape(34.dp)

    val destructiveGradient = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
    val acceptGradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    val videoGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))
}
