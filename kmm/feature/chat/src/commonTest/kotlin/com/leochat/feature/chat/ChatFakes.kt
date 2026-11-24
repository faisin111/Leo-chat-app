package com.leochat.feature.chat

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageAck
import com.leochat.feature.chat.domain.model.RealtimeStatus
import com.leochat.feature.chat.domain.model.TypingEvent
import com.leochat.feature.chat.domain.repository.ChatRealtimeRepository
import com.leochat.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter

val me = CurrentIdentity("u1", "alex", "Alex")

class FakeChatRepository : ChatRepository {
    var conversations: AppResult<List<Conversation>> = Either.Right(listOf(Conversation("c1", title = "Maya"), Conversation("c2", type = "GROUP", title = "Team")))
    var messages: AppResult<List<Message>> = Either.Right(emptyList())
    var sendResult: ((String) -> AppResult<Message>)? = null
    var cachedConversations = listOf<Conversation>()
    val cachedMessages = mutableMapOf<String, List<Message>>()
    val sent = mutableListOf<String>()
    val read = mutableListOf<Pair<String, Long>>()

    // Contacts / group overrides. Defaults keep the original fake behavior.
    var conversationResult: ((String) -> AppResult<Conversation>)? = null
    var directResult: AppResult<Conversation>? = null
    var groupResult: AppResult<Conversation>? = null
    var membersResult: AppResult<List<ConversationMember>> = Either.Right(emptyList())
    var memberMutationResult: AppResult<Unit> = Either.Right(Unit)
    var muteResult: AppResult<Unit> = Either.Right(Unit)
    val directTargets = mutableListOf<String>()
    val createdGroups = mutableListOf<Pair<String, List<String>>>()
    val addedMembers = mutableListOf<Pair<String, List<String>>>()
    val removedMembers = mutableListOf<Pair<String, String>>()
    val mutes = mutableListOf<Pair<String, String?>>()

    override suspend fun currentIdentity() = me
    override suspend fun getConversations(query: String?, cursor: Int?, limit: Int) = conversations
    override suspend fun getConversation(conversationId: String): AppResult<Conversation> =
        conversationResult?.invoke(conversationId) ?: Either.Right(Conversation(conversationId))
    override suspend fun getMessages(conversationId: String, beforeSeq: Long?, afterSeq: Long?, limit: Int) = messages
    override suspend fun sendMessage(conversationId: String, clientMessageId: String, content: String, type: String, replyToId: String?): AppResult<Message> {
        sent += content
        return sendResult?.invoke(clientMessageId)
            ?: Either.Right(Message(id = "srv-${sent.size}", conversationId = conversationId, clientMessageId = clientMessageId, content = content, seq = 100L + sent.size, senderId = me.userId, isOwn = true))
    }
    override suspend fun editMessage(messageId: String, content: String): AppResult<Message> = Either.Right(Message(messageId, "c1", content = content))
    override suspend fun deleteMessage(messageId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun addReaction(messageId: String, emoji: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun removeReaction(messageId: String, emoji: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun markConversationRead(conversationId: String, upToSeq: Long): AppResult<Unit> {
        read += conversationId to upToSeq
        return Either.Right(Unit)
    }
    override suspend fun getUnreadCount(): AppResult<Int> = Either.Right(3)
    override suspend fun startDirectMessage(targetUserId: String): AppResult<Conversation> {
        directTargets += targetUserId
        return directResult ?: Either.Right(Conversation("dm"))
    }
    override suspend fun createGroup(title: String, memberIds: List<String>): AppResult<Conversation> {
        createdGroups += title to memberIds
        return groupResult ?: Either.Right(Conversation("g", type = "GROUP", title = title))
    }
    override suspend fun updateConversation(conversationId: String, title: String?, avatarUrl: String?): AppResult<Conversation> = Either.Right(Conversation(conversationId))
    override suspend fun leaveConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getMembers(conversationId: String): AppResult<List<ConversationMember>> = membersResult
    override suspend fun addGroupMembers(conversationId: String, memberIds: List<String>): AppResult<Unit> {
        addedMembers += conversationId to memberIds
        return memberMutationResult
    }
    override suspend fun removeMember(conversationId: String, userId: String): AppResult<Unit> {
        removedMembers += conversationId to userId
        return memberMutationResult
    }
    override suspend fun updateMemberRole(conversationId: String, userId: String, role: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun muteConversation(conversationId: String, mutedUntil: String): AppResult<Unit> {
        mutes += conversationId to mutedUntil
        return muteResult
    }
    override suspend fun unmuteConversation(conversationId: String): AppResult<Unit> {
        mutes += conversationId to null
        return muteResult
    }
    override suspend fun pinConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun unpinConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun archiveConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun unarchiveConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun aiChat(conversationId: String, prompt: String, clientMessageId: String): AppResult<Message> =
        Either.Right(Message("ai-1", conversationId, content = "AI reply", aiGenerated = true, seq = 500))
    override suspend fun getCachedConversations() = cachedConversations
    override suspend fun saveCachedConversations(conversations: List<Conversation>) { cachedConversations = conversations }
    override suspend fun getCachedMessages(conversationId: String) = cachedMessages[conversationId].orEmpty()
    override suspend fun saveCachedMessages(conversationId: String, messages: List<Message>) { cachedMessages[conversationId] = messages }

    companion object {
        val offline: AppResult<Nothing> = Either.Left(AppFailure.Network())
    }
}

class FakeRealtime : ChatRealtimeRepository {
    val statusFlow = MutableStateFlow(RealtimeStatus.Connected)
    val messageFlow = MutableSharedFlow<Message>(extraBufferCapacity = 16)
    val typingFlow = MutableSharedFlow<TypingEvent>(extraBufferCapacity = 16)
    val ackFlow = MutableSharedFlow<MessageAck>(extraBufferCapacity = 16)
    var connected = false
    var acceptSends = false
    val typingSent = mutableListOf<Boolean>()

    override val status: Flow<RealtimeStatus> = statusFlow
    override fun connect() { connected = true }
    override fun disconnect() { connected = false }
    override fun messages(conversationId: String, identity: CurrentIdentity): Flow<Message> = messageFlow.filter { it.conversationId == conversationId }
    override fun typing(conversationId: String): Flow<TypingEvent> = typingFlow
    override fun acks(): Flow<MessageAck> = ackFlow
    override fun unsubscribe(conversationId: String) = Unit
    override suspend fun sendMessage(conversationId: String, clientMessageId: String, content: String, type: String, replyToId: String?) = acceptSends
    override suspend fun sendAiChat(conversationId: String, prompt: String, clientMessageId: String) = acceptSends
    override suspend fun sendTyping(conversationId: String, isTyping: Boolean): Boolean {
        typingSent += isTyping
        return acceptSends
    }
    override suspend fun sendRead(conversationId: String, upToSeq: Long) = acceptSends
}
