package com.leochat.features.ai.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AiStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette

/** Shown when the AI conversation has no messages. Mirrors `_buildEmptyState`. */
@Composable
fun AiEmptyState(modifier: Modifier = Modifier) {
    val isDark = LocalLeoPalette.current.isDark
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            val shape = RoundedCornerShape(24.dp)
            AiGradientBadge(
                size = 72,
                cornerRadius = 24,
                iconSize = 36,
                modifier = Modifier.shadow(12.dp, shape, ambientColor = AiTokens.gradientGlowSoft, spotColor = AiTokens.gradientGlowSoft),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                AiStrings.emptyTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                AiStrings.emptySubtitle,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = (14 * 1.4).sp,
                color = if (isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary,
            )
        }
    }
}

/** Placeholder card while a reply is generating. Mirrors `_buildGeneratingIndicator`. */
@Composable
fun AiGeneratingIndicator(modifier: Modifier = Modifier) {
    val isDark = LocalLeoPalette.current.isDark
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(if (isDark) LeoColors.darkSurface else Color.White, shape)
            .border(1.dp, if (isDark) LeoColors.darkBorder else AiTokens.slate200, shape)
            .padding(16.dp),
    ) {
        AiGradientBadge(size = 24, cornerRadius = 6, iconSize = 14)
        Spacer(Modifier.width(12.dp))
        Text(
            AiStrings.generating,
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            color = if (isDark) LeoColors.darkTextMuted else AiTokens.slate500,
        )
    }
}
