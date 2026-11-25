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
import com.leochat.android.MainActivity
import com.leochat.android.R
import com.leochat.core.common.constants.AppStrings

/** Posts and dismisses message & reaction notifications grouped per conversation. */
object ChatPushNotificationHelper {
    private const val CHANNEL_ID = "leochat_messages"
    private const val GROUP_MESSAGES = "leochat_group_messages"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            AppStrings.reactionChannelName,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = AppStrings.reactionChannelDescription
            enableVibration(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    fun showMessage(context: Context, event: PushEvent.NewMessage) {
        postConversationNotification(
            context = context,
            conversationId = event.conversationId,
            messageId = event.messageId,
            title = event.displayTitle,
            body = event.displayBody,
        )
    }

    fun showReaction(context: Context, event: PushEvent.MessageReaction) {
        postConversationNotification(
            context = context,
            conversationId = event.conversationId,
            messageId = event.messageId,
            title = event.displayTitle,
            body = event.displayBody,
        )
    }

    fun dismissConversation(context: Context, conversationId: String) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(notificationIdFor(conversationId))
        }
    }

    private fun postConversationNotification(
        context: Context,
        conversationId: String,
        messageId: String?,
        title: String,
        body: String,
    ) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notifId = notificationIdFor(conversationId)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_CONVERSATION_ID, conversationId)
            putExtra(MainActivity.EXTRA_OPEN_CONVERSATION_TITLE, title)
            messageId?.takeIf { it.isNotBlank() }?.let { putExtra(MainActivity.EXTRA_OPEN_MESSAGE_ID, it) }
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setGroup(GROUP_MESSAGES)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        }
    }

    private fun notificationIdFor(conversationId: String): Int =
        "msg:$conversationId".hashCode() and 0x7fffffff
}
