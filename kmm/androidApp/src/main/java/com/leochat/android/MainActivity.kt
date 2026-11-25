package com.leochat.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.leochat.android.push.ChatPushNotificationHelper
import com.leochat.android.push.IncomingCallNotificationHelper
import com.leochat.android.push.PushEvent
import com.leochat.android.push.PushNotificationPayload
import com.leochat.app.LeoChatApp
import com.leochat.core.navigation.AppDeepLink
import com.leochat.core.navigation.DeepLinkBus
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.network.push.PushTokenRegistry
import com.leochat.core.network.stomp.StompClient
import com.leochat.feature.calls.presentation.CallController
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val pushTokenRegistry by inject<PushTokenRegistry>()
    private val tokenStore by inject<TokenStore>()
    private val callController by inject<CallController>()
    private val stompClient by inject<StompClient>()
    private var requestedNotificationPermission = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Transparent bars that automatically adapt icons for light and dark themes.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent { LeoChatApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        pushTokenRegistry.syncInBackground()
        requestPostNotificationsIfAuthenticated()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(EXTRA_OPEN_ACTIVE_CALL, false)) {
            IncomingCallNotificationHelper.dismiss(this)
            intent.removeExtra(EXTRA_OPEN_ACTIVE_CALL)
        }
        val conversationId = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_ID)?.takeIf { it.isNotBlank() }
        if (conversationId != null) {
            val messageId = intent.getStringExtra(EXTRA_OPEN_MESSAGE_ID)?.takeIf { it.isNotBlank() }
            val title = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_TITLE)?.takeIf { it.isNotBlank() }
            ChatPushNotificationHelper.dismissConversation(this, conversationId)
            DeepLinkBus.emit(
                AppDeepLink.OpenConversation(
                    conversationId = conversationId,
                    messageId = messageId,
                    title = title,
                ),
            )
            intent.removeExtra(EXTRA_OPEN_CONVERSATION_ID)
            intent.removeExtra(EXTRA_OPEN_MESSAGE_ID)
            intent.removeExtra(EXTRA_OPEN_CONVERSATION_TITLE)
            return
        }

        // Handle OS system-tray FCM notification taps where Android places the backend `data` map in intent extras.
        val bundle = intent.extras ?: return
        val dataMap = buildMap<String, String> {
            for (key in bundle.keySet()) {
                @Suppress("DEPRECATION")
                val v = bundle.get(key)
                if (v is String) put(key, v)
            }
        }
        when (val pushEvent = PushNotificationPayload.parse(dataMap)) {
            is PushEvent.NewMessage -> {
                ChatPushNotificationHelper.dismissConversation(this, pushEvent.conversationId)
                DeepLinkBus.emit(
                    AppDeepLink.OpenConversation(
                        conversationId = pushEvent.conversationId,
                        messageId = pushEvent.messageId.takeIf { it.isNotBlank() },
                        title = pushEvent.displayTitle,
                    ),
                )
                intent.removeExtra("type")
                intent.removeExtra("conversationId")
            }
            is PushEvent.MessageReaction -> {
                ChatPushNotificationHelper.dismissConversation(this, pushEvent.conversationId)
                DeepLinkBus.emit(
                    AppDeepLink.OpenConversation(
                        conversationId = pushEvent.conversationId,
                        messageId = pushEvent.messageId.takeIf { it.isNotBlank() },
                        title = pushEvent.displayTitle,
                    ),
                )
                intent.removeExtra("type")
                intent.removeExtra("conversationId")
            }
            is PushEvent.IncomingCall -> {
                IncomingCallNotificationHelper.dismiss(this)
                callController.handleSignal(pushEvent.signal)
                stompClient.connect()
                intent.removeExtra("type")
                intent.removeExtra("callId")
            }
            else -> Unit
        }
    }

    private fun requestPostNotificationsIfAuthenticated() {
        if (requestedNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        lifecycleScope.launch {
            if (tokenStore.accessToken() != null && !requestedNotificationPermission) {
                requestedNotificationPermission = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ACTIVE_CALL = "extra_open_active_call"
        const val EXTRA_OPEN_CONVERSATION_ID = "extra_open_conversation_id"
        const val EXTRA_OPEN_MESSAGE_ID = "extra_open_message_id"
        const val EXTRA_OPEN_CONVERSATION_TITLE = "extra_open_conversation_title"
    }
}
