package com.leochat.core.designsystem.component.call

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.theme.CallTokens

/** Frosted pill shown in the call top bar. */
@Composable
fun CallStatusChip(
    background: Color,
    leading: @Composable () -> Unit,
    text: String,
    borderColor: Color = CallTokens.glassBorder,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(background, RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        leading()
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

/** Round glass icon control with a caption. */
@Composable
fun CallControlButton(icon: ImageVector, label: String, isActive: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CallCircleAction(
            icon = icon,
            size = 56.dp,
            iconSize = 24.dp,
            background = if (isActive) Color.White else Color.White.copy(alpha = 0.12f),
            tint = if (isActive) CallTokens.background else Color.White,
            contentDescription = label,
            borderColor = if (isActive) Color.White else CallTokens.glassBorder,
            onClick = onClick,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.78f),
        )
    }
}

/** Pressable circular icon button with optional gradient fill, border, and glow. */
@Composable
fun CallCircleAction(
    icon: ImageVector,
    size: Dp,
    iconSize: Dp,
    background: Color,
    tint: Color,
    contentDescription: String?,
    glow: Color? = null,
    brush: Brush? = null,
    borderColor: Color? = null,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val base = Modifier.leoPressable(interaction).size(size)
    val shaped = (if (glow != null) base.shadow(18.dp, CircleShape, ambientColor = glow, spotColor = glow) else base)
        .clip(CircleShape)
    val painted = if (brush != null) shaped.background(brush) else shaped.background(background)
    val bordered = if (borderColor != null) painted.border(1.dp, borderColor, CircleShape) else painted
    Box(
        contentAlignment = Alignment.Center,
        modifier = bordered.clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Solid status dot. */
@Composable
fun CallDot(color: Color, size: Dp = 8.dp) {
    Box(Modifier.size(size).background(color, CircleShape))
}

/** Animated mini waveform equalizer for active/ringing call states. */
@Composable
fun CallWaveEqualizer(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "callWave")
    val barTargets = listOf(0.55f, 1.0f, 0.7f, 0.9f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(14.dp),
    ) {
        barTargets.forEachIndexed { index, peak ->
            val factor by transition.animateFloat(
                initialValue = 0.28f,
                targetValue = peak,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 520 + index * 110, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "waveBar$index",
            )
            Box(
                Modifier
                    .width(2.5.dp)
                    .height(14.dp * factor)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
