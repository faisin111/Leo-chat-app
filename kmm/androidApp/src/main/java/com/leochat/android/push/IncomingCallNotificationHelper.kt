package com.leochat.android.push

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.os.PowerManager
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leochat.android.IncomingCallActivity
import com.leochat.android.R
import com.leochat.core.common.constants.CallStrings
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

internal enum class IncomingCallPresentationMode {
    FLOATING_OVERLAY,
    NOTIFICATION_SHADE,
}

/** Pure presentation model backing the custom incoming call notification card and presentation routing. */
internal data class IncomingCallCardModel(
    val callerName: String,
    val callerInitials: String,
    val subtitle: String,
    val isVideo: Boolean,
    val avatarUrl: String?,
) {
    companion object {
        fun fromSignal(signal: CallSignal): IncomingCallCardModel {
            val callerName = signal.fromUsername?.trim()?.takeIf { it.isNotEmpty() } ?: CallStrings.defaultCallerName
            val isVideo = signal.callType == CallType.VIDEO
            return IncomingCallCardModel(
                callerName = callerName,
                callerInitials = initialsForCaller(callerName),
                subtitle = if (isVideo) CallStrings.incomingVideoCall else CallStrings.incomingVoiceCall,
                isVideo = isVideo,
                avatarUrl = signal.fromAvatarUrl?.trim()?.takeIf { it.isNotEmpty() },
            )
        }

        fun resolvePresentationMode(
            isInteractive: Boolean,
            isKeyguardLocked: Boolean,
            canDrawOverlays: Boolean,
        ): IncomingCallPresentationMode =
            if (isInteractive && !isKeyguardLocked && canDrawOverlays) {
                IncomingCallPresentationMode.FLOATING_OVERLAY
            } else {
                IncomingCallPresentationMode.NOTIFICATION_SHADE
            }

        fun initialsForCaller(name: String?): String {
            val trimmed = name?.trim().orEmpty()
            if (trimmed.isEmpty()) return "LC"
            val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
            return if (parts.size >= 2) {
                "${parts.first().first()}${parts.last().first()}".uppercase()
            } else {
                trimmed.take(2).uppercase()
            }
        }

        fun isHttpAvatarUrl(url: String?): Boolean {
            val normalized = url?.trim()?.lowercase() ?: return false
            return normalized.startsWith("https://") || normalized.startsWith("http://")
        }
    }
}

/**
 * Coordinates background/lock-screen incoming call UI:
 * - Unlocked with overlay permission: frameless floating OLED [IncomingCallBanner] card.
 * - Locked or without overlay permission: full-screen wake + seamless notification shade card.
 */
object IncomingCallNotificationHelper {
    private const val CHANNEL_ID = "leochat_incoming_calls"
    private const val CHANNEL_ID_SILENT = "leochat_incoming_calls_silent"
    private const val NOTIFICATION_ID = 7001
    private const val AVATAR_SIZE_PX = 160
    private const val AVATAR_FETCH_TIMEOUT_MS = 1_500

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var activeCallId: String? = null

