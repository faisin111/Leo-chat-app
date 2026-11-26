package com.leochat.feature.settings

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.network.createHttpClient
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.settings.data.remote.BlockedUsersRemoteDataSource
import com.leochat.feature.settings.data.repository.BlockedUsersRepositoryImpl
import com.leochat.feature.users.domain.model.UserProfile
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockedUsersRepositoryImplTest {
    @Test
    fun parsesGetBlockedUsersActionAndEnrichesProfiles() = runTest {
        var capturedBody = ""
        val engine = MockEngine { request ->
            capturedBody = (request.body as TextContent).text
            respond(
                """{"action":"GET_BLOCKED_USERS","executed":true,"data":[{"userId":"u-old","blockedAt":"2026-11-20T10:00:00Z"},{"userId":"u-new","blockedAt":"2026-11-25T10:00:00Z"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val secure = InMemorySecureStorage(mapOf(StorageKeys.accessToken to "t"))
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        val users = SettingsFakeUsersRepository().apply {
            meResult = com.leochat.core.common.result.Either.Right(UserProfile(id = "u", username = "blocked_user", displayName = "Blocked Person"))
        }
        val repo = BlockedUsersRepositoryImpl(BlockedUsersRemoteDataSource(http), users)

        val result = repo.getBlockedUsers().getOrNull()!!
        assertEquals("""{"action":"GET_BLOCKED_USERS"}""", capturedBody)
        assertEquals(listOf("u-new", "u-old"), result.map { it.userId })
        assertEquals("Blocked Person", result.first().effectiveName)
    }
}
