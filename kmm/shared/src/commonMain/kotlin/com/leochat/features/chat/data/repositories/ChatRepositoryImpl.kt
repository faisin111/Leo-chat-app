package com.leochat.features.chat.data.repositories

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.core.network.safeApiCall
import com.leochat.features.chat.data.datasources.ChatLocalDataSource
import com.leochat.features.chat.data.datasources.ChatRemoteDataSource
import com.leochat.features.chat.data.models.ConversationDto
import com.leochat.features.chat.data.models.MessageDto
import com.leochat.features.chat.data.models.SendMessageRequestDto
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.ConversationMember
import com.leochat.features.chat.domain.entities.CurrentIdentity
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.repositories.ChatRepository

/**
 * Network-first with cache fallback for lists; cache writes after sends.
 * Mirrors `ChatRepositoryImpl` in Flutter.
 */
class ChatRepositoryImpl(
    private val remote: ChatRemoteDataSource,
    private val local: ChatLocalDataSource,
) : ChatRepository {

    override suspend fun currentIdentity(): CurrentIdentity = local.identity()

    override suspend fun getConversations(query: String?, cursor: Int?, limit: Int): AppResult<List<Conversation>> {
        val userId = local.identity().userId
        return when (val result = safeApiCall { remote.getConversations(cursor, limit, query) }) {
            is Either.Right -> {
                if (cursor == null && query == null) local.saveConversations(userId, result.value)
                Either.Right(result.value.map { it.toDomain() })
            }
            is Either.Left -> {
                val cached = local.conversations(userId)
                if (cached.isNotEmpty() && result.value !is AppFailure.Unauthorized) Either.Right(cached.map { it.toDomain() }) else result
            }
        }
    }

    override suspend fun getConversation(conversationId: String) = safeApiCall { remote.getConversation(conversationId).toDomain() }

    override suspend fun getMessages(conversationId: String, beforeSeq: Long?, afterSeq: Long?, limit: Int): AppResult<List<Message>> {
        val identity = local.identity()
        return when (val result = safeApiCall { remote.getMessages(conversationId, beforeSeq, afterSeq, limit) }) {
            is Either.Right -> {
                if (beforeSeq == null && afterSeq == null) local.saveMessages(identity.userId, conversationId, result.value)
                Either.Right(result.value.map { it.toDomain(identity) }.sortedBy { it.seq })
            }
            is Either.Left -> {
                val cached = local.messages(identity.userId, conversationId)
                if (cached.isNotEmpty() && result.value !is AppFailure.Unauthorized) {
                    Either.Right(cached.map { it.toDomain(identity) }.sortedBy { it.seq })
                } else {
                    result
                }
            }
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        clientMessageId: String,
        content: String,
        type: String,
        replyToId: String?,
    ): AppResult<Message> {
        val identity = local.identity()
        return safeApiCall {
            remote.sendMessage(SendMessageRequestDto(conversationId, clientMessageId, content, type, replyToId))
        }.map { dto ->
            val cached = local.messages(identity.userId, conversationId)
            local.saveMessages(identity.userId, conversationId, cached.filterNot { it.clientMessageId == dto.clientMessageId || it.id == dto.id } + dto)
            dto.toDomain(identity)
        }
    }

    override suspend fun editMessage(messageId: String, content: String): AppResult<Message> {
        val identity = local.identity()
        return safeApiCall { remote.editMessage(messageId, content).toDomain(identity) }
    }

    override suspend fun deleteMessage(messageId: String) = safeApiCall { remote.deleteMessage(messageId) }
    override suspend fun addReaction(messageId: String, emoji: String) = safeApiCall { remote.addReaction(messageId, emoji) }
    override suspend fun removeReaction(messageId: String, emoji: String) = safeApiCall { remote.removeReaction(messageId, emoji) }
    override suspend fun markConversationRead(conversationId: String, upToSeq: Long) =
        safeApiCall { remote.markConversationRead(conversationId, upToSeq) }
    override suspend fun getUnreadCount() = safeApiCall { remote.getUnreadCount() }

    override suspend fun startDirectMessage(targetUserId: String) = safeApiCall { remote.startDirectMessage(targetUserId).toDomain() }
    override suspend fun createGroup(title: String, memberIds: List<String>) = safeApiCall { remote.createGroup(title, memberIds).toDomain() }
    override suspend fun updateConversation(conversationId: String, title: String?, avatarUrl: String?) =
        safeApiCall { remote.updateConversation(conversationId, title, avatarUrl).toDomain() }
    override suspend fun leaveConversation(conversationId: String) = safeApiCall { remote.leaveConversation(conversationId) }
    override suspend fun getMembers(conversationId: String): AppResult<List<ConversationMember>> =
        safeApiCall { remote.getMembers(conversationId).map { it.toDomain() } }
    override suspend fun addGroupMembers(conversationId: String, memberIds: List<String>) =
        safeApiCall { remote.addGroupMembers(conversationId, memberIds) }
    override suspend fun removeMember(conversationId: String, userId: String) = safeApiCall { remote.removeMember(conversationId, userId) }
    override suspend fun updateMemberRole(conversationId: String, userId: String, role: String) =
        safeApiCall { remote.updateMemberRole(conversationId, userId, role) }
    override suspend fun muteConversation(conversationId: String, mutedUntil: String) =
        safeApiCall { remote.muteConversation(conversationId, mutedUntil) }
    override suspend fun unmuteConversation(conversationId: String) = safeApiCall { remote.unmuteConversation(conversationId) }
    override suspend fun pinConversation(conversationId: String) = safeApiCall { remote.pinConversation(conversationId) }
    override suspend fun unpinConversation(conversationId: String) = safeApiCall { remote.unpinConversation(conversationId) }
    override suspend fun archiveConversation(conversationId: String) = safeApiCall { remote.archiveConversation(conversationId) }
    override suspend fun unarchiveConversation(conversationId: String) = safeApiCall { remote.unarchiveConversation(conversationId) }

    override suspend fun aiChat(conversationId: String, prompt: String, clientMessageId: String): AppResult<Message> {
        val identity = local.identity()
        return safeApiCall { remote.aiChat(conversationId, prompt, clientMessageId).toDomain(identity) }
    }

    override suspend fun getCachedConversations(): List<Conversation> =
        local.conversations(local.identity().userId).map { it.toDomain() }

    override suspend fun saveCachedConversations(conversations: List<Conversation>) =
        local.saveConversations(local.identity().userId, conversations.map(ConversationDto::fromDomain))

    override suspend fun getCachedMessages(conversationId: String): List<Message> {
        val identity = local.identity()
        return local.messages(identity.userId, conversationId).map { it.toDomain(identity) }.sortedBy { it.seq }
    }

    override suspend fun saveCachedMessages(conversationId: String, messages: List<Message>) =
        local.saveMessages(local.identity().userId, conversationId, messages.map(MessageDto::fromDomain))
}
