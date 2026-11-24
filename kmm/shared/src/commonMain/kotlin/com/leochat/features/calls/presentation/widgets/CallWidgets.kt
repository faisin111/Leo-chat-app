package com.leochat.features.calls.presentation.widgets

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.widgets.common.buttons.leoPressable

/** Call-screen surfaces from `active_call_page.dart` (slate night palette). */
internal object CallColors {
    val background = Color(0xFF0F172A)
    val videoSurface = Color(0xFF1E293B)
    val ringing = Color(0xFFFFC107)
    val endGlow = Color(0x66EF4444)
    val chip = Color.Black.copy(alpha = 0.4f)
}

/** Formats seconds as `mm:ss`, like `_formatDuration`. */
internal fun formatCallDuration(totalSeconds: Int): String {
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

/** Pill shown in the call top bar. */
@Composable
internal fun CallStatusChip(background: Color, leading: @Composable () -> Unit, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(background, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        leading()
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

/** Round icon control with a caption. Mirrors `_CallControlButton`. */
@Composable
internal fun CallControlButton(icon: ImageVector, label: String, isActive: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircleAction(
            icon = icon,
            size = 54.dp,
            iconSize = 24.dp,
            background = if (isActive) Color.White else Color.White.copy(alpha = 0.15f),
            tint = if (isActive) CallColors.background else Color.White,
            contentDescription = label,
            onClick = onClick,
        )
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
internal fun CircleAction(
    icon: ImageVector,
    size: Dp,
    iconSize: Dp,
    background: Color,
    tint: Color,
    contentDescription: String?,
    glow: Color? = null,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val base = Modifier.leoPressable(interaction).size(size)
    Box(
        contentAlignment = Alignment.Center,
        modifier = (if (glow != null) base.shadow(16.dp, CircleShape, ambientColor = glow, spotColor = glow) else base)
            .clip(CircleShape)
            .background(background)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
internal fun Dot(color: Color, size: Dp = 8.dp) {
    Box(Modifier.size(size).background(color, CircleShape))
}

internal val ControlsArrangement = Arrangement.SpaceEvenly
