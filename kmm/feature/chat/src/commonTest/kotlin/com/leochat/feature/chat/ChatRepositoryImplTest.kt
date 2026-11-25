package com.leochat.feature.chat

import com.leochat.core.datastore.cache.KeyValueCacheStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.common.result.Either
import com.leochat.core.common.serialization.LeoJson
import com.leochat.feature.chat.data.remote.ConversationDto
import kotlinx.serialization.json.JsonObject
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.InMemoryKeyValueStorage
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.chat.data.local.ChatLocalDataSource
import com.leochat.feature.chat.data.remote.ChatRemoteDataSourceImpl
import com.leochat.feature.chat.data.repository.ChatRepositoryImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatRepositoryImplTest {
    private val secure = InMemorySecureStorage(mapOf(StorageKeys.userId to "u1", StorageKeys.username to "alex", StorageKeys.accessToken to "t"))
    private val kv = InMemoryKeyValueStorage()
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private var online = true

    private val repo = run {
        val engine = MockEngine { request ->
            if (!online) throw kotlinx.io.IOException("offline")
            val path = request.url.encodedPath
            when {
                path.endsWith("/conversations") -> respond(
                    """{"content":[{"id":"c1","type":"DIRECT","otherUser":{"id":"u2","displayName":"Maya Chen","avatar":"https://x/m.png"},"lastMessage":{"content":"hey"},"unreadCount":2,"lastSeq":7}]}""",
                    headers = json,
                )
                path.endsWith("/conversations/c1/messages") && request.method.value == "GET" -> respond(
                    """{"items":[{"id":"m2","conversationId":"c1","senderId":"u2","seq":2,"content":"yo","createdAt":"2026-11-23T10:00:00Z"},{"id":"m1","conversationId":"c1","senderUsername":"alex","seq":1,"content":"hi"}]}""",
                    headers = json,
                )
                path.endsWith("/conversations/c1/messages") -> respond(
                    """{"id":"m3","conversationId":"c1","senderId":"u1","seq":3,"clientMessageId":"k1","content":"sent"}""",
                    headers = json,
                )
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        ChatRepositoryImpl(ChatRemoteDataSourceImpl(http), ChatLocalDataSource(secure, KeyValueCacheStore(kv)))
    }

    @Test
    fun parsesNestedTargetAndSnippetThenCaches() = runTest {
        val c = repo.getConversations().getOrNull()!!.single()
        assertEquals("Maya Chen", c.effectiveTitle)
        assertEquals("https://x/m.png", c.preferredAvatarUrl)
        assertEquals("u2", c.targetUserId)
        assertEquals("hey", c.lastMessageSnippet)
        assertEquals(2, c.unreadCount)
        assertEquals(listOf("c1"), repo.getCachedConversations().map { it.id })
    }

    @Test
    fun fallsBackToCacheWhenOffline() = runTest {
        repo.getConversations()
        repo.getMessages("c1")
        online = false
        assertEquals(listOf("c1"), repo.getConversations().getOrNull()?.map { it.id })
        assertEquals(listOf("m1", "m2"), repo.getMessages("c1").getOrNull()?.map { it.id })
    }

    @Test
    fun messagesAreSortedBySeqAndMarkedOwn() = runTest {
        val messages = repo.getMessages("c1").getOrNull()!!
        assertEquals(listOf(1L, 2L), messages.map { it.seq })
        assertTrue(messages.first().isOwn) // matched by username
        assertEquals(false, messages.last().isOwn)
        assertEquals("2026-11-23T10:00:00Z", messages.last().createdAt.toString())
    }

    @Test
    fun sendAppendsToMessageCache() = runTest {
        repo.getMessages("c1")
        val sent = repo.sendMessage("c1", "k1", "sent")
        assertIs<Either.Right<*>>(sent)
        assertEquals(listOf("m1", "m2", "m3"), repo.getCachedMessages("c1").map { it.id })
    }

    @Test
    fun conversationDtoExcludesCurrentUserAndParsesDirectKey() {
        val withMembers = LeoJson.parseToJsonElement(
            """{"id":"c1","type":"DIRECT","members":[{"userId":"u1","displayName":"Alex"},{"userId":"u2","displayName":"Azad"}]}""",
        ) as JsonObject
        val dtoFromMembers = ConversationDto.fromJson(withMembers, currentUserId = "u1")
        assertEquals("u2", dtoFromMembers.targetUserId)
        assertEquals("Azad", dtoFromMembers.title)

        val meId = "11111111-1111-1111-1111-111111111111"
        val peerId = "22222222-2222-2222-2222-222222222222"
        val withDirectKey = LeoJson.parseToJsonElement(
            """{"id":"c2","type":"DIRECT","directKey":"$meId:$peerId"}""",
        ) as JsonObject
        val dtoFromDirectKey = ConversationDto.fromJson(withDirectKey, currentUserId = meId)
        assertEquals(peerId, dtoFromDirectKey.targetUserId)
    }

    @Test
    fun conversationDtoParsesBackendLastMessageShapeAndDeletedState() {
        val deletedObj = LeoJson.parseToJsonElement(
            """{"id":"c1","type":"DIRECT","lastMessage":{"id":"m1","senderId":"u2","type":"TEXT","content":null,"createdAt":"2026-11-25T10:00:00Z","deleted":true}}""",
        ) as JsonObject
        val deletedDto = ConversationDto.fromJson(deletedObj, currentUserId = "u1")
        assertEquals("This message was deleted", deletedDto.lastMessageSnippet)
        assertEquals("2026-11-25T10:00:00Z", deletedDto.lastMessageAt)
        assertEquals("u2", deletedDto.targetUserId)

        val callObj = LeoJson.parseToJsonElement(
            """{"id":"c2","type":"DIRECT","lastMessage":{"id":"m2","senderId":"u2","type":"CALL","content":"Video call","createdAt":"2026-11-25T10:05:00Z","deleted":false}}""",
        ) as JsonObject
        val callDto = ConversationDto.fromJson(callObj, currentUserId = "u1")
        assertEquals("📹 Video call", callDto.lastMessageSnippet)
        assertEquals("2026-11-25T10:05:00Z", callDto.lastMessageAt)

        val fileObj = LeoJson.parseToJsonElement(
            """{"id":"c3","type":"DIRECT","lastMessage":{"id":"m3","senderId":"u2","type":"FILE","content":"https://cdn/spec.pdf","createdAt":"2026-11-25T10:06:00Z","deleted":false}}""",
        ) as JsonObject
        val fileDto = ConversationDto.fromJson(fileObj, currentUserId = "u1")
        assertEquals("📎 spec.pdf", fileDto.lastMessageSnippet)
    }
}
