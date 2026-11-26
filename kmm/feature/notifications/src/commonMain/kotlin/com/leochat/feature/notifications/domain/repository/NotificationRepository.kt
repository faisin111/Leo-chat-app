package com.leochat.feature.notifications.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.notifications.domain.model.CachedNotificationsSnapshot
import com.leochat.feature.notifications.domain.model.NotificationsPage
import kotlinx.coroutines.flow.Flow

/** Persistent notification center contract aligned with `/api/v1/notifications`. */
interface NotificationRepository {
    suspend fun cachedSnapshot(): CachedNotificationsSnapshot
    suspend fun list(page: Int = 0, size: Int = 30, unreadOnly: Boolean = false): AppResult<NotificationsPage>
    suspend fun unreadCount(): AppResult<Int>
    suspend fun markRead(id: String): AppResult<Unit>
    suspend fun markAllRead(): AppResult<Int>
    suspend fun deleteNotification(id: String): AppResult<Unit>
    fun realtimeTriggers(): Flow<Unit>
}
