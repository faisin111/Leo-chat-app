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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors

/**
 * Floating OLED glassmorphic incoming call banner with radar ring pulse,
 * animated waveform, E2EE badge, and glowing decline/accept controls.
 */
@Composable
fun IncomingCallBanner(call: IncomingCallUi, onDecline: () -> Unit, onAccept: () -> Unit, modifier: Modifier = Modifier) {
    val accent = if (call.isVideo) CallTokens.videoAura else LeoColors.success
    val transition = rememberInfiniteTransition(label = "incomingCallPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.42f,
        animationSpec = infiniteRepeatable(tween(1_500, easing = LinearEasing), RepeatMode.Restart),
        label = "incomingScale",
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1_500, easing = LinearEasing), RepeatMode.Restart),
        label = "incomingAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .shadow(28.dp, CallTokens.bannerShape, ambientColor = CallTokens.bannerShadow, spotColor = CallTokens.bannerShadow)
                .clip(CallTokens.bannerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.14f),
                            CallTokens.cardSurface.copy(alpha = 0.97f),
                            CallTokens.background.copy(alpha = 0.98f),
                        ),
                    ),
                )
                .border(1.dp, CallTokens.glassBorder, CallTokens.bannerShape)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    Box(
                        Modifier
                            .size(54.dp)
                            .scale(pulseScale)
                            .border(1.5.dp, accent.copy(alpha = pulseAlpha), CircleShape)
                            .background(accent.copy(alpha = pulseAlpha * 0.25f), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        LeoAvatar(radius = 27.dp, name = call.callerName, imageUrl = call.callerAvatarUrl)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (call.isVideo) CallTokens.videoGradient else CallTokens.acceptGradient)
                            .border(2.dp, CallTokens.cardSurface, CircleShape),
                    ) {
                        Icon(
                            if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = LeoColors.success, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            CallStrings.encrypted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        call.callerName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CallWaveEqualizer(color = accent)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            if (call.isVideo) CallStrings.incomingVideoCall else CallStrings.incomingVoiceCall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IncomingCallActionPill(
                    label = CallStrings.decline,
                    icon = Icons.Rounded.CallEnd,
                    gradient = CallTokens.destructiveGradient,
                    glow = CallTokens.endGlow,
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                )
                IncomingCallActionPill(
                    label = CallStrings.accept,
                    icon = if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Call,
                    gradient = if (call.isVideo) CallTokens.videoGradient else CallTokens.acceptGradient,
                    glow = CallTokens.acceptGlow,
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun IncomingCallActionPill(
    label: String,
    icon: ImageVector,
    gradient: Brush,
    glow: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .leoPressable(interaction)
            .height(46.dp)
            .shadow(14.dp, RoundedCornerShape(23.dp), ambientColor = glow, spotColor = glow)
            .clip(RoundedCornerShape(23.dp))
            .background(gradient)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
