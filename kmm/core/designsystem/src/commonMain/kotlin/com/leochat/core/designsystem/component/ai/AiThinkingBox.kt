package com.leochat.core.designsystem.component.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Collapsible reasoning trace above an assistant reply with smooth expand/collapse and theme awareness. */
@Composable
fun AiThinkingBox(thinkingContent: String, durationText: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val palette = LocalLeoPalette.current
    val shape = RoundedCornerShape(14.dp)
    val bgColor = if (palette.isDark) palette.surfaceElevated else AiTokens.slate50
    val borderColor = if (palette.isDark) palette.border else AiTokens.slate200
    val headerColor = if (palette.isDark) palette.textPrimary else AiTokens.slate600
    val iconColor = if (palette.isDark) palette.textMuted else AiTokens.slate500
    val textColor = if (palette.isDark) palette.textSecondary else AiTokens.slate700

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thinking_chevron_rot",
    )

    Column(
        modifier = modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .aiPressable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(Icons.Rounded.Psychology, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                AiStrings.thinkingProcess(durationText),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = headerColor,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp).rotate(chevronRotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                thinkingContent,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = (12 * 1.5).sp,
                color = textColor,
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            )
        }
    }
}
