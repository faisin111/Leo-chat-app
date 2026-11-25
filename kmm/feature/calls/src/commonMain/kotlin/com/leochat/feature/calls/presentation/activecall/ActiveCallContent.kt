package com.leochat.feature.calls.presentation.activecall

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.call.CallCircleAction
import com.leochat.core.designsystem.component.call.CallControlButton
import com.leochat.core.designsystem.component.call.CallDot
import com.leochat.core.designsystem.component.call.CallStatusChip
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.domain.model.CallStatus
import com.leochat.feature.calls.presentation.CallUiState
import com.leochat.feature.calls.presentation.mapper.statusText
import com.leochat.feature.calls.presentation.mapper.timerText

/** Stateless active call layout: slate background plus the call, when one is active. */
@Composable
fun ActiveCallContent(
    state: CallUiState,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(CallTokens.background)) {
        state.activeCall?.let { call ->
            ActiveCallBody(
                call = call,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker,
                onFlipCamera = onFlipCamera,
                onEnd = onEnd,
            )
        }
    }
}

@Composable
private fun ActiveCallBody(
    call: CallSession,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEnd: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        if (call.isVideo) VideoPlaceholder(call.isCameraFront)
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            CallTopBar(call, Modifier.align(Alignment.TopCenter).padding(16.dp))
            CallerInfo(call, call.statusText(), Modifier.align(Alignment.Center))
            CallControls(
                call = call,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker,
                onFlipCamera = onFlipCamera,
                onEnd = onEnd,
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun VideoPlaceholder(isCameraFront: Boolean) {
    Box(Modifier.fillMaxSize().background(CallTokens.videoSurface), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Videocam, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                if (isCameraFront) CallStrings.frontCameraActive else CallStrings.backCameraActive,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun CallTopBar(call: CallSession, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CallStatusChip(
            background = CallTokens.chip,
            leading = {
                Icon(
                    if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Phone,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            },
            text = if (call.isVideo) CallStrings.leoVideo else CallStrings.leoAudio,
        )
        Spacer(Modifier.weight(1f))
        CallStatusChip(
            background = if (call.isConnected) LeoColors.success.copy(alpha = 0.25f) else CallTokens.chip,
            leading = { CallDot(if (call.isConnected) LeoColors.success else CallTokens.ringing) },
            text = call.timerText(),
        )
    }
}

@Composable
private fun CallerInfo(call: CallSession, statusText: String, modifier: Modifier) {
    val isWaiting = call.status == CallStatus.OUTGOING || call.status == CallStatus.RINGING
    val pulseTransition = rememberInfiniteTransition(label = "callRingPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isWaiting) 1.38f else 1.0f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseScale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = if (isWaiting) 0.45f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1_400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseAlpha",
    )
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isWaiting) {
                val ringColor = if (call.status == CallStatus.RINGING) LeoColors.success else CallTokens.ringing
                Box(
                    Modifier
                        .size(128.dp)
                        .scale(pulseScale)
                        .background(ringColor.copy(alpha = pulseAlpha), CircleShape),
                )
            }
            if (call.isConnected) Box(Modifier.size(140.dp).background(LeoColors.primary.copy(alpha = 0.15f), CircleShape))
            Box(Modifier.background(Color.White, CircleShape).padding(4.dp)) {
                LeoAvatar(radius = 54.dp, name = call.targetUserName, imageUrl = call.targetAvatarUrl)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(call.targetUserName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(statusText, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun CallControls(
    call: CallSession,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
        CallControlButton(
            icon = if (call.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
            label = if (call.isMuted) CallStrings.unmute else CallStrings.mute,
            isActive = call.isMuted,
            onClick = onToggleMute,
        )
        CallControlButton(
            icon = if (call.isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeDown,
            label = CallStrings.speaker,
            isActive = call.isSpeakerOn,
            onClick = onToggleSpeaker,
        )
        if (call.isVideo) CallControlButton(Icons.Rounded.FlipCameraIos, CallStrings.flip, onClick = onFlipCamera)
        CallCircleAction(
            icon = Icons.Rounded.CallEnd,
            size = 64.dp,
            iconSize = 30.dp,
            background = LeoColors.destructive,
            tint = Color.White,
            contentDescription = CallStrings.endCall,
            glow = CallTokens.endGlow,
            onClick = onEnd,
        )
    }
}
