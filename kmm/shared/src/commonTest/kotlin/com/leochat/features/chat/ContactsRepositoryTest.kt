package com.leochat.features.chat

import com.leochat.core.cache.KeyValueCacheStore
import com.leochat.core.config.AppConfig
import com.leochat.core.constants.StorageKeys
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.Either
import com.leochat.core.network.createHttpClient
import com.leochat.core.session.SessionEvents
import com.leochat.core.session.TokenStore
import com.leochat.core.storage.InMemoryKeyValueStorage
import com.leochat.core.storage.InMemorySecureStorage
import com.leochat.features.chat.data.datasources.ChatLocalDataSource
import com.leochat.features.chat.data.datasources.ChatRemoteDataSourceImpl
import com.leochat.features.chat.data.repositories.ChatRepositoryImpl
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.usecases.AddGroupMembersUseCase
import com.leochat.features.chat.domain.usecases.MergeConversationIntoCacheUseCase
import com.leochat.features.chat.domain.usecases.ResolveConversationUseCase
import com.leochat.features.chat.domain.usecases.SetConversationMutedUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Group, direct-message, member, and mute endpoints used by the contacts screens. */
class ContactsRepositoryTest {
    private val secure = InMemorySecureStorage(mapOf(StorageKeys.userId to "u1", StorageKeys.username to "alex", StorageKeys.accessToken to "t"))
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val calls = mutableListOf<String>()

    private val repo = run {
        val engine = MockEngine { request ->
            val path = request.url.encodedPath.substringAfter("/api/v1/")
            val body = request.body.toByteArray().decodeToString()
            calls += "${request.method.value} $path $body".trim()
            when {
                path == "conversations/direct" -> respond("""{"id":"dm1","type":"DIRECT","otherUser":{"id":"u2","displayName":"Maya"}}""", headers = json)
                path == "conversations/group" -> respond("""{"id":"g1","type":"GROUP","title":"Team"}""", headers = json)
                path == "conversations/g1/members" && request.method.value == "GET" -> respond(
                    """[{"conversationId":"g1","userId":"u1","role":"OWNER","username":"alex"},{"conversationId":"g1","id":"u2","displayName":"Maya","profilePictureUrl":"https://x/m.png"}]""",
                    headers = json,
                )
                path == "conversations/g1/members/u404" -> respondError(HttpStatusCode.Forbidden, """{"message":"Only admins can remove members"}""", json)
                path.startsWith("conversations/g1") -> respond("", HttpStatusCode.NoContent)
                path == "conversations/g2" -> respond("""{"id":"g2","type":"GROUP","title":"Remote"}""", headers = json)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        ChatRepositoryImpl(ChatRemoteDataSourceImpl(http), ChatLocalDataSource(secure, KeyValueCacheStore(InMemoryKeyValueStorage())))
    }

    @Test
    fun startDirectMessagePostsTargetAndParsesConversation() = runTest {
        val c = repo.startDirectMessage("u2").getOrNull()!!
        assertEquals("dm1", c.id)
        assertEquals("Maya", c.effectiveTitle)
        assertTrue(calls.single().startsWith("POST conversations/direct"))
        assertTrue(calls.single().contains("\"targetUserId\":\"u2\""))
    }

    @Test
    fun createGroupPostsTitleAndMemberIds() = runTest {
        val c = repo.createGroup("Team", listOf("u2", "u3")).getOrNull()!!
        assertTrue(c.isGroup)
        assertEquals("Team", c.effectiveTitle)
        assertTrue(calls.single().contains("\"memberIds\":[\"u2\",\"u3\"]"))
    }

    @Test
    fun getMembersParsesIdAndAvatarFallbacks() = runTest {
        val members = repo.getMembers("g1").getOrNull()!!
        assertEquals(listOf("u1", "u2"), members.map { it.userId })
        assertTrue(members[0].isOwner)
        assertEquals("MEMBER", members[1].role)
        assertEquals("Maya", members[1].effectiveName)
        assertEquals("https://x/m.png", members[1].avatarUrl)
    }

    @Test
    fun memberMutationsHitExpectedEndpoints() = runTest {
        assertIs<Either.Right<Unit>>(AddGroupMembersUseCase(repo)("g1", listOf("u3", "u3")))
        assertIs<Either.Right<Unit>>(repo.removeMember("g1", "u2"))
        assertEquals("POST conversations/g1/members {\"memberIds\":[\"u3\"]}", calls[0])
        assertEquals("DELETE conversations/g1/members/u2", calls[1])
    }

    @Test
    fun forbiddenRemoveMapsToFailure() = runTest {
        val result = repo.removeMember("g1", "u404")
        assertIs<AppFailure>(result.leftOrNull())
    }

    @Test
    fun muteSendsOneYearAndUnmuteDeletes() = runTest {
        val now = Instant.parse("2026-11-24T00:00:00Z")
        val useCase = SetConversationMutedUseCase(repo) { now }
        useCase("g1", muted = true)
        useCase("g1", muted = false)
        assertEquals("PUT conversations/g1/mute {\"mutedUntil\":\"2027-11-24T00:00:00Z\"}", calls[0])
        assertEquals("DELETE conversations/g1/mute", calls[1])
    }

    @Test
    fun resolvePrefersCacheThenRest() = runTest {
        MergeConversationIntoCacheUseCase(repo)(Conversation("g1", type = "GROUP", title = "Cached", isMuted = true))
        val cached = ResolveConversationUseCase(repo)("g1").getOrNull()!!
        assertEquals("Cached", cached.title)
        assertTrue(cached.isMuted)
        assertTrue(calls.isEmpty())
        assertEquals("Remote", ResolveConversationUseCase(repo)("g2").getOrNull()!!.title)
        assertEquals("GET conversations/g2", calls.single())
    }
}
