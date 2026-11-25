package com.leochat.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.util.ClientIds
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageAck
import com.leochat.feature.chat.domain.model.MessageSendStatus
import com.leochat.feature.chat.domain.model.TypingEvent
import com.leochat.feature.chat.domain.model.mergeMessages
import com.leochat.feature.chat.domain.repository.ChatRealtimeRepository
import com.leochat.feature.chat.domain.usecase.AiPrompt
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.TypingRules
import com.leochat.feature.chat.presentation.ChatFilter
import com.leochat.feature.chat.presentation.ChatUiState
import com.leochat.feature.media.domain.model.VoiceMessageType
import com.leochat.feature.media.domain.model.VoiceMessageDraft
import com.leochat.feature.media.domain.model.VoiceRecording
import com.leochat.feature.media.domain.usecase.SendVoiceMessageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Conversations, messages, realtime, typing, and optimistic sends.
 * Mirrors `ChatNotifier`: cache-first rendering, REST as the reliable path,
 * STOMP for live delivery, merge by id/clientMessageId sorted by `seq`.
 */
class ChatViewModel(
    private val useCases: ChatUseCaseBundle,
    private val realtime: ChatRealtimeRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val logout: LogoutUseCase,
    private val sendVoiceMessage: SendVoiceMessageUseCase? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = mutableState.asStateFlow()

    private var identity = CurrentIdentity("", "", "")
    private var conversationJobs: List<Job> = emptyList()
    private var selectJob: Job? = null
    private var typingClearJob: Job? = null
    private var loadConversationsJob: Job? = null
    private var isLocalUserTyping = false
    private var lastLocalTypingAt = 0L

    init {
        viewModelScope.launch {
            identity = useCases.getIdentity()
            realtime.connect()
            launch { realtime.status.collect { s -> mutableState.update { it.copy(realtimeStatus = s) } } }
            launch { realtime.acks().collect(::onAck) }
            launch {
                getCurrentUser().onRight { user ->
                    mutableState.update { it.copy(currentUser = user) }
                    if (user != null) {
                        identity = CurrentIdentity(user.id, user.username, user.displayName)
                    }
                }
            }
            loadConversations()
            loadUnreadCount()
        }
    }

    // region Conversations

    /** Cache-first list load with background refresh. Concurrent plain loads share one job. */
    fun loadConversations(query: String? = null): Job {
        val isSearch = !query.isNullOrBlank()
        if (!isSearch) loadConversationsJob?.takeIf { it.isActive }?.let { return it }
        val job = viewModelScope.launch {
            if (!isSearch) {
                val cached = useCases.getCachedConversations()
                if (cached.isNotEmpty()) {
                    mutableState.update { it.copy(conversations = cached, isLoadingConversations = false) }
                    if (state.value.selectedConversation == null) selectConversation(cached.first())
                } else if (state.value.conversations.isEmpty()) {
                    mutableState.update { it.copy(isLoadingConversations = true) }
                }
            } else if (state.value.conversations.isEmpty()) {
                mutableState.update { it.copy(isLoadingConversations = true) }
            }

            useCases.getConversations(query).fold(
                ifLeft = { failure ->
                    mutableState.update {
                        it.copy(isLoadingConversations = false, errorMessage = if (it.conversations.isEmpty()) failure.message else null)
                    }
                },
                ifRight = { items ->
                    mutableState.update { s ->
                        val refreshedSelection = s.selectedConversation?.let { sel -> items.firstOrNull { it.id == sel.id } ?: sel }
                        s.copy(conversations = items, isLoadingConversations = false, selectedConversation = refreshedSelection)
                    }
                    if (!isSearch) useCases.saveConversations(items)
                    // Flutter preselects the first chat so the split view never starts empty.
                    if (state.value.selectedConversation == null && items.isNotEmpty()) selectConversation(items.first())
                },
            )
        }
        if (!isSearch) loadConversationsJob = job
        return job
    }

    fun refresh() = loadConversations(state.value.searchQuery.trim().ifEmpty { null })

    fun search(query: String) {
        mutableState.update { it.copy(searchQuery = query) }
        loadConversations(query.trim().ifEmpty { null })
    }

    fun setFilter(filter: ChatFilter) = mutableState.update { it.copy(filter = filter) }

    fun loadUnreadCount() {
        viewModelScope.launch {
            useCases.getUnreadCount().onRight { count -> mutableState.update { it.copy(totalUnreadCount = count) } }
        }
    }

    fun leaveConversation(conversationId: String) {
        viewModelScope.launch {
            useCases.leaveConversation(conversationId).onRight {
                if (state.value.selectedConversationId == conversationId) clearSelection()
                realtime.unsubscribe(conversationId)
                val updated = state.value.conversations.filterNot { it.id == conversationId }
                mutableState.update { it.copy(conversations = updated) }
                useCases.saveConversations(updated)
            }.onLeft { failure -> mutableState.update { it.copy(errorMessage = failure.message) } }
        }
    }

    fun deleteConversation(conversationId: String) = leaveConversation(conversationId)

    fun togglePin(conversationId: String) {
        val conv = state.value.conversations.firstOrNull { it.id == conversationId } ?: return
        val newPinned = !conv.isPinned
        val updated = state.value.conversations.map {
            if (it.id == conversationId) it.copy(isPinned = newPinned) else it
        }
        mutableState.update { s ->
            s.copy(
                conversations = updated,
                selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(isPinned = newPinned) else s.selectedConversation,
            )
        }
        viewModelScope.launch {
            useCases.pinConversation(conversationId, newPinned).fold(
                ifLeft = { failure ->
                    val rolledBack = state.value.conversations.map {
                        if (it.id == conversationId) it.copy(isPinned = !newPinned) else it
                    }
                    mutableState.update { s ->
                        s.copy(
                            conversations = rolledBack,
                            errorMessage = failure.message,
                            selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(isPinned = !newPinned) else s.selectedConversation,
                        )
                    }
                },
                ifRight = {
                    useCases.saveConversations(state.value.conversations)
                },
            )
        }
    }

    fun toggleMute(conversationId: String) {
        val conv = state.value.conversations.firstOrNull { it.id == conversationId } ?: return
        val newMuted = !conv.isMuted
        val updated = state.value.conversations.map {
            if (it.id == conversationId) it.copy(isMuted = newMuted) else it
        }
        mutableState.update { s ->
            s.copy(
                conversations = updated,
                selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(isMuted = newMuted) else s.selectedConversation,
            )
        }
        viewModelScope.launch {
            useCases.setConversationMuted(conversationId, newMuted).fold(
                ifLeft = { failure ->
                    val rolledBack = state.value.conversations.map {
                        if (it.id == conversationId) it.copy(isMuted = !newMuted) else it
                    }
                    mutableState.update { s ->
                        s.copy(
                            conversations = rolledBack,
                            errorMessage = failure.message,
                            selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(isMuted = !newMuted) else s.selectedConversation,
                        )
                    }
                },
                ifRight = {
                    useCases.saveConversations(state.value.conversations)
                },
            )
        }
    }

    fun toggleArchive(conversationId: String) {
        val conv = state.value.conversations.firstOrNull { it.id == conversationId } ?: return
        val isArchived = conv.status.equals("ARCHIVED", ignoreCase = true)
        val newStatus = if (isArchived) "ACTIVE" else "ARCHIVED"
        val updated = state.value.conversations.map {
            if (it.id == conversationId) it.copy(status = newStatus) else it
        }
        mutableState.update { s ->
            s.copy(
                conversations = updated,
                selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(status = newStatus) else s.selectedConversation,
            )
        }
        viewModelScope.launch {
            useCases.archiveConversation(conversationId, !isArchived).fold(
                ifLeft = { failure ->
                    val rolledBack = state.value.conversations.map {
                        if (it.id == conversationId) it.copy(status = conv.status) else it
                    }
                    mutableState.update { s ->
                        s.copy(
                            conversations = rolledBack,
                            errorMessage = failure.message,
                            selectedConversation = if (s.selectedConversation?.id == conversationId) s.selectedConversation.copy(status = conv.status) else s.selectedConversation,
                        )
                    }
                },
                ifRight = {
                    useCases.saveConversations(state.value.conversations)
                },
            )
        }
    }

    /**
     * Resolves the callee's user ID for 1-on-1 calls (mirrors Web `ConversationPage.tsx`):
     * `targetUserId`, `directKey`, `createdBy`, loaded/cached message senders, or `/conversations/{id}/members`.
     */
    suspend fun resolveCallTargetUserId(conversation: Conversation): String? {
        if (conversation.isGroup || conversation.isAi) return null
        val myId = identity.userId.ifBlank { state.value.currentUser?.id.orEmpty() }
            .ifBlank { useCases.getIdentity().userId }
            .trim()
        val self = myId.takeIf { it.isNotEmpty() }

        conversation.resolveDirectTargetUserId(self)?.let { return it }

        state.value.messages.firstOrNull { m ->
            m.conversationId == conversation.id &&
                !m.aiGenerated &&
                m.senderId.isNotBlank() &&
                (self == null || !m.senderId.equals(self, ignoreCase = true))
        }?.senderId?.let { return cacheResolvedTarget(conversation.id, it) }

        useCases.getCachedMessages(conversation.id).firstOrNull { m ->
            !m.aiGenerated &&
                m.senderId.isNotBlank() &&
                (self == null || !m.senderId.equals(self, ignoreCase = true))
        }?.senderId?.let { return cacheResolvedTarget(conversation.id, it) }

        val members = useCases.getMembers(conversation.id).getOrNull().orEmpty()
        members.firstOrNull { member ->
            member.userId.isNotBlank() && (self == null || !member.userId.equals(self, ignoreCase = true))
        }?.let { member ->
            return cacheResolvedTarget(
                conversationId = conversation.id,
                targetUserId = member.userId,
                username = member.username,
                displayName = member.displayName,
                avatarUrl = member.avatarUrl,
            )
        }
        return null
    }

    private suspend fun cacheResolvedTarget(
        conversationId: String,
        targetUserId: String,
        username: String? = null,
        displayName: String? = null,
        avatarUrl: String? = null,
    ): String {
        fun Conversation.withTarget() = copy(
            targetUserId = targetUserId,
            targetUsername = targetUsername ?: username,
            targetDisplayName = targetDisplayName ?: displayName,
            targetAvatarUrl = targetAvatarUrl ?: avatarUrl,
        )
        val updatedList = state.value.conversations.map { if (it.id == conversationId) it.withTarget() else it }
        mutableState.update { s ->
            s.copy(
                conversations = updatedList,
                selectedConversation = s.selectedConversation?.let { sel -> if (sel.id == conversationId) sel.withTarget() else sel },
            )
        }
        useCases.saveConversations(updatedList)
        return targetUserId
    }

    // endregion

    // region Selection and messages

    fun openConversationById(conversationId: String, fallbackTitle: String? = null) {
        val existing = state.value.conversations.firstOrNull { it.id == conversationId }
            ?: Conversation(
                id = conversationId,
                title = fallbackTitle?.takeIf { it.isNotBlank() } ?: AppStrings.chats,
            )
        selectConversation(existing)
        refresh()
    }

    fun selectConversation(conversation: Conversation) {
        val current = state.value
        if (current.selectedConversationId == conversation.id && current.messages.isNotEmpty()) return
        current.selectedConversationId?.takeIf { it != conversation.id }?.let { realtime.unsubscribe(it) }
        conversationJobs.forEach { it.cancel() }
        conversationJobs = emptyList()
        selectJob?.cancel()

        // Select synchronously so a pushed conversation page never sees an empty selection or stale messages.
        mutableState.update {
            it.copy(
                selectedConversation = conversation,
                isLoadingMessages = true,
                messages = emptyList(),
                typingStatusText = null,
                errorMessage = null,
            )
        }

        selectJob = viewModelScope.launch {
            val cached = useCases.getCachedMessages(conversation.id)
            if (state.value.selectedConversationId != conversation.id) return@launch
            mutableState.update { s ->
                if (s.selectedConversationId == conversation.id) {
                    s.copy(isLoadingMessages = cached.isEmpty(), messages = cached)
                } else s
            }
            conversationJobs = listOf(
                launch { realtime.messages(conversation.id, identity).collect(::onRealtimeMessage) },
                launch { realtime.typing(conversation.id).collect(::onTyping) },
            )
            loadMessages(conversation.id, preserveCurrentOnError = cached.isNotEmpty())
        }
    }

    fun clearSelection() {
        selectJob?.cancel()
        selectJob = null
        conversationJobs.forEach { it.cancel() }
        conversationJobs = emptyList()
        state.value.selectedConversationId?.let { realtime.unsubscribe(it) }
        mutableState.update { it.copy(selectedConversation = null, messages = emptyList(), typingStatusText = null) }
    }

    private suspend fun loadMessages(conversationId: String, preserveCurrentOnError: Boolean) {
        if (state.value.selectedConversationId != conversationId) return
        useCases.getMessages(conversationId).fold(
            ifLeft = { failure ->
                mutableState.update { s ->
                    if (s.selectedConversationId == conversationId) {
                        s.copy(
                            messages = if (preserveCurrentOnError) s.messages else emptyList(),
                            isLoadingMessages = false,
                            errorMessage = failure.message,
                        )
                    } else s
                }
            },
            ifRight = { remote ->
                if (state.value.selectedConversationId != conversationId) return@fold
                // Keep optimistic messages that the server has not returned yet, strictly for this conversation.
                val pending = state.value.messages.filter { m ->
                    m.conversationId == conversationId &&
                        (m.sendStatus != MessageSendStatus.Sent ||
                            (m.clientMessageId.isNotEmpty() && remote.none { it.clientMessageId == m.clientMessageId }))
                }
                val merged = (remote + pending).sortedBy { it.seq }
                mutableState.update { s ->
                    if (s.selectedConversationId == conversationId) {
                        s.copy(messages = merged, isLoadingMessages = false)
                    } else s
                }
                useCases.saveMessages(conversationId, merged)
                merged.lastOrNull { !it.isOwn }?.let { markRead(conversationId, it.seq) }
            },
        )
    }

    // endregion

    // region Sending

    fun sendMessage(text: String, type: String = "TEXT", replyToId: String? = null) {
        val conv = state.value.selectedConversation ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        if (conv.isAi || AiPrompt.isAiCommand(trimmed)) {
            val prompt = if (conv.isAi) trimmed else AiPrompt.strip(trimmed)
            if (prompt.isNotEmpty()) {
                sendAiPrompt(conv.id, prompt)
                return
            }
        }

        val clientMessageId = ClientIds.next("client")
        insertOptimistic(conv, clientMessageId, trimmed, type, replyToId)
        viewModelScope.launch { deliver(conv, clientMessageId, trimmed, type, replyToId) }
    }

    /**
     * Voice note: optimistic bubble plays the local file, then upload (presign, PUT, confirm)
     * and send the media URL as a `VOICE` message. Mirrors `ChatNotifier.sendVoice`.
     */
    fun sendVoice(recording: VoiceRecording) = sendVoiceFrom(recording.filePath) { useCase -> useCase(recording) }

    private fun sendVoiceFrom(
        source: String,
        upload: suspend (SendVoiceMessageUseCase) -> AppResult<VoiceMessageDraft>,
    ) {
        val conv = state.value.selectedConversation ?: return
        val useCase = sendVoiceMessage ?: return
        val clientMessageId = ClientIds.next("client")
        insertOptimistic(conv, clientMessageId, source, VoiceMessageType, null)
        viewModelScope.launch {
            upload(useCase).fold(
                ifLeft = { failure -> markFailed(clientMessageId, failure.message) },
                ifRight = { draft -> deliver(conv, clientMessageId, draft.content, draft.type, null) },
            )
        }
    }

    /** STOMP + REST with one clientMessageId, so the REST call is idempotent with the WebSocket send. */
    private suspend fun deliver(conv: Conversation, clientMessageId: String, content: String, type: String, replyToId: String?) {
        realtime.sendMessage(conv.id, clientMessageId, content, type, replyToId)
        useCases.sendMessage(conv.id, clientMessageId, content, type, replyToId).fold(
            ifLeft = { failure -> markFailed(clientMessageId, failure.message) },
            ifRight = { persisted ->
                val list = state.value.messages.map {
                    if (it.clientMessageId == clientMessageId) persisted.copy(isOwn = true, sendStatus = MessageSendStatus.Sent) else it
                }.sortedBy { it.seq }
                mutableState.update { s ->
                    if (s.selectedConversationId == conv.id) {
                        s.copy(messages = list, errorMessage = null)
                    } else s
                }
                useCases.saveMessages(conv.id, list)
                updateConversationPreview(
                    conv.id,
                    persisted.previewSnippet() ?: persisted.content ?: content,
                    persisted.seq,
                    persisted.createdAt,
                )
            },
        )
    }

    /** Re-sends a failed message (same content, new client id), as the Flutter retry does. */
    fun retry(message: Message) {
        mutableState.update { s -> s.copy(messages = s.messages.filterNot { it.clientMessageId == message.clientMessageId }) }
        if (message.type.uppercase() == VoiceMessageType || message.type.uppercase() == "AUDIO") {
            val source = message.content.orEmpty()
            sendVoiceFrom(source) { useCase -> useCase(source) }
        } else {
            sendMessage(message.content.orEmpty(), message.type, message.replyToId)
        }
    }

    private fun sendAiPrompt(conversationId: String, prompt: String) {
        val conv = state.value.selectedConversation ?: return
        if (conv.id != conversationId) return
        val clientMessageId = ClientIds.next("ai-client")
        insertOptimistic(conv, clientMessageId, prompt, "TEXT", null)
        viewModelScope.launch {
            if (realtime.sendAiChat(conversationId, prompt, clientMessageId)) return@launch
            useCases.sendAiPrompt(conversationId, prompt, clientMessageId).fold(
                ifLeft = { failure -> markFailed(clientMessageId, failure.message) },
                ifRight = { aiMessage ->
                    mutableState.update { s ->
                        if (s.selectedConversationId == conversationId) {
                            val sent = s.messages.map {
                                if (it.clientMessageId == clientMessageId) it.copy(sendStatus = MessageSendStatus.Sent) else it
                            }
                            s.copy(messages = if (sent.any { it.id == aiMessage.id }) sent else mergeMessages(sent, listOf(aiMessage)))
                        } else s
                    }
                },
            )
        }
    }

    private fun insertOptimistic(conv: Conversation, clientMessageId: String, content: String, type: String, replyToId: String?) {
        if (state.value.selectedConversationId != conv.id) return
        val messages = state.value.messages
        val optimistic = Message(
            id = clientMessageId,
            conversationId = conv.id,
            senderId = identity.userId,
            senderUsername = identity.username,
            clientMessageId = clientMessageId,
            content = content,
            type = type,
            replyToId = replyToId,
            createdAt = Clock.System.now(),
            isOwn = true,
            seq = (messages.lastOrNull()?.seq ?: conv.lastSeq) + 1,
            sendStatus = MessageSendStatus.Sending,
        )
        mutableState.update { s ->
            if (s.selectedConversationId == conv.id) {
                s.copy(messages = messages + optimistic, errorMessage = null)
            } else s
        }
    }

    private fun markFailed(clientMessageId: String, error: String) {
        mutableState.update { s ->
            s.copy(
                messages = s.messages.map { if (it.clientMessageId == clientMessageId) it.copy(sendStatus = MessageSendStatus.Failed) else it },
                errorMessage = error,
            )
        }
    }

    // endregion

    // region Message actions

    fun editMessage(messageId: String, content: String) {
        viewModelScope.launch {
            useCases.editMessage(messageId, content).onRight { updated ->
                replaceAndCache { if (it.id == messageId) updated.copy(isOwn = it.isOwn) else it }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            useCases.deleteMessage(messageId).onRight {
                val target = state.value.messages.firstOrNull { it.id == messageId }
                replaceAndCache { if (it.id == messageId) it.copy(deleted = true, content = null) else it }
                val conv = state.value.selectedConversation
                if (conv != null && target != null && target.seq >= conv.lastSeq) {
                    updateConversationPreview(conv.id, AppStrings.messageDeleted, target.seq, target.createdAt)
                }
            }
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val message = state.value.messages.firstOrNull { it.id == messageId } ?: Message(id = messageId, conversationId = "")
        viewModelScope.launch { useCases.toggleReaction(message, emoji) }
    }

    private suspend fun replaceAndCache(transform: (Message) -> Message) {
        val list = state.value.messages.map(transform)
        mutableState.update { it.copy(messages = list) }
        state.value.selectedConversationId?.let { useCases.saveMessages(it, list) }
    }

    // endregion

    // region Realtime

    private suspend fun onRealtimeMessage(incoming: Message) {
        val selectedId = state.value.selectedConversationId
        if (selectedId == incoming.conversationId) {
            mutableState.update { s ->
                if (s.selectedConversationId == incoming.conversationId) {
                    val merged = mergeMessages(s.messages, listOf(incoming))
                    s.copy(messages = merged)
                } else s
            }
            useCases.saveMessages(incoming.conversationId, state.value.messages)
            if (!incoming.isOwn) markRead(incoming.conversationId, incoming.seq)
        }
        updateConversationPreview(
            incoming.conversationId,
            incoming.previewSnippet() ?: AppStrings.attachmentFallback,
            incoming.seq,
            incoming.createdAt,
        )
    }

    private fun onAck(ack: MessageAck) {
        if (state.value.selectedConversationId != ack.conversationId) return
        mutableState.update { s ->
            if (s.selectedConversationId == ack.conversationId) {
                s.copy(
                    messages = s.messages.map {
                        // A socket ack means the server stored it, even if the REST call failed.
                        if (it.clientMessageId == ack.clientMessageId) {
                            it.copy(id = ack.id.ifEmpty { it.id }, seq = if (ack.seq > 0) ack.seq else it.seq, sendStatus = MessageSendStatus.Sent)
                        } else {
                            it
                        }
                    }.sortedBy { it.seq },
                )
            } else s
        }
    }

    private fun onTyping(event: TypingEvent) {
        if (TypingRules.isFromMe(identity, event.userId, event.username, event.displayName)) return
        val echo = !TypingRules.hasSender(event.userId, event.username, event.displayName) &&
            isLocalUserTyping && nowMillis() - lastLocalTypingAt < 3_000
        if (echo) return
        val conv = state.value.selectedConversation ?: return
        if (event.conversationId != null && event.conversationId != conv.id) return

        typingClearJob?.cancel()
        if (event.isTyping) {
            val name = event.displayName ?: event.username ?: conv.effectiveTitle
            mutableState.update { it.copy(typingStatusText = "$name is typing...") }
            typingClearJob = viewModelScope.launch {
                delay(4_000)
                mutableState.update { it.copy(typingStatusText = null) }
            }
        } else {
            mutableState.update { it.copy(typingStatusText = null) }
        }
    }

    fun sendTyping(isTyping: Boolean) {
        isLocalUserTyping = isTyping
        if (isTyping) lastLocalTypingAt = nowMillis()
        val id = state.value.selectedConversationId ?: return
        viewModelScope.launch { realtime.sendTyping(id, isTyping) }
    }

    private fun markRead(conversationId: String, upToSeq: Long) {
        viewModelScope.launch {
            realtime.sendRead(conversationId, upToSeq)
            useCases.markRead(conversationId, upToSeq)
        }
        mutableState.update { s -> s.copy(conversations = s.conversations.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }) }
    }

    private suspend fun updateConversationPreview(conversationId: String, snippet: String, lastSeq: Long, time: kotlinx.datetime.Instant?) {
        val updated = state.value.conversations.map {
            if (it.id == conversationId && (lastSeq >= it.lastSeq || it.lastSeq == 0L)) {
                it.copy(lastMessageSnippet = snippet, lastSeq = lastSeq, lastMessageAt = time ?: it.lastMessageAt ?: Clock.System.now())
            } else {
                it
            }
        }
        mutableState.update { it.copy(conversations = updated) }
        useCases.saveConversations(updated)
    }

    // endregion

    fun dismissError() = mutableState.update { it.copy(errorMessage = null) }

    fun signOut() {
        if (state.value.isSigningOut) return
        mutableState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            selectJob?.cancel()
            selectJob = null
            conversationJobs.forEach { it.cancel() }
            conversationJobs = emptyList()
            realtime.disconnect()
            logout()
            mutableState.update { it.copy(isSigningOut = false, signedOut = true) }
        }
    }

    override fun onCleared() {
        selectJob?.cancel()
        selectJob = null
        conversationJobs.forEach { it.cancel() }
        conversationJobs = emptyList()
        realtime.disconnect()
        super.onCleared()
    }

    private fun nowMillis() = Clock.System.now().toEpochMilliseconds()
}
