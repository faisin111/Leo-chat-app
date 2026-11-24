package com.leochat.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Leo AI surfaces: brand gradient, slate scale, and the pending-action banner palette. */
object AiTokens {
    val gradientStart = Color(0xFFF97316)
    val gradientEnd = Color(0xFFF43F5E)
    val gradient = Brush.linearGradient(listOf(gradientStart, gradientEnd))
    val gradientGlowSoft = gradientStart.copy(alpha = 0.3f)
    val gradientGlow = gradientStart.copy(alpha = 0.35f)

    val slate50 = Color(0xFFF8FAFC)
    val slate100 = Color(0xFFF1F5F9)
    val slate200 = Color(0xFFE2E8F0)
    val slate300 = Color(0xFFCBD5E1)
    val slate400 = Color(0xFF94A3B8)
    val slate500 = Color(0xFF64748B)
    val slate600 = Color(0xFF475569)
    val slate700 = Color(0xFF334155)
    val slate800 = Color(0xFF1E293B)
    val cardShadow = Color(0x08000000)

    // Pending action banner
    val actionSurfaceLight = Color(0xFFFFF7ED)
    val actionSurfaceDark = Color(0xFF2C2014)
    val actionTitleLight = Color(0xFF9A3412)
    val actionBodyLight = Color(0xFFC2410C)
}
