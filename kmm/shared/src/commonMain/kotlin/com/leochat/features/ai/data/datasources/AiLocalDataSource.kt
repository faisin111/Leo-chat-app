package com.leochat.features.ai.data.datasources

import com.leochat.core.cache.CacheKeys
import com.leochat.core.cache.CacheStore
import com.leochat.core.constants.StorageKeys
import com.leochat.core.storage.SecureStorage
import com.leochat.features.ai.data.models.AiMessageDto
import kotlinx.serialization.builtins.ListSerializer

/** Per-user cache of the AI conversation. Skips caching when no user is signed in. */
class AiLocalDataSource(
    private val secureStorage: SecureStorage,
    private val cache: CacheStore,
) {
    private val serializer = ListSerializer(AiMessageDto.serializer())

    private suspend fun userId(): String? = secureStorage.read(StorageKeys.userId)?.takeIf { it.isNotBlank() }

    suspend fun messages(): List<AiMessageDto> {
        val user = userId() ?: return emptyList()
        return cache.read(CacheKeys.aiMessages(user), serializer).orEmpty()
    }

    suspend fun saveMessages(items: List<AiMessageDto>) {
        val user = userId() ?: return
        cache.write(CacheKeys.aiMessages(user), serializer, items)
    }

    suspend fun clear() {
        val user = userId() ?: return
        cache.remove(CacheKeys.aiMessages(user))
    }
}
