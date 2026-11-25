package com.leochat.android.push

import com.google.firebase.messaging.FirebaseMessaging
import com.leochat.core.network.push.PushPlatform
import com.leochat.core.network.push.PushTokenProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Supplies the Android FCM registration token for `POST /api/v1/devices/push-token`. */
class AndroidPushTokenProvider : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.ANDROID

    override suspend fun currentToken(): String? = suspendCancellableCoroutine { cont ->
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (cont.isActive) cont.resume(token?.takeIf { it.isNotBlank() })
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
                .addOnCanceledListener {
                    if (cont.isActive) cont.resume(null)
                }
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    }
}
