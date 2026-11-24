package com.leochat.feature.ai

import com.leochat.core.common.constants.AiStrings
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiActionResult
import com.leochat.feature.ai.domain.model.AiConversation
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.model.AiMessageRole
import com.leochat.feature.ai.domain.model.AiModelInfo
import com.leochat.feature.ai.domain.repository.AiRepository
import com.leochat.feature.ai.domain.usecase.AiUseCaseBundle
import com.leochat.feature.ai.presentation.ai.AiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeAiRepository : AiRepository {
    var conversation: AppResult<AiConversation> = Either.Right(AiConversation("ai-c"))
    var history: AppResult<List<AiMessage>> = Either.Right(emptyList())
    var cached: List<AiMessage> = emptyList()
    var chatResult: AppResult<AiMessage> = Either.Right(msg("r1", AiMessageRole.Assistant, "reply"))
    var deleteResult: AppResult<Unit> = Either.Right(Unit)
    var actionResult: AppResult<AiActionResult> = Either.Right(AiActionResult("SEND_MESSAGE", "Sent", "ok", false))
    val chatCalls = mutableListOf<Triple<String, String, String?>>()
    val actionCalls = mutableListOf<AiActionRequest>()
    var createCalls = 0

    override suspend fun getOrCreateConversation(): AppResult<AiConversation> = conversation.also { createCalls++ }
    override suspend fun deleteConversation(): AppResult<Unit> = deleteResult.also { if (it.isRight) cached = emptyList() }
    override suspend fun getHistory(conversationId: String, limit: Int) = history
    override suspend fun getCachedHistory() = cached
    override suspend fun saveCachedHistory(messages: List<AiMessage>) { cached = messages }
    override suspend fun chat(conversationId: String, prompt: String, clientMessageId: String?): AppResult<AiMessage> {
        chatCalls += Triple(conversationId, prompt, clientMessageId)
        return chatResult
    }
    override suspend fun executeAction(request: AiActionRequest): AppResult<AiActionResult> {
        actionCalls += request
        return actionResult
    }

    companion object {
        fun msg(id: String, role: AiMessageRole, content: String) =
            AiMessage(id, role, content, Instant.parse("2026-11-23T10:00:00Z"))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AiControllerTest {
    private val repo = FakeAiRepository()

    private fun TestScope.controller(): AiViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return AiViewModel(AiUseCaseBundle.from(repo))
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initCreatesConversationAndLoadsHistory() = runTest {
        repo.history = Either.Right(listOf(FakeAiRepository.msg("h1", AiMessageRole.User, "q")))
        val c = controller()
        advanceUntilIdle()
        assertEquals("ai-c", c.state.value.conversationId)
        assertEquals(listOf("h1"), c.state.value.messages.map { it.id })
        assertNull(c.state.value.errorMessage)
    }

    @Test
    fun showsCachedHistoryWhenServerFails() = runTest {
        repo.cached = listOf(FakeAiRepository.msg("cached", AiMessageRole.Assistant, "old"))
        repo.conversation = Either.Left(AppFailure.Network())
        val c = controller()
        advanceUntilIdle()
        assertEquals(listOf("cached"), c.state.value.messages.map { it.id })
        assertEquals(AppFailure.Network().message, c.state.value.errorMessage)
    }

    @Test
    fun emptyStateDefaultsAndSuggestionsVisible() = runTest {
        val c = controller()
        advanceUntilIdle()
        val s = c.state.value
        assertTrue(s.isEmpty)
        assertTrue(s.showSuggestions)
        assertEquals(AiModelInfo.defaultModel, s.selectedModel)
        assertFalse(s.isDeepThinkEnabled)
        assertTrue(s.isWebSearchEnabled)
    }

    @Test
    fun sendAppendsUserThenAssistantWithSelectedModel() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.selectModel(AiModelInfo.models[2])
        c.sendMessage("  explain seq  ")
        assertTrue(c.state.value.isGenerating)
        assertFalse(c.state.value.showSuggestions)
        assertEquals("explain seq", c.state.value.messages.single().content)
        advanceUntilIdle()
        val s = c.state.value
        assertFalse(s.isGenerating)
        assertEquals(listOf(AiMessageRole.User, AiMessageRole.Assistant), s.messages.map { it.role })
        assertEquals("leo-coder", s.messages.last().modelId)
        val call = repo.chatCalls.single()
        assertEquals("ai-c", call.first)
        assertEquals("explain seq", call.second)
        assertTrue(call.third!!.startsWith("ai-client-"))
        assertEquals(s.messages, repo.cached)
    }

    @Test
    fun ignoresBlankAndConcurrentSends() = runTest {
        val c = controller()
        advanceUntilIdle()
        assertNull(c.sendMessage("   "))
        c.sendMessage("one")
        assertNull(c.sendMessage("two"))
        advanceUntilIdle()
        assertEquals(1, repo.chatCalls.size)
    }

    @Test
    fun chatFailureAppendsServerErrorMessage() = runTest {
        repo.chatResult = Either.Left(AppFailure.Api("Model overloaded"))
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("hi")
        advanceUntilIdle()
        val s = c.state.value
        assertEquals(AiStrings.serverError("Model overloaded"), s.messages.last().content)
        assertEquals("Model overloaded", s.errorMessage)
        assertFalse(s.isGenerating)
    }

    @Test
    fun missingConversationRetriesThenReportsUnavailable() = runTest {
        repo.conversation = Either.Left(AppFailure.Network())
        val c = controller()
        advanceUntilIdle()
        c.sendMessage("hi")
        advanceUntilIdle()
        assertEquals(2, repo.createCalls)
        assertEquals(AiStrings.conversationUnavailable, c.state.value.messages.last().content)
        assertTrue(repo.chatCalls.isEmpty())
    }

    @Test
    fun deleteClearsMessagesAndConversation() = runTest {
        repo.history = Either.Right(listOf(FakeAiRepository.msg("h1", AiMessageRole.User, "q")))
        val c = controller()
        advanceUntilIdle()
        c.deleteConversation()
        advanceUntilIdle()
        assertTrue(c.state.value.messages.isEmpty())
        assertNull(c.state.value.conversationId)
        assertFalse(c.state.value.isGenerating)
    }

    @Test
    fun deleteFailureKeepsMessages() = runTest {
        repo.history = Either.Right(listOf(FakeAiRepository.msg("h1", AiMessageRole.User, "q")))
        repo.deleteResult = Either.Left(AppFailure.Api("nope"))
        val c = controller()
        advanceUntilIdle()
        c.clearHistory()
        advanceUntilIdle()
        assertEquals(1, c.state.value.messages.size)
        assertEquals("nope", c.state.value.errorMessage)
    }

    @Test
    fun togglesCapabilities() = runTest {
        val c = controller()
        c.toggleDeepThink()
        c.toggleWebSearch()
        assertTrue(c.state.value.isDeepThinkEnabled)
        assertFalse(c.state.value.isWebSearchEnabled)
    }

    @Test
    fun actionNeedingConfirmationBecomesPendingThenRunsConfirmed() = runTest {
        repo.actionResult = Either.Right(AiActionResult("SEND_MESSAGE", null, null, confirmationRequired = true))
        val c = controller()
        advanceUntilIdle()
        val request = AiActionRequest("SEND_MESSAGE", mapOf("to" to "maya"))
        c.executeAction(request)
        advanceUntilIdle()
        assertEquals(request, c.state.value.pendingAction)

        repo.actionResult = Either.Right(AiActionResult("SEND_MESSAGE", "Sent", "ok", confirmationRequired = false))
        c.confirmPendingAction()
        advanceUntilIdle()
        assertTrue(repo.actionCalls.last().confirmed)
        assertNull(c.state.value.pendingAction)
        assertEquals(AiStrings.actionCompleted("SEND_MESSAGE", "Sent", "ok"), c.state.value.messages.last().content)
    }

    @Test
    fun cancelPendingActionClearsBanner() = runTest {
        repo.actionResult = Either.Right(AiActionResult(null, null, null, confirmationRequired = true))
        val c = controller()
        advanceUntilIdle()
        c.executeAction(AiActionRequest("START_CALL"))
        advanceUntilIdle()
        c.cancelPendingAction()
        assertNull(c.state.value.pendingAction)
        assertNull(c.confirmPendingAction())
    }
}
