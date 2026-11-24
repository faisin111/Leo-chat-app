package com.leochat.feature.chat.domain.usecase

import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.repository.ChatRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class GetCurrentIdentityUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): CurrentIdentity = repository.currentIdentity()
}

class GetCachedConversationsUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): List<Conversation> = repository.getCachedConversations()
}

class GetConversationsUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(query: String? = null): AppResult<List<Conversation>> =
        repository.getConversations(query = query?.trim()?.takeIf { it.isNotEmpty() })
}

class SaveConversationsCacheUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversations: List<Conversation>) = repository.saveCachedConversations(conversations)
}

class GetCachedMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String): List<Message> = repository.getCachedMessages(conversationId)
}

class GetMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, beforeSeq: Long? = null): AppResult<List<Message>> =
        repository.getMessages(conversationId, beforeSeq = beforeSeq)
}

class SaveMessagesCacheUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, messages: List<Message>) =
        repository.saveCachedMessages(conversationId, messages)
}

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        conversationId: String,
        clientMessageId: String,
        content: String,
        type: String = "TEXT",
        replyToId: String? = null,
    ): AppResult<Message> = repository.sendMessage(conversationId, clientMessageId, content.trim(), type, replyToId)
}

class SendAiPromptUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, prompt: String, clientMessageId: String): AppResult<Message> =
        repository.aiChat(conversationId, prompt.trim(), clientMessageId)
}

class EditMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(messageId: String, content: String): AppResult<Message> =
        repository.editMessage(messageId, content.trim())
}

class DeleteMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(messageId: String): AppResult<Unit> = repository.deleteMessage(messageId)
}

class ToggleReactionUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: Message, emoji: String): AppResult<Unit> =
        if (message.hasUserReacted(emoji)) repository.removeReaction(message.id, emoji)
        else repository.addReaction(message.id, emoji)
}

class MarkConversationReadUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, upToSeq: Long): AppResult<Unit> =
        repository.markConversationRead(conversationId, upToSeq)
}

class GetUnreadCountUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): AppResult<Int> = repository.getUnreadCount()
}

class LeaveConversationUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String): AppResult<Unit> = repository.leaveConversation(conversationId)
}

class StartDirectMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(targetUserId: String): AppResult<Conversation> = repository.startDirectMessage(targetUserId)
}

class CreateGroupUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(title: String, memberIds: List<String>): AppResult<Conversation> =
        repository.createGroup(title.trim(), memberIds)
}

/** Cached conversation when present (keeps unread/mute/preview), otherwise the REST detail. */
class ResolveConversationUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String): AppResult<Conversation> =
        repository.getCachedConversations().firstOrNull { it.id == conversationId }?.let { Either.Right(it) }
            ?: repository.getConversation(conversationId)
}

/** Puts a created or updated conversation at the top of the cached list, replacing any copy by id. */
class MergeConversationIntoCacheUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversation: Conversation) {
        val cached = repository.getCachedConversations()
        val index = cached.indexOfFirst { it.id == conversation.id }
        val merged = if (index >= 0) cached.toMutableList().also { it[index] = conversation } else listOf(conversation) + cached
        repository.saveCachedConversations(merged)
    }
}

class GetGroupMembersUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String): AppResult<List<ConversationMember>> = repository.getMembers(conversationId)
}

class AddGroupMembersUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, memberIds: List<String>): AppResult<Unit> =
        if (memberIds.isEmpty()) Either.Right(Unit) else repository.addGroupMembers(conversationId, memberIds.distinct())
}

class RemoveGroupMemberUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(conversationId: String, userId: String): AppResult<Unit> = repository.removeMember(conversationId, userId)
}

/** Mutes for one year (Flutter `Duration(days: 365)`) or unmutes. */
class SetConversationMutedUseCase(
    private val repository: ChatRepository,
    private val now: () -> Instant = { Clock.System.now() },
) {
    suspend operator fun invoke(conversationId: String, muted: Boolean): AppResult<Unit> =
        if (muted) repository.muteConversation(conversationId, (now() + MUTE_DURATION).toString())
        else repository.unmuteConversation(conversationId)

    companion object {
        val MUTE_DURATION = 365.days
    }
}

/** The use cases the chat controller coordinates, injected as one unit. */
class ChatUseCaseBundle(
    val getIdentity: GetCurrentIdentityUseCase,
    val getCachedConversations: GetCachedConversationsUseCase,
    val getConversations: GetConversationsUseCase,
    val saveConversations: SaveConversationsCacheUseCase,
    val getCachedMessages: GetCachedMessagesUseCase,
    val getMessages: GetMessagesUseCase,
    val saveMessages: SaveMessagesCacheUseCase,
    val sendMessage: SendMessageUseCase,
    val sendAiPrompt: SendAiPromptUseCase,
    val editMessage: EditMessageUseCase,
    val deleteMessage: DeleteMessageUseCase,
    val toggleReaction: ToggleReactionUseCase,
    val markRead: MarkConversationReadUseCase,
    val getUnreadCount: GetUnreadCountUseCase,
    val leaveConversation: LeaveConversationUseCase,
) {
    companion object {
        fun from(repository: ChatRepository) = ChatUseCaseBundle(
            GetCurrentIdentityUseCase(repository),
            GetCachedConversationsUseCase(repository),
            GetConversationsUseCase(repository),
            SaveConversationsCacheUseCase(repository),
            GetCachedMessagesUseCase(repository),
            GetMessagesUseCase(repository),
            SaveMessagesCacheUseCase(repository),
            SendMessageUseCase(repository),
            SendAiPromptUseCase(repository),
            EditMessageUseCase(repository),
            DeleteMessageUseCase(repository),
            ToggleReactionUseCase(repository),
            MarkConversationReadUseCase(repository),
            GetUnreadCountUseCase(repository),
            LeaveConversationUseCase(repository),
        )
    }
}

/** Parses the `/ai` composer prefix. Mirrors `ChatNotifier.sendMessage`. */
object AiPrompt {
    private val prefix = Regex("^/ai\\s*", RegexOption.IGNORE_CASE)

    fun isAiCommand(text: String): Boolean = text.trim().lowercase().startsWith("/ai")

    fun strip(text: String): String = text.trim().replaceFirst(prefix, "").trim()
}

/** Who is typing, ignoring the local user and server echoes. Mirrors `_onRealtimeTypingReceived`. */
object TypingRules {
    fun isFromMe(
        me: CurrentIdentity,
        userId: String?,
        username: String?,
        displayName: String?,
    ): Boolean =
        (me.userId.isNotEmpty() && userId == me.userId) ||
            (me.username.isNotEmpty() && username != null && username.equals(me.username, ignoreCase = true)) ||
            (me.displayName.isNotEmpty() && displayName != null && displayName.equals(me.displayName, ignoreCase = true)) ||
            (displayName != null && displayName.equals("you", ignoreCase = true))

    fun hasSender(userId: String?, username: String?, displayName: String?): Boolean =
        !userId.isNullOrEmpty() || !username.isNullOrEmpty() || !displayName.isNullOrEmpty()
}
