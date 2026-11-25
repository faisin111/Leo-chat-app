package com.leochat.android

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.android.push.IncomingCallNotificationHelper
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.call.IncomingCallBanner
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.core.designsystem.component.feedback.LeoSnackBarController
import com.leochat.core.designsystem.component.feedback.LeoSnackBarHost
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTheme
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallType
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.calls.presentation.mapper.toIncomingCallUi
import com.leochat.feature.media.presentation.permission.MediaPermission
import com.leochat.feature.media.presentation.permission.rememberMediaPermissionRequester
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

/**
 * Full-screen / lock-screen incoming call popup launched via high-priority FCM notification
 * when the app is backgrounded. Renders the exact [IncomingCallBanner] OLED glassmorphic popup.
 */
class IncomingCallActivity : ComponentActivity() {
    private val callController by inject<CallController>()
    private var autoAcceptRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLockScreenPresentation()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        activeInstance = WeakReference(this)
        handleCallIntent(intent)

        val fallbackUi = IncomingCallUi(
            callerName = intent.getStringExtra(EXTRA_CALLER_NAME).orEmpty().ifBlank { CallStrings.defaultCallerName },
            callerAvatarUrl = intent.getStringExtra(EXTRA_CALLER_AVATAR),
            isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false),
        )

        setContent {
            val state by callController.state.collectAsStateWithLifecycle()
            val permissions = rememberMediaPermissionRequester()
            val scope = rememberCoroutineScope()
            val snackBar = remember { LeoSnackBarController(SnackbarHostState(), scope) }

            val incomingUi = state.incomingCall?.toIncomingCallUi() ?: fallbackUi
            val isVideo = state.incomingCall?.isVideo ?: fallbackUi.isVideo

            var sawIncoming by remember { mutableStateOf(state.incomingCall != null) }
            LaunchedEffect(state.incomingCall, state.activeCall) {
                if (state.incomingCall != null) sawIncoming = true
                if (state.activeCall != null) {
                    openMainCallUiAndFinish()
                } else if (sawIncoming && state.incomingCall == null) {
                    IncomingCallNotificationHelper.dismiss(this@IncomingCallActivity)
                    finish()
                }
            }

            fun acceptWithPermissions() {
                if (isVideo) {
                    permissions.request(setOf(MediaPermission.Microphone, MediaPermission.Camera)) { bothGranted ->
                        if (bothGranted) {
                            IncomingCallNotificationHelper.dismiss(this@IncomingCallActivity)
                            callController.acceptIncomingCall()
                            openMainCallUiAndFinish()
                        } else {
                            permissions.request(setOf(MediaPermission.Microphone)) { micGranted ->
                                if (micGranted) {
                                    IncomingCallNotificationHelper.dismiss(this@IncomingCallActivity)
                                    callController.acceptIncomingCall()
                                    openMainCallUiAndFinish()
                                } else {
                                    snackBar.showInfo(CallStrings.microphonePermissionRequired)
                                }
                            }
                        }
                    }
                } else {
                    permissions.request(setOf(MediaPermission.Microphone)) { granted ->
                        if (granted) {
                            IncomingCallNotificationHelper.dismiss(this@IncomingCallActivity)
                            callController.acceptIncomingCall()
                            openMainCallUiAndFinish()
                        } else {
                            snackBar.showInfo(CallStrings.microphonePermissionRequired)
                        }
                    }
                }
            }

            LaunchedEffect(autoAcceptRequested) {
                if (autoAcceptRequested) {
                    autoAcceptRequested = false
                    acceptWithPermissions()
                }
            }

            val auraColor = if (incomingUi.isVideo) CallTokens.videoAura else LeoColors.success
            LeoTheme(mode = LeoThemeMode.Dark) {
                CompositionLocalProvider(LocalLeoSnackBar provides snackBar) {
                    Scaffold(
                        containerColor = CallTokens.background,
                        snackbarHost = { LeoSnackBarHost(snackBar.hostState) },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            auraColor.copy(alpha = 0.24f),
                                            LeoColors.primary.copy(alpha = 0.08f),
                                            CallTokens.background,
                                        ),
                                    ),
                                )
                                .safeDrawingPadding()
                                .padding(innerPadding)
                                .padding(vertical = 24.dp),
                        ) {
                            IncomingCallBanner(
                                call = incomingUi,
                                onDecline = {
                                    IncomingCallNotificationHelper.dismiss(this@IncomingCallActivity)
                                    callController.rejectIncomingCall()
                                    finish()
                                },
                                onAccept = { acceptWithPermissions() },
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCallIntent(intent)
    }

    override fun onDestroy() {
        if (activeInstance?.get() === this) activeInstance = null
        super.onDestroy()
    }

    private fun handleCallIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_AUTO_ACCEPT, false) == true) {
            autoAcceptRequested = true
        }
    }

    private fun openMainCallUiAndFinish() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_ACTIVE_CALL, true)
            },
        )
        finish()
    }

    @Suppress("DEPRECATION")
    private fun enableLockScreenPresentation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }

    companion object {
        private const val EXTRA_CALL_ID = "extra_call_id"
        private const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
        private const val EXTRA_CALLER_ID = "extra_caller_id"
        private const val EXTRA_CALLER_NAME = "extra_caller_name"
        private const val EXTRA_CALLER_AVATAR = "extra_caller_avatar"
        private const val EXTRA_IS_VIDEO = "extra_is_video"
        private const val EXTRA_AUTO_ACCEPT = "extra_auto_accept"

        private var activeInstance: WeakReference<IncomingCallActivity>? = null

        fun finishIfActive() {
            activeInstance?.get()?.runOnUiThread {
                activeInstance?.get()?.finish()
            }
        }

        fun createIntent(context: Context, signal: CallSignal, autoAccept: Boolean = false): Intent =
            Intent(context, IncomingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_CALL_ID, signal.callId)
                putExtra(EXTRA_CONVERSATION_ID, signal.conversationId)
                putExtra(EXTRA_CALLER_ID, signal.fromUserId)
                putExtra(EXTRA_CALLER_NAME, signal.fromUsername)
                putExtra(EXTRA_CALLER_AVATAR, signal.fromAvatarUrl)
                putExtra(EXTRA_IS_VIDEO, signal.callType == CallType.VIDEO)
                putExtra(EXTRA_AUTO_ACCEPT, autoAccept)
            }
    }
}
