package com.leochat.feature.auth

import com.leochat.core.datastore.cache.CacheKeys
import com.leochat.core.datastore.cache.KeyValueCacheStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.Either
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.InMemoryKeyValueStorage
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.auth.data.local.AuthLocalDataSource
import com.leochat.feature.auth.data.remote.AuthRemoteDataSourceImpl
import com.leochat.feature.auth.data.repository.AuthRepositoryImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private val config = AppConfig("https://api.test/api/v1", "wss://api.test/ws")
    private val secure = InMemorySecureStorage()
    private val kv = InMemoryKeyValueStorage()
    private val tokens = TokenStore(secure)
    private val events = SessionEvents()
    private val requests = mutableListOf<HttpRequestData>()

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): AuthRepositoryImpl {
        val engine = MockEngine { request -> requests += request; handler(request) }
        val http = createHttpClient(engine, config, tokens, events)
        return AuthRepositoryImpl(AuthRemoteDataSourceImpl(http), AuthLocalDataSource(secure, tokens), KeyValueCacheStore(kv))
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun loginParsesAlternateTokenKeyAndPersistsSession() = runTest {
        val repo = repository {
            respond("""{"token":"abc","refreshToken":"r1","user":{"id":7,"username":"alex","avatar":"https://x/a.png"}}""", headers = jsonHeaders)
        }
        val result = repo.login("alex", "pw")
        assertIs<Either.Right<*>>(result)
        assertEquals("abc", secure.read(StorageKeys.accessToken))
        assertEquals("r1", secure.read(StorageKeys.refreshToken))
        assertEquals("7", secure.read(StorageKeys.userId))
        assertEquals("alex", secure.read(StorageKeys.displayName)) // falls back to username
        assertEquals("https://api.test/api/v1/auth/login", requests.single().url.toString())
        assertTrue(repo.hasActiveSession())
    }

    @Test
    fun loginErrorMapsServerMessage() = runTest {
        val repo = repository {
            respond("""{"message":"Bad credentials","code":"AUTH_FAILED"}""", HttpStatusCode.BadRequest, jsonHeaders)
        }
        val failure = repo.login("alex", "pw").leftOrNull()
        assertEquals(AppFailure.Api("Bad credentials", "AUTH_FAILED", 400), failure)
    }

    @Test
    fun authenticatedRequestRefreshesTokenOnceOn401() = runTest {
        secure.write(StorageKeys.accessToken, "old")
        secure.write(StorageKeys.refreshToken, "refresh")
        secure.write(StorageKeys.userId, "1")
        secure.write(StorageKeys.username, "alex")
        val repo = repository { request ->
            when {
                request.url.encodedPath.endsWith("/auth/refresh") ->
                    respond("""{"accessToken":"new","refreshToken":"refresh2"}""", headers = jsonHeaders)
                request.headers[HttpHeaders.Authorization] == "Bearer new" ->
                    respond("""{"id":"1","username":"alex","displayName":"Alex"}""", headers = jsonHeaders)
                else -> respond("", HttpStatusCode.Unauthorized)
            }
        }
        val user = repo.getCurrentUser().getOrNull()
        assertEquals("Alex", user?.displayName)
        assertEquals("new", secure.read(StorageKeys.accessToken))
        assertEquals("refresh2", secure.read(StorageKeys.refreshToken))
    }

    @Test
    fun logoutClearsTokensAndUserCache() = runTest {
        secure.write(StorageKeys.accessToken, "a")
        secure.write(StorageKeys.userId, "1")
        secure.write(StorageKeys.username, "alex")
        val cache = KeyValueCacheStore(kv)
        cache.write(CacheKeys.conversations("1"), String.serializer(), "cached")
        kv.putString("leo_theme_mode", "dark")
        val repo = repository { respond("", HttpStatusCode.OK) }

        repo.logout()

        assertNull(secure.read(StorageKeys.accessToken))
        assertNull(cache.read(CacheKeys.conversations("1"), String.serializer()))
        assertEquals("dark", kv.getString("leo_theme_mode"))
    }
}