    @Volatile
    private var avatarJob: Job? = null

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val highChannel = NotificationChannel(
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
            manager.createNotificationChannel(highChannel)
        }
        if (manager.getNotificationChannel(CHANNEL_ID_SILENT) == null) {
            val lowChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                CallStrings.ongoingCallChannelName,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = CallStrings.ongoingCallChannelDescription
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(lowChannel)
        }
    }

    fun showIncomingCall(context: Context, signal: CallSignal) {
        ensureChannel(context)
        val appContext = context.applicationContext
        val model = IncomingCallCardModel.fromSignal(signal)
        activeCallId = signal.callId
        avatarJob?.cancel()

        val mode = IncomingCallCardModel.resolvePresentationMode(
            isInteractive = isDeviceInteractive(appContext),
            isKeyguardLocked = isDeviceLocked(appContext),
            canDrawOverlays = IncomingCallOverlayController.canDrawOverlays(appContext),
        )
        val overlayShown = mode == IncomingCallPresentationMode.FLOATING_OVERLAY &&
            IncomingCallOverlayController.show(appContext, signal)

        if (!hasNotificationPermission(appContext)) return

        postNotification(
            context = appContext,
            signal = signal,
            model = model,
            avatarBitmap = null,
            useSilentShadeOnly = overlayShown,
            onlyAlertOnce = false,
        )

        val avatarUrl = model.avatarUrl
        if (IncomingCallCardModel.isHttpAvatarUrl(avatarUrl) && avatarUrl != null) {
            avatarJob = scope.launch {
                val bitmap = loadCircularAvatarBitmap(avatarUrl) ?: return@launch
                if (activeCallId != signal.callId) return@launch
                postNotification(
                    context = appContext,
                    signal = signal,
                    model = model,
                    avatarBitmap = bitmap,
                    useSilentShadeOnly = overlayShown,
                    onlyAlertOnce = true,
                )
            }
        }
    }

    fun dismiss(context: Context, animated: Boolean = true) {
        activeCallId = null
        avatarJob?.cancel()
        avatarJob = null
        IncomingCallOverlayController.dismiss(animated = animated)
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }

    private fun isDeviceInteractive(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return powerManager.isInteractive
    }

    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
        return keyguardManager.isKeyguardLocked
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun postNotification(
        context: Context,
        signal: CallSignal,
        model: IncomingCallCardModel,
        avatarBitmap: Bitmap?,
        useSilentShadeOnly: Boolean,
        onlyAlertOnce: Boolean,
    ) {
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            IncomingCallActivity.createIntent(context, signal, autoAccept = false),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + 1,
            IncomingCallActivity.createIntent(context, signal, autoAccept = true),
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

        val collapsedViews = buildRemoteViews(
            context = context,
            layoutRes = R.layout.notification_incoming_call_collapsed,
            isExpanded = false,
            model = model,
            avatarBitmap = avatarBitmap,
            acceptPendingIntent = acceptPendingIntent,
            declinePendingIntent = declinePendingIntent,
        )
        val expandedViews = buildRemoteViews(
            context = context,
            layoutRes = R.layout.notification_incoming_call_expanded,
            isExpanded = true,
            model = model,
            avatarBitmap = avatarBitmap,
            acceptPendingIntent = acceptPendingIntent,
            declinePendingIntent = declinePendingIntent,
        )

        val channelId = if (useSilentShadeOnly) CHANNEL_ID_SILENT else CHANNEL_ID
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_call)
            .setColor(if (model.isVideo) 0xFF8B5CF6.toInt() else 0xFF10B981.toInt())
            .setContentTitle(model.callerName)
            .setContentText(model.subtitle)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(if (useSilentShadeOnly) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(onlyAlertOnce || useSilentShadeOnly)
            .setTimeoutAfter(45_000L)
            .setContentIntent(fullScreenPendingIntent)
            .setCustomContentView(collapsedViews)
            .setCustomBigContentView(expandedViews)

        if (!useSilentShadeOnly) {
            builder
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setCustomHeadsUpContentView(expandedViews)
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun buildRemoteViews(
        context: Context,
        @LayoutRes layoutRes: Int,
        isExpanded: Boolean,
        model: IncomingCallCardModel,
        avatarBitmap: Bitmap?,
        acceptPendingIntent: PendingIntent,
        declinePendingIntent: PendingIntent,
    ): RemoteViews {
        val haloRes = if (model.isVideo) R.drawable.bg_call_avatar_halo_video else R.drawable.bg_call_avatar_halo_voice
        val badgeBgRes = if (model.isVideo) R.drawable.bg_call_badge_video else R.drawable.bg_call_badge_voice
        val callIconRes = if (model.isVideo) R.drawable.ic_call_videocam else R.drawable.ic_call_phone
        val waveRes = if (model.isVideo) R.drawable.ic_call_wave_video else R.drawable.ic_call_wave_voice
        val acceptBgRes = if (model.isVideo) R.drawable.bg_call_btn_accept_video else R.drawable.bg_call_btn_accept_voice

        return RemoteViews(context.packageName, layoutRes).apply {
            setImageViewResource(R.id.iv_avatar_halo, haloRes)
            setInt(R.id.badge_container, "setBackgroundResource", badgeBgRes)
            setImageViewResource(R.id.iv_badge_icon, callIconRes)
            setImageViewResource(R.id.iv_call_wave, waveRes)
            setInt(R.id.btn_accept, "setBackgroundResource", acceptBgRes)
            setImageViewResource(R.id.iv_accept_icon, callIconRes)

            setTextViewText(R.id.tv_caller_name, model.callerName)
            setTextViewText(R.id.tv_call_subtitle, model.subtitle)

            if (avatarBitmap != null) {
                setViewVisibility(R.id.iv_avatar, View.VISIBLE)
                setViewVisibility(R.id.tv_avatar_initials, View.GONE)
                setImageViewBitmap(R.id.iv_avatar, avatarBitmap)
            } else {
                setViewVisibility(R.id.iv_avatar, View.GONE)
                setViewVisibility(R.id.tv_avatar_initials, View.VISIBLE)
                setTextViewText(R.id.tv_avatar_initials, model.callerInitials)
            }

            if (isExpanded) {
                setTextViewText(R.id.tv_e2ee, CallStrings.encrypted)
                setTextViewText(R.id.tv_decline, CallStrings.decline)
                setTextViewText(R.id.tv_accept, CallStrings.accept)
            }

            setOnClickPendingIntent(R.id.btn_decline, declinePendingIntent)
            setOnClickPendingIntent(R.id.btn_accept, acceptPendingIntent)
        }
    }

    private fun loadCircularAvatarBitmap(imageUrl: String): Bitmap? = runCatching {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = AVATAR_FETCH_TIMEOUT_MS
            readTimeout = AVATAR_FETCH_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) return@runCatching null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val decoded = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size,
                BitmapFactory.Options().apply {
                    inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, AVATAR_SIZE_PX)
                },
            ) ?: return@runCatching null
            cropToCircle(decoded).also {
                if (it !== decoded) decoded.recycle()
            }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private fun calculateSampleSize(width: Int, height: Int, targetPx: Int): Int {
        var sample = 1
        val minDim = min(width, height)
        if (minDim <= 0) return sample
        while (minDim / (sample * 2) >= targetPx) {
            sample *= 2
        }
        return sample
    }

    private fun cropToCircle(source: Bitmap): Bitmap {
        val size = min(source.width, source.height).coerceAtLeast(1)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val left = (source.width - size) / 2
        val top = (source.height - size) / 2
        val srcRect = Rect(left, top, left + size, top + size)
        val dstRect = Rect(0, 0, size, size)
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, srcRect, dstRect, paint)
        return output
    }
}
