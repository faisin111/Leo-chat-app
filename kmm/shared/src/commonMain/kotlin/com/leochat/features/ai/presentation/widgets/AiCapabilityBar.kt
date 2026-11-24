package com.leochat.features.ai.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AiStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette

/** Reasoning and web-search toggles under the app bar. */
@Composable
fun AiCapabilityBar(
    isDeepThinkEnabled: Boolean,
    isWebSearchEnabled: Boolean,
    onToggleDeepThink: () -> Unit,
    onToggleWebSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalLeoPalette.current.isDark
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) LeoColors.darkSurface.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        AiCapabilityChip(Icons.Rounded.Psychology, AiStrings.reasoning, isDeepThinkEnabled, isDark, onToggleDeepThink)
        Spacer(Modifier.width(8.dp))
        AiCapabilityChip(Icons.Rounded.Public, AiStrings.webSearch, isWebSearchEnabled, isDark, onToggleWebSearch)
    }
}

/** Mirrors `_CapabilityChip`. */
@Composable
fun AiCapabilityChip(icon: ImageVector, label: String, isActive: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val content = when {
        isActive -> LeoColors.primary
        isDark -> LeoColors.darkTextSecondary
        else -> AiTokens.slate500
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .aiPressable(onClick = onClick)
            .background(
                when {
                    isActive -> if (isDark) LeoColors.primarySoftDark else LeoColors.primarySoft
                    isDark -> LeoColors.darkInput
                    else -> AiTokens.slate100
                },
                shape,
            )
            .border(
                1.dp,
                when {
                    isActive -> LeoColors.primary
                    isDark -> LeoColors.darkBorder
                    else -> AiTokens.slate200
                },
                shape,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = content)
    }
}
