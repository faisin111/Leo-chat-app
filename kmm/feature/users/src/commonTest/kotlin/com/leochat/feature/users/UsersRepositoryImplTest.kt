package com.leochat.feature.users

import com.leochat.core.network.config.AppConfig
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.Either
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.users.data.remote.UsersRemoteDataSource
import com.leochat.feature.users.data.repository.UsersRepositoryImpl
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UsersRepositoryImplTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()

    private val repo = run {
        val engine = MockEngine { request ->
            requests += request
            val path = request.url.encodedPath
            when {
                path.endsWith("/users/me") && request.method.value == "GET" -> respond(
                    """{"id":"u1","username":"alex","displayName":" ","role":"ADMIN","age":30,"emailVerified":true}""",
                    headers = json,
                )
                path.endsWith("/users/search") -> respond(
                    """{"content":[{"id":"u2","username":"maya","displayName":"Maya Chen","lastSeenAt":"2026-11-23T10:00:00Z"}]}""",
                    headers = json,
                )
                path.endsWith("/users/me/sessions") -> respond(
                    """[{"sessionId":"s1","createdAt":"2026-11-20T10:00:00Z","active":true},{"id":"s2","createdAt":"2026-11-19T10:00:00Z","logoutAt":"2026-11-19T12:00:00Z","active":false}]""",
                    headers = json,
                )
                path.endsWith("/users/me/profile") -> respond("", HttpStatusCode.NoContent)
                path.endsWith("/users/me/profile-picture") && request.method.value == "DELETE" ->
                    respond("", HttpStatusCode.NoContent)
                path.endsWith("/users/presence") -> respond(
                    """[
                      {"userId":"u-online","online":true,"lastSeenAt":"2026-11-26T07:00:00Z","status":"ACTIVE"},
                      {"userId":"u-offline","online":false,"lastSeenAt":"2026-11-26T06:50:00Z","status":"ACTIVE"}
                    ]""",
                    headers = json,
                )
                path.endsWith("/users/missing") -> respondError(HttpStatusCode.NotFound)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val secure = InMemorySecureStorage(mapOf(StorageKeys.accessToken to "t"))
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        UsersRepositoryImpl(UsersRemoteDataSource(http))
    }

    @Test
    fun parsesProfileWithFallbackName() = runTest {
        val me = repo.getMe().getOrNull()!!
        assertEquals("alex", me.effectiveName)
        assertTrue(me.isEmailVerified)
        assertEquals(30, me.age)
    }

    @Test
    fun searchReadsPagedEnvelopeAndSendsQuery() = runTest {
        val items = repo.searchUsers(" maya ").getOrNull()!!
        assertEquals("Maya Chen", items.single().effectiveName)
        assertEquals("maya", requests.last().url.parameters["q"])
    }

    @Test
    fun sessionsMarkCurrentDevice() = runTest {
        val sessions = repo.getSessions().getOrNull()!!
        assertEquals(listOf("s1", "s2"), sessions.map { it.sessionId })
        assertTrue(sessions[0].isCurrentDevice)
        assertFalse(sessions[1].isCurrentDevice)
    }

    @Test
    fun updateProfileSendsOnlyChangedFields() = runTest {
        assertIs<Either.Right<Unit>>(
            repo.updateProfile(
                ProfileUpdate(
                    displayName = "Alex",
                    profilePictureStorageKey = "avatars/alex.jpg",
                    age = 31,
                ),
            ),
        )
        val body = (requests.last().body as TextContent).text
        assertEquals("""{"displayName":"Alex","profilePictureStorageKey":"avatars/alex.jpg","age":31}""", body)
        assertEquals("PATCH", requests.last().method.value)
    }

    @Test
    fun deleteProfilePictureCallsDeleteEndpoint() = runTest {
        assertIs<Either.Right<Unit>>(repo.deleteProfilePicture())
        assertEquals("DELETE", requests.last().method.value)
        assertTrue(requests.last().url.encodedPath.endsWith("/users/me/profile-picture"))
    }

    @Test
    fun missingUserMapsToFailure() = runTest {
        assertIs<AppFailure>(repo.getUserById("missing").leftOrNull())
    }

    @Test
    fun blankSearchSkipsNetwork() = runTest {
        val result = SearchUsersUseCase(repo)("   ")
        assertEquals(emptyList(), result.getOrNull())
        assertTrue(requests.isEmpty())
    }

    @Test
    fun presenceReadsOnlineBooleanAndFormatsLastSeen() = runTest {
        val items = repo.getPresence(listOf("u-online", "u-offline")).getOrNull()!!
        val now = Instant.parse("2026-11-26T07:05:00Z")

        assertEquals(listOf("u-online", "u-offline"), items.map { it.userId })
        assertTrue(items[0].isOnline(now))
        assertEquals("Active now", items[0].statusLabel(now))

        // Uses backend real-time `online=false` cleanly and formats last-seen relative text
        assertFalse(items[1].isOnline(now))
        assertEquals("Active 15m ago", items[1].statusLabel(now))
    }
}
