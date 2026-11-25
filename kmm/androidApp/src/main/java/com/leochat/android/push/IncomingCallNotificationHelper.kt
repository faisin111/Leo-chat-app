package com.leochat.android.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leochat.android.IncomingCallActivity
import com.leochat.android.R
import com.leochat.core.common.constants.CallStrings
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallType

/** Posts and dismisses the high-priority background/lock-screen incoming call notification. */
object IncomingCallNotificationHelper {
    private const val CHANNEL_ID = "leochat_incoming_calls"
    private const val NOTIFICATION_ID = 7001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CallStrings.incomingCallChannelName,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CallStrings.incomingCallChannelDescription
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            enableVibration(true)
            // CallController/CallSoundPlayer handles ringtone lifecycle directly.
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    fun showIncomingCall(context: Context, signal: CallSignal) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val callerName = signal.fromUsername?.takeIf { it.isNotBlank() } ?: CallStrings.defaultCallerName
        val isVideo = signal.callType == CallType.VIDEO
        val subtitle = if (isVideo) CallStrings.incomingVideoCall else CallStrings.incomingVoiceCall

        val fullScreenIntent = IncomingCallActivity.createIntent(context, signal, autoAccept = false)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val acceptIntent = IncomingCallActivity.createIntent(context, signal, autoAccept = true)
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + 1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val declineIntent = Intent(context, CallNotificationActionReceiver::class.java).apply {
            action = CallNotificationActionReceiver.ACTION_DECLINE_CALL
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID + 2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(callerName)
            .setContentText(subtitle)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setTimeoutAfter(45_000L)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, CallStrings.decline, declinePendingIntent)
            .addAction(0, CallStrings.accept, acceptPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun dismiss(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }
}
