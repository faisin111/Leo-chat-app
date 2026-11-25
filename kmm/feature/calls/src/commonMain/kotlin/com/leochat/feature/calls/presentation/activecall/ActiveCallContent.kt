package com.leochat.feature.calls.presentation.activecall

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.leochat.core.designsystem.component.call.CallWaveEqualizer
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.domain.model.CallStatus
import com.leochat.feature.calls.presentation.CallUiState
import com.leochat.feature.calls.presentation.mapper.statusText
import com.leochat.feature.calls.presentation.mapper.timerText

typealias CallVideoSlot = @Composable (isLocal: Boolean, isMirror: Boolean, modifier: Modifier) -> Unit

/** Stateless OLED glassmorphic active call layout. */
@Composable
fun ActiveCallContent(
    state: CallUiState,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleCamera: () -> Unit = {},
    videoSlot: CallVideoSlot = { _, _, _ -> },
) {
    Box(modifier.fillMaxSize().background(CallTokens.background)) {
        state.activeCall?.let { call ->
            ActiveCallBody(
                call = call,
                hasLocalVideo = state.hasLocalVideo,
                hasRemoteVideo = state.hasRemoteVideo,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker,
                onFlipCamera = onFlipCamera,
                onToggleCamera = onToggleCamera,
                onEnd = onEnd,
                videoSlot = videoSlot,
            )
        }
    }
}

@Composable
private fun ActiveCallBody(
    call: CallSession,
    hasLocalVideo: Boolean,
    hasRemoteVideo: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onToggleCamera: () -> Unit,
    onEnd: () -> Unit,
    videoSlot: CallVideoSlot,
) {
    val auraColor = when {
        call.isConnected -> LeoColors.success
        call.status == CallStatus.RINGING -> LeoColors.success
        call.isVideo -> CallTokens.videoAura
        else -> CallTokens.outgoingAura
    }
    Box(Modifier.fillMaxSize()) {
        // Ambient OLED radial aura
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = 0.22f),
                            LeoColors.primary.copy(alpha = 0.08f),
                            CallTokens.background,
                        ),
                    ),
                ),
        )
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (call.isVideo) {
                VideoCallStage(
                    call = call,
                    hasLocalVideo = hasLocalVideo,
                    hasRemoteVideo = hasRemoteVideo,
                    videoSlot = videoSlot,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, top = 66.dp, bottom = 112.dp),
                )
            } else {
                CallerInfo(call, call.statusText(), Modifier.align(Alignment.Center).padding(bottom = 36.dp))
            }
            CallTopBar(call, Modifier.align(Alignment.TopCenter).padding(horizontal = 18.dp, vertical = 14.dp))
            CallControls(
                call = call,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker,
                onFlipCamera = onFlipCamera,
                onToggleCamera = onToggleCamera,
                onEnd = onEnd,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
            )
        }
    }
}

@Composable
private fun VideoCallStage(
    call: CallSession,
    hasLocalVideo: Boolean,
    hasRemoteVideo: Boolean,
    videoSlot: CallVideoSlot,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val isWideLandscape = maxWidth >= 600.dp && maxWidth > maxHeight
        if (isWideLandscape) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RemoteVideoCard(
                    call = call,
                    hasRemoteVideo = hasRemoteVideo,
                    videoSlot = videoSlot,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                LocalVideoCard(
                    call = call,
                    hasLocalVideo = hasLocalVideo,
                    videoSlot = videoSlot,
                    isCompact = false,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                RemoteVideoCard(
                    call = call,
                    hasRemoteVideo = hasRemoteVideo,
                    videoSlot = videoSlot,
                    modifier = Modifier.fillMaxSize(),
                )
                LocalVideoCard(
                    call = call,
                    hasLocalVideo = hasLocalVideo,
                    videoSlot = videoSlot,
                    isCompact = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(width = 118.dp, height = 166.dp),
                )
            }
        }
    }
}

