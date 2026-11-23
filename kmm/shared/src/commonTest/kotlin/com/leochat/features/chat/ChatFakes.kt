package com.leochat.features.chat

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.ConversationMember
import com.leochat.features.chat.domain.entities.CurrentIdentity
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.entities.MessageAck
import com.leochat.features.chat.domain.entities.RealtimeStatus
import com.leochat.features.chat.domain.entities.TypingEvent
import com.leochat.features.chat.domain.repositories.ChatRealtimeRepository
import com.leochat.features.chat.domain.repositories.ChatRepository
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

    override suspend fun currentIdentity() = me
    override suspend fun getConversations(query: String?, cursor: Int?, limit: Int) = conversations
    override suspend fun getConversation(conversationId: String): AppResult<Conversation> = Either.Right(Conversation(conversationId))
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
    override suspend fun startDirectMessage(targetUserId: String): AppResult<Conversation> = Either.Right(Conversation("dm"))
    override suspend fun createGroup(title: String, memberIds: List<String>): AppResult<Conversation> = Either.Right(Conversation("g", type = "GROUP", title = title))
    override suspend fun updateConversation(conversationId: String, title: String?, avatarUrl: String?): AppResult<Conversation> = Either.Right(Conversation(conversationId))
    override suspend fun leaveConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getMembers(conversationId: String): AppResult<List<ConversationMember>> = Either.Right(emptyList())
    override suspend fun addGroupMembers(conversationId: String, memberIds: List<String>): AppResult<Unit> = Either.Right(Unit)
    override suspend fun removeMember(conversationId: String, userId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun updateMemberRole(conversationId: String, userId: String, role: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun muteConversation(conversationId: String, mutedUntil: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun unmuteConversation(conversationId: String): AppResult<Unit> = Either.Right(Unit)
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
