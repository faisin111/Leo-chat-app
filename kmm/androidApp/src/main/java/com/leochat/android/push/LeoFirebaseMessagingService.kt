package com.leochat.android.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.leochat.android.IncomingCallActivity
import com.leochat.core.network.push.PushTokenRegistry
import com.leochat.core.network.stomp.StompClient
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.presentation.CallController
import org.koin.core.context.GlobalContext

/** Receives high-priority FCM `INCOMING_CALL` messages and FCM token refreshes. */
class LeoFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val koin = GlobalContext.getOrNull() ?: return
        runCatching { koin.get<PushTokenRegistry>().syncInBackground(tokenOverride = token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val koin = GlobalContext.getOrNull() ?: return

        when (val event = PushCallPayload.parse(message.data, message.notification?.title)) {
            is PushCallEvent.Incoming -> {
                // Immediately establish/restore STOMP so full WebRTC SDP OFFER and ICE arrive cleanly.
                runCatching { koin.get<StompClient>().connect() }
                runCatching { koin.get<CallController>().handleSignal(event.signal) }
                if (!AppForegroundTracker.isMainForeground) {
                    IncomingCallNotificationHelper.showIncomingCall(applicationContext, event.signal)
                }
            }
            is PushCallEvent.Cancel -> {
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
