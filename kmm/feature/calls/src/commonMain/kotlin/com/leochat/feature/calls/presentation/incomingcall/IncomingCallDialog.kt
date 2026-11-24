package com.leochat.feature.calls.presentation.incomingcall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.call.IncomingCallBanner
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.calls.presentation.mapper.toIncomingCallUi
import com.leochat.feature.media.presentation.permission.MediaPermission
import com.leochat.feature.media.presentation.permission.rememberMediaPermissionRequester
import org.koin.compose.koinInject

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
                call = call.toIncomingCallUi(),
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
