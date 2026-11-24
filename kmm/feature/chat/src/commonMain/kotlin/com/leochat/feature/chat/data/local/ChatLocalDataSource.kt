package com.leochat.feature.chat.data.local

import com.leochat.core.datastore.cache.CacheKeys
import com.leochat.core.datastore.cache.CacheStore
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.feature.chat.data.remote.ConversationDto
import com.leochat.feature.chat.data.remote.MessageDto
import com.leochat.feature.chat.domain.model.CurrentIdentity
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
