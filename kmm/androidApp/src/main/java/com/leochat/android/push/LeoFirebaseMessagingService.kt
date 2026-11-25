package com.leochat.android.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.leochat.android.IncomingCallActivity
import com.leochat.core.navigation.DeepLinkBus
import com.leochat.core.network.push.PushTokenRegistry
import com.leochat.core.network.stomp.StompClient
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.presentation.CallController
import org.koin.core.context.GlobalContext

/**
 * Receives high-priority FCM notifications across all supported backend types:
 * - `NEW_MESSAGE` (`TEXT`, `IMAGE`, `VOICE`, `FILE`, `CALL`, `SYSTEM`)
 * - `MESSAGE_REACTION`
 * - `INCOMING_CALL` (and call cancel events)
 */
class LeoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val koin = GlobalContext.getOrNull() ?: return
        runCatching { koin.get<PushTokenRegistry>().syncInBackground(tokenOverride = token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val koin = GlobalContext.getOrNull() ?: return

        when (
            val event = PushNotificationPayload.parse(
                data = message.data,
                notificationTitle = message.notification?.title,
                notificationBody = message.notification?.body,
            )
        ) {
            is PushEvent.NewMessage -> {
                val viewingSameChat = AppForegroundTracker.isMainForeground &&
                    DeepLinkBus.activeConversationId.value == event.conversationId
                if (!viewingSameChat) {
                    ChatPushNotificationHelper.showMessage(applicationContext, event)
                }
            }
            is PushEvent.MessageReaction -> {
                val viewingSameChat = AppForegroundTracker.isMainForeground &&
                    DeepLinkBus.activeConversationId.value == event.conversationId
                if (!viewingSameChat) {
                    ChatPushNotificationHelper.showReaction(applicationContext, event)
                }
            }
            is PushEvent.IncomingCall -> {
                // Initialize CallController first so /user/queue/calls is subscribed before STOMP connects & triggers drain.
                runCatching { koin.get<CallController>().handleSignal(event.signal) }
                runCatching { koin.get<StompClient>().connect() }
                if (!AppForegroundTracker.isMainForeground) {
                    IncomingCallNotificationHelper.showIncomingCall(applicationContext, event.signal)
                }
            }
            is PushEvent.CancelCall -> {
                IncomingCallNotificationHelper.dismiss(applicationContext)
                IncomingCallActivity.finishIfActive()
                runCatching {
                    koin.get<CallController>().handleSignal(
                        CallSignal(
                            callId = event.callId,
                            conversationId = event.conversationId,
                            action = CallSignalAction.End,
                        ),
                    )
                }
            }
            null -> Unit
        }
    }
}