@Composable
private fun RemoteVideoCard(
    call: CallSession,
    hasRemoteVideo: Boolean,
    videoSlot: CallVideoSlot,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(CallTokens.videoSurface)
            .border(1.dp, CallTokens.glassBorder, shape),
    ) {
        if (hasRemoteVideo) {
            videoSlot(false, false, Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CallerInfo(call, call.statusText(), Modifier.padding(horizontal = 20.dp))
            }
        }

        // Participant status pill at bottom-left, matching Web ActiveCallModal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.56f))
                .border(1.dp, CallTokens.glassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            CallDot(if (hasRemoteVideo || call.isConnected) LeoColors.success else CallTokens.ringing, size = 7.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                call.targetUserName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            if (!hasRemoteVideo && call.isConnected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    CallStrings.waitingForVideo,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
private fun LocalVideoCard(
    call: CallSession,
    hasLocalVideo: Boolean,
    videoSlot: CallVideoSlot,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(if (isCompact) 20.dp else 26.dp)
    val showCamera = hasLocalVideo && !call.isCameraOff
    Box(
        modifier = modifier
            .clip(shape)
            .background(CallTokens.cardSurface)
            .border(1.5.dp, Color.White.copy(alpha = 0.22f), shape),
    ) {
        if (showCamera) {
            videoSlot(true, call.isCameraFront, Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 40.dp else 56.dp)
                        .clip(CircleShape)
                        .background(CallTokens.glassSurface)
                        .border(1.dp, CallTokens.glassBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.VideocamOff,
                        contentDescription = CallStrings.cameraOff,
                        tint = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(if (isCompact) 20.dp else 26.dp),
                    )
                }
                if (!isCompact || call.isCameraOff) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        CallStrings.cameraOff,
                        fontSize = if (isCompact) 10.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
        }

        // Bottom-left "You" + mute badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.56f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                CallStrings.youLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            if (call.isMuted) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Rounded.MicOff,
                    contentDescription = CallStrings.unmute,
                    tint = LeoColors.destructive,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun CallTopBar(call: CallSession, modifier: Modifier) {
    val activeDotColor = when {
        call.isConnected || call.status == CallStatus.RINGING -> LeoColors.success
        else -> CallTokens.ringing
    }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CallStatusChip(
            background = CallTokens.chip,
            leading = {
                Icon(
                    if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Phone,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp),
                )
            },
            text = if (call.isVideo) CallStrings.leoVideo else CallStrings.leoAudio,
        )
        Spacer(Modifier.weight(1f))
        CallStatusChip(
            background = if (call.isConnected || call.status == CallStatus.RINGING) {
                LeoColors.success.copy(alpha = 0.18f)
            } else {
                CallTokens.chip
            },
            borderColor = if (call.isConnected || call.status == CallStatus.RINGING) {
                LeoColors.success.copy(alpha = 0.35f)
            } else {
                CallTokens.glassBorder
            },
            leading = { CallDot(activeDotColor) },
            text = call.timerText(),
        )
    }
}

@Composable
private fun CallerInfo(call: CallSession, statusText: String, modifier: Modifier) {
    val isWaiting = call.status == CallStatus.OUTGOING || call.status == CallStatus.RINGING
    val ringColor = when {
        call.status == CallStatus.RINGING || call.isConnected -> LeoColors.success
        call.isVideo -> CallTokens.videoAura
        else -> CallTokens.outgoingAura
    }
    val pulseTransition = rememberInfiniteTransition(label = "callRingPulse")
    val pulse1 by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isWaiting) 1.52f else 1.12f,
        animationSpec = infiniteRepeatable(tween(1_800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse1",
    )
    val alpha1 by pulseTransition.animateFloat(
        initialValue = if (isWaiting) 0.45f else if (call.isConnected) 0.2f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1_800, easing = LinearEasing), RepeatMode.Restart),
        label = "alpha1",
    )
    val pulse2 by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isWaiting) 1.32f else 1.0f,
        animationSpec = infiniteRepeatable(tween(1_800, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse2",
    )
    val alpha2 by pulseTransition.animateFloat(
        initialValue = if (isWaiting) 0.35f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1_800, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "alpha2",
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isWaiting || call.isConnected) {
                Box(
                    Modifier
                        .size(124.dp)
                        .scale(pulse1)
                        .border(1.5.dp, ringColor.copy(alpha = alpha1), CircleShape)
                        .background(ringColor.copy(alpha = alpha1 * 0.25f), CircleShape),
                )
            }
            if (isWaiting) {
                Box(
                    Modifier
                        .size(124.dp)
                        .scale(pulse2)
                        .border(1.5.dp, ringColor.copy(alpha = alpha2), CircleShape),
                )
            }
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                ringColor.copy(alpha = 0.9f),
                                LeoColors.primary.copy(alpha = 0.8f),
                            ),
                        ),
                        shape = CircleShape,
                    )
                    .padding(5.dp),
                contentAlignment = Alignment.Center,
            ) {
                LeoAvatar(radius = 57.dp, name = call.targetUserName, imageUrl = call.targetAvatarUrl)
            }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            call.targetUserName,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CallTokens.glassSurface)
                .border(1.dp, CallTokens.glassBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            if (call.status == CallStatus.RINGING || call.isConnected) {
                CallWaveEqualizer(color = LeoColors.success)
                Spacer(Modifier.width(8.dp))
            } else {
                CallDot(CallTokens.ringing, size = 7.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                statusText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.88f),
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lock, null, tint = LeoColors.success.copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                CallStrings.encrypted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun CallControls(
    call: CallSession,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onToggleCamera: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
            .clip(CallTokens.dockShape)
            .background(CallTokens.cardSurface.copy(alpha = 0.85f))
            .border(1.dp, CallTokens.glassBorder, CallTokens.dockShape)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        CallControlButton(
            icon = if (call.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
            label = if (call.isMuted) CallStrings.unmute else CallStrings.mute,
            isActive = call.isMuted,
            onClick = onToggleMute,
        )
        CallControlButton(
            icon = if (call.isSpeakerOn) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeDown,
            label = CallStrings.speaker,
            isActive = call.isSpeakerOn,
            onClick = onToggleSpeaker,
        )
        if (call.isVideo) {
            CallControlButton(
                icon = if (call.isCameraOff) Icons.Rounded.VideocamOff else Icons.Rounded.Videocam,
                label = CallStrings.camera,
                isActive = call.isCameraOff,
                onClick = onToggleCamera,
            )
            CallControlButton(Icons.Rounded.FlipCameraIos, CallStrings.flip, onClick = onFlipCamera)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CallCircleAction(
                icon = Icons.Rounded.CallEnd,
                size = 58.dp,
                iconSize = 28.dp,
                background = LeoColors.destructive,
                brush = CallTokens.destructiveGradient,
                tint = Color.White,
                contentDescription = CallStrings.endCall,
                glow = CallTokens.endGlow,
                onClick = onEnd,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                CallStrings.endCall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}
