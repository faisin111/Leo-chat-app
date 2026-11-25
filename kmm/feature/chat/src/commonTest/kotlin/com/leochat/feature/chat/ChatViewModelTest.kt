package com.leochat.feature.chat

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.core.common.serialization.LeoJson
import com.leochat.feature.auth.FakeAuthRepository
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.chat.data.remote.MessageDto
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageAck
import com.leochat.feature.chat.domain.model.MessageAttachment
import com.leochat.feature.chat.domain.model.MessageSendStatus
import com.leochat.feature.chat.domain.model.TypingEvent
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.presentation.ChatViewModel
import com.leochat.feature.chat.presentation.ChatFilter
import com.leochat.feature.media.domain.model.PickedAttachment
import com.leochat.feature.media.domain.model.UploadedAttachment
import com.leochat.feature.media.domain.repository.MediaRepository
import com.leochat.feature.media.domain.usecase.GetMediaDownloadUrlUseCase
import com.leochat.feature.media.domain.usecase.UploadAttachmentUseCase
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatControllerTest {
    private val repo = FakeChatRepository()
    private val realtime = FakeRealtime()
    private val auth = FakeAuthRepository()

    private fun TestScope.controller(): ChatViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return ChatViewModel(ChatUseCaseBundle.from(repo), realtime, GetCurrentUserUseCase(auth), LogoutUseCase(auth))
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsConversationsConnectsAndPreselectsFirst() = runTest {
        val c = controller()
        advanceUntilIdle()
        val s = c.state.value
        assertTrue(realtime.connected)
        assertFalse(s.isLoadingConversations)
        assertEquals(listOf("c1", "c2"), s.conversations.map { it.id })
        assertEquals("c1", s.selectedConversationId)
        assertEquals(3, s.totalUnreadCount)
        assertEquals(s.conversations, repo.cachedConversations)
    }

    @Test
    fun showsCacheWhenNetworkFails() = runTest {
        repo.cachedConversations = listOf(Conversation("cached", title = "Offline chat"))
        repo.conversations = FakeChatRepository.offline
        val c = controller()
        advanceUntilIdle()
        assertEquals(listOf("cached"), c.state.value.conversations.map { it.id })
        assertNull(c.state.value.errorMessage)
    }

    @Test
    fun filtersAndSearchesLocally() = runTest {
        repo.conversations = Either.Right(
            listOf(
                Conversation("a", title = "Maya", unreadCount = 2, lastMessageSnippet = "see you"),
                Conversation("b", type = "GROUP", title = "Team"),
            ),
        )
        val c = controller()
        advanceUntilIdle()
        c.setFilter(ChatFilter.Unread)
        assertEquals(listOf("a"), c.state.value.displayedConversations.map { it.id })
        c.setFilter(ChatFilter.Groups)
        assertEquals(listOf("b"), c.state.value.displayedConversations.map { it.id })
        c.setFilter(ChatFilter.All)
        c.search("SEE")
        assertEquals(listOf("a"), c.state.value.displayedConversations.map { it.id })
    }

    @Test
    fun sendIsOptimisticThenReplacedByServerMessage() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("  hello  ")
        val optimistic = c.state.value.messages.single()
        assertEquals(MessageSendStatus.Sending, optimistic.sendStatus)
        assertEquals("hello", optimistic.content)
        advanceUntilIdle()
        val persisted = c.state.value.messages.single()
        assertEquals("srv-1", persisted.id)
        assertEquals(MessageSendStatus.Sent, persisted.sendStatus)
        assertEquals(listOf("hello"), repo.sent)
        assertEquals("hello", c.state.value.conversations.first { it.id == "c1" }.lastMessageSnippet)
    }

    @Test
    fun failedSendCanBeRetried() = runTest {
        repo.sendResult = { Either.Left(AppFailure.Network()) }
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("hi")
        advanceUntilIdle()
        val failed = c.state.value.messages.single()
        assertEquals(MessageSendStatus.Failed, failed.sendStatus)

        repo.sendResult = null
        c.retry(failed)
        advanceUntilIdle()
        assertEquals(MessageSendStatus.Sent, c.state.value.messages.single().sendStatus)
    }

    @Test
    fun aiCommandUsesRestFallbackWhenSocketDown() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("/ai summarize this")
        advanceUntilIdle()
        val messages = c.state.value.messages
        assertEquals(listOf("summarize this", "AI reply"), messages.map { it.content })
        assertTrue(repo.sent.isEmpty())
    }

    @Test
    fun realtimeMessagesMergeByIdAndMarkRead() = runTest {
        val c = controller()
        advanceUntilIdle()
        realtime.messageFlow.emit(Message("m2", "c1", content = "second", seq = 2))
        realtime.messageFlow.emit(Message("m1", "c1", content = "first", seq = 1))
        realtime.messageFlow.emit(Message("m2", "c1", content = "edited", seq = 2))
        advanceUntilIdle()
        assertEquals(listOf("first", "edited"), c.state.value.messages.map { it.content })
        assertTrue(repo.read.contains("c1" to 2L))
    }

    @Test
    fun ackConfirmsOptimisticMessage() = runTest {
        repo.sendResult = { Either.Left(AppFailure.Unknown()) }
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("hey")
        val clientId = c.state.value.messages.single().clientMessageId
        realtime.ackFlow.emit(MessageAck("c1", clientId, "srv-9", 9))
        testScheduler.runCurrent()
        val acked = c.state.value.messages.single()
        assertEquals("srv-9", acked.id)
        assertEquals(MessageSendStatus.Sent, acked.sendStatus)
    }

    @Test
    fun typingFromOthersShowsThenClears() = runTest {
        val c = controller()
        advanceUntilIdle()
        realtime.typingFlow.emit(TypingEvent("c1", "u2", "maya", "Maya", true))
        testScheduler.runCurrent()
        assertEquals("Maya is typing...", c.state.value.typingStatusText)
        advanceTimeBy(4_100)
        assertNull(c.state.value.typingStatusText)

        realtime.typingFlow.emit(TypingEvent("c1", "u1", "alex", null, true))
        testScheduler.runCurrent()
        assertNull(c.state.value.typingStatusText)
    }

    @Test
    fun leavingRemovesConversationAndClearsSelection() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.leaveConversation("c1")
        advanceUntilIdle()
        assertEquals(listOf("c2"), c.state.value.conversations.map { it.id })
        assertNull(c.state.value.selectedConversation)
    }

    @Test
    fun switchingConversationCancelsPreviousLoadAndIsolatesMessages() = runTest {
        repo.messages = Either.Right(listOf(Message("m1", "c1", content = "ai message", aiGenerated = true)))
        val c = controller()
        advanceUntilIdle()
        assertEquals("c1", c.state.value.selectedConversationId)
        assertEquals(listOf("ai message"), c.state.value.messages.map { it.content })

        // Switch to c2 with different messages
        repo.messages = Either.Right(listOf(Message("m2", "c2", content = "user message", senderUsername = "maya")))
        c.selectConversation(Conversation("c2", title = "Team"))
        advanceUntilIdle()

        assertEquals("c2", c.state.value.selectedConversationId)
        assertEquals(listOf("user message"), c.state.value.messages.map { it.content })
        assertFalse(c.state.value.messages.any { it.conversationId == "c1" })
    }

    @Test
    fun resolveCallTargetUserIdFallsBackToMessagesAndMembers() = runTest {
        // FakeAuthRepository.sampleSession.user.id is "1".
        repo.membersResult = Either.Right(
            listOf(
                ConversationMember("c-mem", "1", "OWNER"),
                ConversationMember("c-mem", "u9", "MEMBER", username = "azad"),
            ),
        )
        val c = controller()
        advanceUntilIdle()

        // Resolves from directKey
        assertEquals("u2", c.resolveCallTargetUserId(Conversation("c-dk", directKey = "1_u2")))

        // Resolves from loaded messages when targetUserId/directKey missing
        repo.messages = Either.Right(listOf(Message("m1", "c1", senderId = "u7", content = "hey")))
        c.selectConversation(Conversation("c1", title = "Maya"))
        advanceUntilIdle()
        assertEquals("u7", c.resolveCallTargetUserId(Conversation("c1", title = "Maya")))

        // Resolves from /members API fallback and refuses groups
        assertEquals("u9", c.resolveCallTargetUserId(Conversation("c-mem", title = "Azad")))
        assertNull(c.resolveCallTargetUserId(Conversation("c-grp", type = "GROUP", targetUserId = "u9")))
    }

    @Test
    fun realtimeCallAndOutOfOrderMessagesRespectSequenceForPreview() = runTest {
        val c = controller()
        advanceUntilIdle()

        // Persistent CALL message arrives and updates conversation preview
        realtime.messageFlow.emit(
            Message("call-msg-1", "c1", senderId = "u2", type = "CALL", content = "Voice call", seq = 5),
        )
        advanceUntilIdle()
        assertEquals("📞 Voice call", c.state.value.conversations.first { it.id == "c1" }.lastMessageSnippet)
        assertEquals(5L, c.state.value.conversations.first { it.id == "c1" }.lastSeq)

        // Older seq message must not overwrite newer preview snippet or lastSeq
        realtime.messageFlow.emit(
            Message("old-msg", "c1", senderId = "u2", type = "TEXT", content = "older text", seq = 2),
        )
        advanceUntilIdle()
        assertEquals("📞 Voice call", c.state.value.conversations.first { it.id == "c1" }.lastMessageSnippet)
        assertEquals(5L, c.state.value.conversations.first { it.id == "c1" }.lastSeq)
    }

    @Test
    fun signOutDisconnectsAndLogsOut() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.signOut()
        advanceUntilIdle()
        assertFalse(realtime.connected)
        assertTrue(c.state.value.signedOut)
        assertTrue("logout" in auth.calls)
    }

    @Test
    fun sendAttachmentUploadsResolvesAndParsesWebAttachments() = runTest {
        val mediaRepo = object : MediaRepository {
            override suspend fun uploadVoice(filePath: String): AppResult<String> = Either.Right("https://cdn.test/v.m4a")
            override suspend fun uploadAttachment(uriOrPath: String, fileName: String, mimeType: String): AppResult<UploadedAttachment> =
                Either.Right(UploadedAttachment("img/uploaded-1", "https://cdn.test/uploaded-1.jpg", fileName, mimeType, 1024L))
            override suspend fun getDownloadUrl(storageKey: String): AppResult<String> =
                Either.Right("https://cdn.test/$storageKey")
        }
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val c = ChatViewModel(
            useCases = ChatUseCaseBundle.from(repo),
            realtime = realtime,
            getCurrentUser = GetCurrentUserUseCase(auth),
            logout = LogoutUseCase(auth),
            uploadAttachment = UploadAttachmentUseCase(mediaRepo),
            getMediaDownloadUrl = GetMediaDownloadUrlUseCase(mediaRepo),
        )
        advanceUntilIdle()

        c.sendAttachment(PickedAttachment("content://gallery/1", "sunset.jpg", "image/jpeg", 1024L, isImage = true), "Sunset!")
        val optimistic = c.state.value.messages.single()
        assertEquals(MessageSendStatus.Sending, optimistic.sendStatus)
        assertEquals("IMAGE", optimistic.type)
        assertEquals("content://gallery/1", optimistic.primaryAttachment?.localPreviewUrl)

        advanceUntilIdle()
        assertEquals("img/uploaded-1", repo.sentAttachments.single().storageKey)
        assertEquals("sunset.jpg", repo.sentAttachments.single().fileName)
        assertEquals("https://cdn.test/uploaded-1.jpg", c.state.value.resolvedMediaUrls["img/uploaded-1"])

        // Incoming Web image message with empty caption & storageKey attachment triggers download URL resolution
        realtime.messageFlow.emit(
            Message(
                id = "web-img-1",
                conversationId = "c1",
                senderId = "u2",
                type = "IMAGE",
                content = "",
                seq = 200L,
                attachments = listOf(MessageAttachment("att-1", "img/remote-9", "photo.png", "image/png", 512L)),
            ),
        )
        advanceUntilIdle()
        assertEquals("https://cdn.test/img/remote-9", c.state.value.resolvedMediaUrls["img/remote-9"])

        val json = LeoJson.parseToJsonElement(
            """{"id":"m-att","conversationId":"c1","type":"IMAGE","content":"","attachments":[{"id":"a1","storageKey":"img/k9","fileName":"p.png","mimeType":"image/png","sizeBytes":2048}]}""",
        ) as JsonObject
        val dto = MessageDto.fromJson(json).toDomain(me)
        assertTrue(dto.isImage)
        assertEquals("img/k9", dto.primaryAttachment?.storageKey)
    }
}
