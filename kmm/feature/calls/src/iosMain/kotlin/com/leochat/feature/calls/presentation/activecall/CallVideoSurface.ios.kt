package com.leochat.feature.calls.presentation.activecall

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.leochat.feature.calls.presentation.CallController

@Composable
actual fun CallVideoSurface(
    controller: CallController,
    isLocal: Boolean,
    isMirror: Boolean,
    modifier: Modifier,
) {
    Box(modifier)
}
