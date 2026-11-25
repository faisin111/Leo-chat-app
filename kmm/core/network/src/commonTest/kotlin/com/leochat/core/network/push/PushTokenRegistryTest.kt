package com.leochat.core.network.push

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.network.createHttpClient
import com.leochat.core.platform.storage.InMemorySecureStorage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushTokenRegistryTest {
    private val config = AppConfig("https://api.test/api/v1", "wss://api.test/ws")

    @Test
    fun syncAndUnregisterPushTokenMatchesBackendContract() = runTest {
        val storage = InMemorySecureStorage()
        val tokens = TokenStore(storage)
        val requests = mutableListOf<HttpRequestData>()
        val engine = MockEngine { request ->
            requests += request
            respondOk()
        }
        val http = createHttpClient(engine, config, tokens, SessionEvents())
        val provider = object : PushTokenProvider {
            override val platform = PushPlatform.ANDROID
            override suspend fun currentToken(): String = "fcm-token-123"
        }
        val registry = PushTokenRegistry(http, tokens, backgroundScope, provider)

        // Unauthenticated session skips registration
        assertFalse(registry.syncToken())
        assertTrue(requests.isEmpty())

        // Authenticated session registers ANDROID FCM token once
        storage.write(StorageKeys.accessToken, "jwt-1")
        assertTrue(registry.syncToken())
        assertTrue(registry.syncToken()) // deduplicated while token/session unchanged
        assertEquals(1, requests.size)
        assertEquals(HttpMethod.Post, requests[0].method)
        assertEquals("https://api.test/api/v1/devices/push-token", requests[0].url.toString())
        val body = (requests[0].body as TextContent).text
        assertTrue(body.contains("\"token\":\"fcm-token-123\""))
        assertTrue(body.contains("\"platform\":\"ANDROID\""))

        // Sign-out unregisters before tokens are wiped
        registry.unregisterCurrentToken()
        assertEquals(2, requests.size)
        assertEquals(HttpMethod.Delete, requests[1].method)
        assertTrue(requests[1].url.toString().startsWith("https://api.test/api/v1/devices/push-token"))
    }

    @Test
    fun failedSyncAllowsRetry() = runTest {
        val storage = InMemorySecureStorage().apply { write(StorageKeys.accessToken, "jwt-1") }
        val tokens = TokenStore(storage)
        var calls = 0
        val engine = MockEngine {
            calls++
            if (calls == 1) respond("", HttpStatusCode.InternalServerError) else respondOk()
        }
        val http = createHttpClient(engine, config, tokens, SessionEvents())
        val registry = PushTokenRegistry(http, tokens, backgroundScope)

        assertFalse(registry.syncToken("token-a"))
        assertTrue(registry.syncToken("token-a"))
        assertEquals(2, calls)
    }
}
