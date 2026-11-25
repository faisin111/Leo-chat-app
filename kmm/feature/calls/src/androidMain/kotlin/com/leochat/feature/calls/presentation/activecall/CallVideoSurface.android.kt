package com.leochat.feature.calls.presentation.activecall

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.feature.calls.data.device.AndroidCallMediaEngine
import com.leochat.feature.calls.data.device.WebRtcFactoryHolder
import com.leochat.feature.calls.presentation.CallController
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

@Composable
actual fun CallVideoSurface(
    controller: CallController,
    isLocal: Boolean,
    isMirror: Boolean,
    modifier: Modifier,
) {
    val engine by controller.mediaEngineState.collectAsStateWithLifecycle()
    val videoEngine = engine as? AndroidCallMediaEngine
    val eglContext = WebRtcFactoryHolder.eglContext

    if (videoEngine == null || eglContext == null) {
        Box(modifier)
        return
    }

    val context = LocalContext.current
    val renderer = remember(isLocal) {
        SurfaceViewRenderer(context).apply {
            runCatching {
                init(eglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setEnableHardwareScaler(true)
                // Place local PiP overlay above remote SurfaceView to prevent SurfaceView Z-fighting.
                setZOrderMediaOverlay(isLocal)
            }
        }
    }

    DisposableEffect(videoEngine, renderer, isLocal) {
        if (isLocal) {
            videoEngine.attachLocalSink(renderer)
        } else {
            videoEngine.attachRemoteSink(renderer)
        }
        onDispose {
            if (isLocal) {
                videoEngine.detachLocalSink(renderer)
            } else {
                videoEngine.detachRemoteSink(renderer)
            }
            runCatching { renderer.clearImage() }
        }
    }

    DisposableEffect(renderer) {
        onDispose {
            runCatching { renderer.release() }
        }
    }

    AndroidView(
        factory = { renderer },
        modifier = modifier,
        update = { view ->
            runCatching { view.setMirror(isMirror) }
        },
    )
}
