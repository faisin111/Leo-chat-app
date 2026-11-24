package com.leochat.feature.ai

import com.leochat.core.datastore.cache.CacheKeys
import com.leochat.core.datastore.cache.KeyValueCacheStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.Either
import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.InMemoryKeyValueStorage
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.ai.data.local.AiLocalDataSource
import com.leochat.feature.ai.data.remote.AiRemoteDataSourceImpl
import com.leochat.feature.ai.data.repository.AiRepositoryImpl
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiMessageRole
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiRepositoryImplTest {
    private val secure = InMemorySecureStorage(mapOf(StorageKeys.userId to "u1", StorageKeys.accessToken to "t"))
    private val kv = InMemoryKeyValueStorage()
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()
    private var failChat = false

    private val repo = run {
        val engine = MockEngine { request ->
            requests += request
            val path = request.url.encodedPath
            when {
                path.endsWith("/ai/conversation") && request.method == HttpMethod.Post ->
                    respond("""{"id":"ai-c","title":"Leo AI"}""", headers = json)
                path.endsWith("/ai/conversation") && request.method == HttpMethod.Delete ->
                    respond("", HttpStatusCode.NoContent)
                path.endsWith("/conversations/ai-c/messages") -> respond(
                    """{"items":[{"id":"m2","seq":2,"content":"answer","aiGenerated":true},{"id":"m1","seq":1,"content":"question"}]}""",
                    headers = json,
                )
                path.endsWith("/ai/chat") && failChat ->
                    respond("""{"message":"Model overloaded"}""", HttpStatusCode.ServiceUnavailable, json)
                path.endsWith("/ai/chat") ->
                    respond("""{"id":"r1","conversationId":"ai-c","seq":3,"content":"reply","aiGenerated":false}""", headers = json)
                path.endsWith("/ai/actions") ->
                    respond("""{"action":"SEND_MESSAGE","confirmationRequired":true}""", headers = json)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val http = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        AiRepositoryImpl(AiRemoteDataSourceImpl(http), AiLocalDataSource(secure, KeyValueCacheStore(kv)))
    }

    private fun HttpRequestData.jsonBody() = LeoJson.parseToJsonElement((body as TextContent).text).jsonObject

    @Test
    fun getsOrCreatesConversation() = runTest {
        val conversation = repo.getOrCreateConversation().getOrNull()
        assertEquals("ai-c", conversation?.id)
        assertEquals(HttpMethod.Post, requests.single().method)
    }

    @Test
    fun historyIsSortedBySeqMappedByRoleAndCached() = runTest {
        val history = repo.getHistory("ai-c").getOrNull()!!
        assertEquals(listOf("m1", "m2"), history.map { it.id })
        assertEquals(listOf(AiMessageRole.User, AiMessageRole.Assistant), history.map { it.role })
        assertEquals("50", requests.single().url.parameters["limit"])
        assertEquals(listOf("m1", "m2"), repo.getCachedHistory().map { it.id })
        assertNotNull(kv.getString(CacheKeys.aiMessages("u1")))
    }

    @Test
    fun chatPostsPromptAndAlwaysReturnsAssistant() = runTest {
        val reply = repo.chat("ai-c", "hello", "k1").getOrNull()!!
        assertEquals(AiMessageRole.Assistant, reply.role)
        assertEquals("reply", reply.content)
        val body = requests.single().jsonBody()
        assertEquals("ai-c", body["conversationId"]?.jsonPrimitive?.content)
        assertEquals("hello", body["prompt"]?.jsonPrimitive?.content)
        assertEquals("k1", body["clientMessageId"]?.jsonPrimitive?.content)
    }

    @Test
    fun chatOmitsMissingClientMessageId() = runTest {
        repo.chat("ai-c", "hello", null)
        assertNull(requests.single().jsonBody()["clientMessageId"])
    }

    @Test
    fun chatFailureMapsToAppFailure() = runTest {
        failChat = true
        val result = repo.chat("ai-c", "hello", "k1")
        assertIs<Either.Left<AppFailure>>(result)
        assertEquals("Model overloaded", result.value.message)
    }

    @Test
    fun deleteClearsCachedHistory() = runTest {
        repo.getHistory("ai-c")
        assertTrue(repo.getCachedHistory().isNotEmpty())
        assertTrue(repo.deleteConversation().isRight)
        assertTrue(repo.getCachedHistory().isEmpty())
    }

    @Test
    fun executeActionSendsRequestAndParsesResult() = runTest {
        val result = repo.executeAction(AiActionRequest("SEND_MESSAGE", mapOf("text" to "hi"))).getOrNull()!!
        assertTrue(result.confirmationRequired)
        assertEquals("SEND_MESSAGE", result.action)
        assertEquals("hi", requests.single().jsonBody()["text"]?.jsonPrimitive?.content)
    }
}
