package com.leochat.features.chat

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.Either
import com.leochat.features.auth.FakeAuthRepository
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.entities.MessageAck
import com.leochat.features.chat.domain.entities.MessageSendStatus
import com.leochat.features.chat.domain.entities.TypingEvent
import com.leochat.features.chat.domain.usecases.ChatUseCaseBundle
import com.leochat.features.chat.presentation.controllers.ChatController
import com.leochat.features.chat.presentation.states.ChatFilter
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

    private fun TestScope.controller(): ChatController {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return ChatController(ChatUseCaseBundle.from(repo), realtime, GetCurrentUserUseCase(auth), LogoutUseCase(auth))
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
    fun signOutDisconnectsAndLogsOut() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.signOut()
        advanceUntilIdle()
        assertFalse(realtime.connected)
        assertTrue(c.state.value.signedOut)
        assertTrue("logout" in auth.calls)
    }
}
