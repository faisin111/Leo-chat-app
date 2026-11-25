package com.leochat.android.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.leochat.android.IncomingCallActivity
import com.leochat.feature.calls.presentation.CallController
import org.koin.core.context.GlobalContext

/** Handles Decline taps directly from the background incoming call notification. */
class CallNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DECLINE_CALL) return
        IncomingCallNotificationHelper.dismiss(context)
        runCatching {
            GlobalContext.getOrNull()?.get<CallController>()?.rejectIncomingCall()
        }
        IncomingCallActivity.finishIfActive()
    }

    companion object {
        const val ACTION_DECLINE_CALL = "com.leochat.android.ACTION_DECLINE_CALL"
    }
}
