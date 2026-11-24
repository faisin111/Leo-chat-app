package com.leochat.features.ai.presentation.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.features.ai.domain.entities.AiModelIcon

/**
 * Leo AI surface tokens. The Flutter AI tab uses a slate scale and an
 * orange-to-rose brand gradient that only this feature renders.
 */
internal object AiTokens {
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

internal fun AiModelIcon.imageVector(): ImageVector = when (this) {
    AiModelIcon.Sparkles -> Icons.Filled.AutoAwesome
    AiModelIcon.Brain -> Icons.Rounded.Psychology
    AiModelIcon.Code -> Icons.Rounded.Code
    AiModelIcon.Zap -> Icons.Rounded.Bolt
}

/** `LeoPressable` equivalent: scale-bounce feedback without a ripple. */
internal fun Modifier.aiPressable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .leoPressable(interaction, enabled = enabled)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}
