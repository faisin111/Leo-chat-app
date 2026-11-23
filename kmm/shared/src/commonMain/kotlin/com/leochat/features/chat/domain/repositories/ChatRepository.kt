package com.leochat.features.chat.domain.repositories

import com.leochat.core.errors.AppResult
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.ConversationMember
import com.leochat.features.chat.domain.entities.CurrentIdentity
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.entities.MessageAck
import com.leochat.features.chat.domain.entities.RealtimeStatus
import com.leochat.features.chat.domain.entities.TypingEvent
import kotlinx.coroutines.flow.Flow

/** REST + cache operations. Mirrors `ChatRepository` in Flutter. */
interface ChatRepository {
    suspend fun currentIdentity(): CurrentIdentity

    suspend fun getConversations(query: String? = null, cursor: Int? = null, limit: Int = 50): AppResult<List<Conversation>>
    suspend fun getConversation(conversationId: String): AppResult<Conversation>
    suspend fun getMessages(conversationId: String, beforeSeq: Long? = null, afterSeq: Long? = null, limit: Int = 50): AppResult<List<Message>>
    suspend fun sendMessage(
        conversationId: String,
        clientMessageId: String,
        content: String,
        type: String = "TEXT",
        replyToId: String? = null,
    ): AppResult<Message>
    suspend fun editMessage(messageId: String, content: String): AppResult<Message>
    suspend fun deleteMessage(messageId: String): AppResult<Unit>
    suspend fun addReaction(messageId: String, emoji: String): AppResult<Unit>
    suspend fun removeReaction(messageId: String, emoji: String): AppResult<Unit>
    suspend fun markConversationRead(conversationId: String, upToSeq: Long): AppResult<Unit>
    suspend fun getUnreadCount(): AppResult<Int>

    suspend fun startDirectMessage(targetUserId: String): AppResult<Conversation>
    suspend fun createGroup(title: String, memberIds: List<String>): AppResult<Conversation>
    suspend fun updateConversation(conversationId: String, title: String? = null, avatarUrl: String? = null): AppResult<Conversation>
    suspend fun leaveConversation(conversationId: String): AppResult<Unit>
    suspend fun getMembers(conversationId: String): AppResult<List<ConversationMember>>
    suspend fun addGroupMembers(conversationId: String, memberIds: List<String>): AppResult<Unit>
    suspend fun removeMember(conversationId: String, userId: String): AppResult<Unit>
    suspend fun updateMemberRole(conversationId: String, userId: String, role: String): AppResult<Unit>
    suspend fun muteConversation(conversationId: String, mutedUntil: String): AppResult<Unit>
    suspend fun unmuteConversation(conversationId: String): AppResult<Unit>
    suspend fun pinConversation(conversationId: String): AppResult<Unit>
    suspend fun unpinConversation(conversationId: String): AppResult<Unit>
    suspend fun archiveConversation(conversationId: String): AppResult<Unit>
    suspend fun unarchiveConversation(conversationId: String): AppResult<Unit>

    suspend fun aiChat(conversationId: String, prompt: String, clientMessageId: String): AppResult<Message>

    suspend fun getCachedConversations(): List<Conversation>
    suspend fun saveCachedConversations(conversations: List<Conversation>)
    suspend fun getCachedMessages(conversationId: String): List<Message>
    suspend fun saveCachedMessages(conversationId: String, messages: List<Message>)
}

/** STOMP transport for chat. Mirrors the chat parts of `StompService`. */
interface ChatRealtimeRepository {
    val status: Flow<RealtimeStatus>
    fun connect()
    fun disconnect()
    fun messages(conversationId: String, identity: CurrentIdentity): Flow<Message>
    fun typing(conversationId: String): Flow<TypingEvent>
    fun acks(): Flow<MessageAck>
    fun unsubscribe(conversationId: String)
    suspend fun sendMessage(conversationId: String, clientMessageId: String, content: String, type: String = "TEXT", replyToId: String? = null): Boolean
    suspend fun sendAiChat(conversationId: String, prompt: String, clientMessageId: String): Boolean
    suspend fun sendTyping(conversationId: String, isTyping: Boolean): Boolean
    suspend fun sendRead(conversationId: String, upToSeq: Long): Boolean
}
