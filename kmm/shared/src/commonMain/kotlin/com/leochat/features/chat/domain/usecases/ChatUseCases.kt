package com.leochat.features.chat.domain.usecases

import com.leochat.core.errors.AppResult
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.CurrentIdentity
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.repositories.ChatRepository

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
