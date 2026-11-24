package com.leochat.features.calls.presentation.pages

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.CallStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.features.calls.domain.entities.CallSession
import com.leochat.features.calls.domain.entities.CallStatus
import com.leochat.features.calls.presentation.controllers.CallController
import com.leochat.features.calls.presentation.widgets.CallColors
import com.leochat.features.calls.presentation.widgets.CallControlButton
import com.leochat.features.calls.presentation.widgets.CallStatusChip
import com.leochat.features.calls.presentation.widgets.CircleAction
import com.leochat.features.calls.presentation.widgets.ControlsArrangement
import com.leochat.features.calls.presentation.widgets.Dot
import com.leochat.features.calls.presentation.widgets.formatCallDuration
import org.koin.compose.koinInject

/**
 * Full-screen call UI. Mirrors `ActiveCallPage`: calls [onEnded] (pop the route)
 * once the call is gone, whether ended locally or by the other side.
 */
@Composable
fun ActiveCallPage(onEnded: () -> Unit, controller: CallController = koinInject()) {
    val state by controller.state.collectAsStateWithLifecycle()
    val call = state.activeCall
    val ended = call == null
    LaunchedEffect(ended) { if (ended) onEnded() }

    Box(Modifier.fillMaxSize().background(CallColors.background)) {
        if (call != null) {
            ActiveCallContent(
                call = call,
                onToggleMute = controller::toggleMute,
                onToggleSpeaker = controller::toggleSpeaker,
                onFlipCamera = controller::flipCamera,
                onEnd = {
                    controller.endCall()
                    onEnded()
                },
            )
        }
    }
}

@Composable
internal fun ActiveCallContent(
    call: CallSession,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onFlipCamera: () -> Unit,
    onEnd: () -> Unit,
) {
    val statusText = when {
        call.isConnected -> CallStrings.connected
        call.status == CallStatus.OUTGOING -> CallStrings.calling
        else -> CallStrings.connecting
    }
    Box(Modifier.fillMaxSize()) {
        if (call.isVideo) VideoPlaceholder(call.isCameraFront)
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            CallTopBar(call, Modifier.align(Alignment.TopCenter).padding(16.dp))
            CallerInfo(call, statusText, Modifier.align(Alignment.Center))
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
    Box(Modifier.fillMaxSize().background(CallColors.videoSurface), contentAlignment = Alignment.Center) {
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
            background = CallColors.chip,
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
            background = if (call.isConnected) LeoColors.success.copy(alpha = 0.25f) else CallColors.chip,
            leading = { Dot(if (call.isConnected) LeoColors.success else CallColors.ringing) },
            text = when {
                call.isConnected -> formatCallDuration(call.connectedSeconds)
                call.status == CallStatus.OUTGOING -> CallStrings.calling
                else -> CallStrings.connecting
            },
        )
    }
}

@Composable
private fun CallerInfo(call: CallSession, statusText: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
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
    Row(modifier.fillMaxWidth(), horizontalArrangement = ControlsArrangement, verticalAlignment = Alignment.Top) {
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
        CircleAction(
            icon = Icons.Rounded.CallEnd,
            size = 64.dp,
            iconSize = 30.dp,
            background = LeoColors.destructive,
            tint = Color.White,
            contentDescription = CallStrings.endCall,
            glow = CallColors.endGlow,
            onClick = onEnd,
        )
    }
}

