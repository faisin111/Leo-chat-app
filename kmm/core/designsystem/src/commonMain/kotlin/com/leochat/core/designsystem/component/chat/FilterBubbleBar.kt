package com.leochat.core.designsystem.component.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.component.chat.model.FilterOption
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

private val PillShape = RoundedCornerShape(999.dp)
private val SelectedGradient = Brush.linearGradient(listOf(LeoColors.primary, LeoColors.primaryLight), Offset.Zero, Offset.Infinite)

/** Horizontally scrolling bubble filter (All / Unread / Groups). Mirrors `LeoFilterBubbleBar`. */
@Composable
fun <T> FilterBubbleBar(
    options: List<FilterOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            BubblePill(option.label, option.value == selected, badge = option.badge) { onSelect(option.value) }
        }
    }
}

@Composable
private fun BubblePill(label: String, isSelected: Boolean, badge: String? = null, onClick: () -> Unit) {
    val palette = LeoThemeTokens.palette
    val scale by animateFloatAsState(if (isSelected) 1.04f else 1f, tween(240, easing = EaseOutBack), label = "pillScale")
    val textColor by animateColorAsState(if (isSelected) Color.White else palette.textSecondary, tween(240), label = "pillText")
    val interaction = remember { MutableInteractionSource() }
    val base = Modifier.scale(scale).leoPressable(interaction)
    val decorated = if (isSelected) {
        base.shadow(6.dp, PillShape, ambientColor = ChatTokens.pillGlow, spotColor = ChatTokens.pillGlow).background(SelectedGradient, PillShape)
    } else {
        base.background(palette.input, PillShape)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = decorated
            .clip(PillShape)
            .clickable(interactionSource = interaction, indication = ripple(color = LeoColors.primary), onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.5.dp),
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.2).sp, color = textColor)
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                badge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) LeoColors.primary else Color.White,
                modifier = Modifier
                    .background(if (isSelected) Color.White else LeoColors.primary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
    }
}
