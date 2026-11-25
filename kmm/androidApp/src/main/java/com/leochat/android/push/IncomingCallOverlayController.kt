package com.leochat.android.push

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.leochat.android.IncomingCallActivity
import com.leochat.core.designsystem.component.call.IncomingCallBanner
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTheme
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.presentation.CallController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Displays a frameless floating OLED [IncomingCallBanner] overlay over other apps when the user
 * is unlocked and `SYSTEM_ALERT_WINDOW` permission is granted, using GPU-accelerated spring entrance
 * and exit transitions without SurfaceFlinger relayout jank.
 */
object IncomingCallOverlayController {
    private const val TIMEOUT_MS = 45_000L
    private const val EXIT_ANIM_MS = 220L

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentView: ComposeView? = null
    private var currentOwners: OverlayLifecycleOwner? = null
    private var currentWindowManager: WindowManager? = null
    private var currentGeneration: Long = 0L
    private var visibleState by mutableStateOf(false)
    private var timeoutRunnable: Runnable? = null
    private var exitCleanupRunnable: Runnable? = null

    fun canDrawOverlays(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)

    /** Shows the floating [IncomingCallBanner] on the main thread; returns true if attached cleanly. */
    fun show(context: Context, signal: CallSignal): Boolean {
        if (!canDrawOverlays(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appContext = context.applicationContext

        if (Looper.myLooper() == Looper.getMainLooper()) {
            return showOnMainThread(appContext, signal)
        }

        val latch = CountDownLatch(1)
        var attached = false
        mainHandler.post {
            attached = showOnMainThread(appContext, signal)
            latch.countDown()
        }
        runCatching { latch.await(400, TimeUnit.MILLISECONDS) }
        return attached
    }

    fun dismiss(animated: Boolean = true) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            dismissOnMainThread(animated)
        } else {
            mainHandler.post { dismissOnMainThread(animated) }
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun showOnMainThread(appContext: Context, signal: CallSignal): Boolean {
        removeImmediatelyOnMainThread()
        val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return false
        val owners = OverlayLifecycleOwner()
        owners.attachAndResume()

        val generation = ++currentGeneration
        visibleState = true

        val model = IncomingCallCardModel.fromSignal(signal)
        val uiModel = IncomingCallUi(
            callerName = model.callerName,
            callerAvatarUrl = model.avatarUrl,
            isVideo = model.isVideo,
        )
        val auraColor = if (model.isVideo) CallTokens.videoAura else LeoColors.success

        val composeView = ComposeView(appContext).apply {
            setViewTreeLifecycleOwner(owners)
            setViewTreeViewModelStoreOwner(owners)
            setViewTreeSavedStateRegistryOwner(owners)
            setContent {
                val clickInteraction = remember { MutableInteractionSource() }
                val progress = remember(generation) { Animatable(0f) }
                val offsetPx = remember(generation) { Animatable(-140f) }
                val isVisible = visibleState

                LaunchedEffect(isVisible, generation) {
                    if (isVisible) {
                        coroutineScope {
                            launch {
                                progress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                )
                            }
                            launch {
                                offsetPx.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                )
                            }
                        }
                    } else {
                        coroutineScope {
                            launch {
                                progress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing),
                                )
                            }
                            launch {
                                offsetPx.animateTo(
                                    targetValue = -110f,
                                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
                                )
                            }
                        }
                    }
                }

                val p = progress.value.coerceIn(0f, 1f)
                val scale = 0.92f + (0.08f * p)

                LeoTheme(mode = LeoThemeMode.Dark) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        auraColor.copy(alpha = 0.16f * p),
                                        Color.Transparent,
                                    ),
                                ),
                            )
                            .padding(bottom = 14.dp)
                            .graphicsLayer {
                                alpha = p
                                translationY = offsetPx.value
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable(
                                interactionSource = clickInteraction,
                                indication = null,
                                onClick = {
                                    IncomingCallNotificationHelper.dismiss(appContext, animated = false)
                                    appContext.startActivity(
                                        IncomingCallActivity.createIntent(appContext, signal, autoAccept = false),
                                    )
                                },
                            ),
                    ) {
                        IncomingCallBanner(
                            call = uiModel,
                            onDecline = {
                                IncomingCallNotificationHelper.dismiss(appContext, animated = true)
                                runCatching { GlobalContext.getOrNull()?.get<CallController>()?.rejectIncomingCall() }
                            },
                            onAccept = {
                                IncomingCallNotificationHelper.dismiss(appContext, animated = false)
                                appContext.startActivity(
                                    IncomingCallActivity.createIntent(appContext, signal, autoAccept = true),
                                )
                            },
                        )
                    }
                }
            }
        }

        val statusBarResId = appContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        val topOffset = if (statusBarResId > 0) appContext.resources.getDimensionPixelSize(statusBarResId) else 0

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = topOffset
        }

        return runCatching {
            windowManager.addView(composeView, layoutParams)
            currentView = composeView
            currentOwners = owners
            currentWindowManager = windowManager
            val timeout = Runnable { IncomingCallNotificationHelper.dismiss(appContext, animated = true) }
            timeoutRunnable = timeout
            mainHandler.postDelayed(timeout, TIMEOUT_MS)
            true
        }.getOrElse {
            owners.destroy()
            false
        }
    }

    private fun dismissOnMainThread(animated: Boolean) {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
        if (currentView == null) return

        if (!animated) {
            removeImmediatelyOnMainThread()
            return
        }

        val targetGeneration = currentGeneration
        visibleState = false
        exitCleanupRunnable?.let { mainHandler.removeCallbacks(it) }
        val cleanup = Runnable {
            if (currentGeneration == targetGeneration) {
                removeImmediatelyOnMainThread()
            }
        }
        exitCleanupRunnable = cleanup
        mainHandler.postDelayed(cleanup, EXIT_ANIM_MS)
    }

    private fun removeImmediatelyOnMainThread() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
        exitCleanupRunnable?.let { mainHandler.removeCallbacks(it) }
        exitCleanupRunnable = null

        val view = currentView
        val wm = currentWindowManager
        val owners = currentOwners
        currentView = null
        currentWindowManager = null
        currentOwners = null
        if (view != null && wm != null) {
            runCatching { wm.removeViewImmediate(view) }
        }
        owners?.destroy()
    }

    private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val store = ViewModelStore()
        private val savedStateController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val viewModelStore: ViewModelStore get() = store
        override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

        fun attachAndResume() {
            savedStateController.performAttach()
            savedStateController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun destroy() {
            runCatching {
                if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                }
                if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                }
                if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                }
            }
            store.clear()
        }
    }
}
