package com.leochat.feature.notifications.data.local

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.datastore.cache.CacheKeys
import com.leochat.core.datastore.cache.CacheStore
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.feature.notifications.data.remote.CachedNotificationsDto

/** Per-user cache for notification center feed and unread badge count. */
class NotificationLocalDataSource(
    private val secureStorage: SecureStorage,
    private val cache: CacheStore,
) {
    suspend fun userId(): String = secureStorage.read(StorageKeys.userId).orEmpty()

    fun read(userId: String): CachedNotificationsDto =
        if (userId.isBlank()) CachedNotificationsDto()
        else cache.read(CacheKeys.notifications(userId), CachedNotificationsDto.serializer()) ?: CachedNotificationsDto()

    fun write(userId: String, snapshot: CachedNotificationsDto) {
        if (userId.isBlank()) return
        cache.write(CacheKeys.notifications(userId), CachedNotificationsDto.serializer(), snapshot)
    }
}
