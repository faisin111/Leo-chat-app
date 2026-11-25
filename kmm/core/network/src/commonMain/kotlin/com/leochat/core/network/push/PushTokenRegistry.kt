package com.leochat.core.network.push

import com.leochat.core.network.auth.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class PushPlatform { ANDROID, IOS }

/** Supplies the device push token (FCM on Android, no-op on platforms without push yet). */
interface PushTokenProvider {
    val platform: PushPlatform
    suspend fun currentToken(): String?
}

object NoOpPushTokenProvider : PushTokenProvider {
    override val platform: PushPlatform = PushPlatform.ANDROID
    override suspend fun currentToken(): String? = null
}

/**
 * Registers and unregisters the device push token against the authenticated backend endpoints:
 * - `POST /api/v1/devices/push-token` (`{ "token": "...", "platform": "ANDROID" }`)
 * - `DELETE /api/v1/devices/push-token`
 */
class PushTokenRegistry(
    private val http: HttpClient,
    private val tokens: TokenStore,
    private val scope: CoroutineScope,
    private val tokenProvider: PushTokenProvider = NoOpPushTokenProvider,
) {
    private val mutex = Mutex()
    private var lastSynced: Pair<String, String>? = null // accessToken to pushToken

    /** Syncs the push token asynchronously without blocking caller flows. */
    fun syncInBackground(tokenOverride: String? = null) {
        scope.launch { runCatching { syncToken(tokenOverride) } }
    }

    /** Registers the current device push token if an authenticated session is active. */
    suspend fun syncToken(tokenOverride: String? = null): Boolean = mutex.withLock {
        val accessToken = tokens.accessToken() ?: return@withLock false
        val pushToken = tokenOverride?.trim()?.takeIf { it.isNotEmpty() }
            ?: runCatching { tokenProvider.currentToken() }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            ?: return@withLock false

        if (lastSynced == (accessToken to pushToken)) return@withLock true

        val response = runCatching {
            http.post("devices/push-token") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("token", pushToken)
                        put("platform", tokenProvider.platform.name)
                    },
                )
            }
        }.getOrNull() ?: return@withLock false

        if (response.status.isSuccess()) {
            lastSynced = accessToken to pushToken
            true
        } else {
            false
        }
    }

    /** Best-effort unregistration before local session tokens are cleared on sign-out. */
    suspend fun unregisterCurrentToken() = mutex.withLock {
        val hadToken = tokens.accessToken() != null
        val cachedPushToken = lastSynced?.second
        lastSynced = null
        if (!hadToken) return@withLock

        val pushToken = cachedPushToken
            ?: runCatching { tokenProvider.currentToken() }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }

        runCatching {
            http.delete("devices/push-token") {
                if (pushToken != null) {
                    parameter("token", pushToken)
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject { put("token", pushToken) })
                }
            }
        }
    }
}
