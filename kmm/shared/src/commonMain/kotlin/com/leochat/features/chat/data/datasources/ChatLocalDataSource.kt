package com.leochat.features.chat.data.datasources

import com.leochat.core.cache.CacheKeys
import com.leochat.core.cache.CacheStore
import com.leochat.core.constants.StorageKeys
import com.leochat.core.storage.SecureStorage
import com.leochat.features.chat.data.models.ConversationDto
import com.leochat.features.chat.data.models.MessageDto
import com.leochat.features.chat.domain.entities.CurrentIdentity
import kotlinx.serialization.builtins.ListSerializer

/** Signed-in identity (secure storage) and per-user chat cache. */
class ChatLocalDataSource(
    private val secureStorage: SecureStorage,
    private val cache: CacheStore,
) {
    suspend fun identity(): CurrentIdentity = CurrentIdentity(
        userId = secureStorage.read(StorageKeys.userId).orEmpty(),
        username = secureStorage.read(StorageKeys.username).orEmpty(),
        displayName = secureStorage.read(StorageKeys.displayName).orEmpty(),
    )

    fun conversations(userId: String): List<ConversationDto> =
        cache.read(CacheKeys.conversations(userId), ListSerializer(ConversationDto.serializer())).orEmpty()

    fun saveConversations(userId: String, items: List<ConversationDto>) =
        cache.write(CacheKeys.conversations(userId), ListSerializer(ConversationDto.serializer()), items)

    fun messages(userId: String, conversationId: String): List<MessageDto> =
        cache.read(CacheKeys.messages(userId, conversationId), ListSerializer(MessageDto.serializer())).orEmpty()

    fun saveMessages(userId: String, conversationId: String, items: List<MessageDto>) =
        cache.write(CacheKeys.messages(userId, conversationId), ListSerializer(MessageDto.serializer()), items.sortedBy { it.seq })
}
