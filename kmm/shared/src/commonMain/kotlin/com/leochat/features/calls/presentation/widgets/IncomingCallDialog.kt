package com.leochat.features.calls.presentation.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.CallStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.features.calls.domain.entities.CallSession
import com.leochat.features.calls.presentation.controllers.CallController
import com.leochat.features.media.presentation.permissions.MediaPermission
import com.leochat.features.media.presentation.permissions.rememberMediaPermissionRequester
import org.koin.compose.koinInject

private val BannerShape = RoundedCornerShape(24.dp)
private val BannerShadow = Color(0x44000000)

/**
 * Incoming call banner overlay. Mirrors `IncomingCallBanner`: place it at the
 * top of the app root; it renders nothing while no call is ringing. Accepting
 * asks for the microphone, answers, then calls [onAccepted] (open the call route).
 */
@Composable
fun IncomingCallDialog(
    onAccepted: () -> Unit,
    modifier: Modifier = Modifier,
    onPermissionDenied: () -> Unit = {},
    controller: CallController = koinInject(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val permissions = rememberMediaPermissionRequester()
    // Keep the last call so the exit animation still has content.
    var shown by remember { mutableStateOf<CallSession?>(null) }
    state.incomingCall?.let { shown = it }

    AnimatedVisibility(
        visible = state.incomingCall != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        shown?.let { call ->
            IncomingCallBanner(
                call = call,
                onDecline = controller::rejectIncomingCall,
                onAccept = {
                    permissions.request(setOf(MediaPermission.Microphone)) { granted ->
                        if (granted) {
                            controller.acceptIncomingCall()
                            onAccepted()
                        } else {
                            onPermissionDenied()
                        }
                    }
                },
            )
        }
    }
}

@Composable
internal fun IncomingCallBanner(call: CallSession, onDecline: () -> Unit, onAccept: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(20.dp, BannerShape, ambientColor = BannerShadow, spotColor = BannerShadow)
            .background(CallColors.background, BannerShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LeoAvatar(radius = 24.dp, name = call.targetUserName, imageUrl = call.targetAvatarUrl)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                call.targetUserName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.PhoneInTalk,
                    null,
                    tint = LeoColors.primary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (call.isVideo) CallStrings.incomingVideoCall else CallStrings.incomingVoiceCall,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
        CircleAction(Icons.Rounded.CallEnd, 42.dp, 20.dp, LeoColors.destructive, Color.White, CallStrings.decline, onClick = onDecline)
        Spacer(Modifier.width(10.dp))
        CircleAction(Icons.Rounded.Call, 42.dp, 20.dp, LeoColors.success, Color.White, CallStrings.accept, onClick = onAccept)
    }
}
