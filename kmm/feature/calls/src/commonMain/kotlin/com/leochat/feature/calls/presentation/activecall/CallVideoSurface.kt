package com.leochat.feature.calls.presentation.activecall

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.leochat.feature.calls.presentation.CallController

/** Renders live WebRTC local or remote video frames when supported by the platform engine. */
@Composable
expect fun CallVideoSurface(
    controller: CallController,
    isLocal: Boolean,
    isMirror: Boolean,
    modifier: Modifier = Modifier,
)
