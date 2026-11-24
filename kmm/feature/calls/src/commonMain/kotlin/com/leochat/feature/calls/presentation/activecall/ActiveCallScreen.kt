package com.leochat.feature.calls.presentation.activecall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.feature.calls.presentation.CallController
import org.koin.compose.koinInject

/**
 * Full-screen call UI. Mirrors `ActiveCallScreen`: calls [onEnded] (pop the route)
 * once the call is gone, whether ended locally or by the other side.
 */
@Composable
fun ActiveCallScreen(onEnded: () -> Unit, controller: CallController = koinInject()) {
    val state by controller.state.collectAsStateWithLifecycle()
    val ended = state.activeCall == null
    LaunchedEffect(ended) { if (ended) onEnded() }

    ActiveCallContent(
        state = state,
        onToggleMute = controller::toggleMute,
        onToggleSpeaker = controller::toggleSpeaker,
        onFlipCamera = controller::flipCamera,
        onEnd = {
            controller.endCall()
            onEnded()
        },
    )
}
