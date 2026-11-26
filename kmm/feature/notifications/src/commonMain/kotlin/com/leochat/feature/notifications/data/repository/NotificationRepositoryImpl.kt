package com.leochat.feature.notifications.data.repository

import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.network.safeApiCall
import com.leochat.core.network.string
import com.leochat.core.network.stomp.StompClient
import com.leochat.feature.notifications.data.local.NotificationLocalDataSource
import com.leochat.feature.notifications.data.remote.AppNotificationDto
import com.leochat.feature.notifications.data.remote.CachedNotificationsDto
import com.leochat.feature.notifications.data.remote.NotificationRemoteDataSource
import com.leochat.feature.notifications.domain.model.CachedNotificationsSnapshot
import com.leochat.feature.notifications.domain.model.NotificationsPage
import com.leochat.feature.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.json.JsonObject

private const val QueueNotifications = "/user/queue/notifications"
private const val QueueCalls = "/user/queue/calls"

/** REST + STOMP + user-scoped local cache repository for persistent notifications. */
class NotificationRepositoryImpl(
    private val remote: NotificationRemoteDataSource,
    private val local: NotificationLocalDataSource,
    private val stomp: StompClient,
) : NotificationRepository {

    override suspend fun cachedSnapshot(): CachedNotificationsSnapshot {
        val dto = local.read(local.userId())
        return CachedNotificationsSnapshot(
            items = dto.items.map { it.toDomain() },
            unreadCount = dto.unreadCount,
        )
    }

    override suspend fun list(page: Int, size: Int, unreadOnly: Boolean): AppResult<NotificationsPage> =
        when (val result = safeApiCall { remote.list(page, size, unreadOnly) }) {
            is Either.Left -> result
            is Either.Right -> {
                val (dtos, meta) = result.value
                val items = dtos.map { it.toDomain() }
                if (page == 0 && !unreadOnly) {
                    val userId = local.userId()
                    val existing = local.read(userId)
                    local.write(userId, existing.copy(items = dtos))
                }
                Either.Right(meta.toPage(items))
            }
        }

    override suspend fun unreadCount(): AppResult<Int> =
        when (val result = safeApiCall { remote.unreadCount() }) {
            is Either.Left -> result
            is Either.Right -> {
                val userId = local.userId()
                val existing = local.read(userId)
                local.write(userId, existing.copy(unreadCount = result.value))
                result
            }
        }

    override suspend fun markRead(id: String): AppResult<Unit> {
        val userId = local.userId()
        val cached = local.read(userId)
        val wasUnread = cached.items.any { it.id == id && !it.read }
        if (cached.items.isNotEmpty()) {
            val updated = cached.items.map { item ->
                if (item.id == id) item.copy(read = true) else item
            }
            val nextUnread = if (wasUnread) (cached.unreadCount - 1).coerceAtLeast(0) else cached.unreadCount
            local.write(userId, CachedNotificationsDto(updated, nextUnread))
        }
        return safeApiCall { remote.markRead(id) }
    }

    override suspend fun markAllRead(): AppResult<Int> {
        val userId = local.userId()
        val cached = local.read(userId)
        if (cached.items.isNotEmpty() || cached.unreadCount > 0) {
            local.write(userId, CachedNotificationsDto(cached.items.map { it.copy(read = true) }, 0))
        }
        return safeApiCall { remote.markAllRead() }
    }

    override suspend fun deleteNotification(id: String): AppResult<Unit> {
        val userId = local.userId()
        val cached = local.read(userId)
        val removed = cached.items.firstOrNull { it.id == id }
        if (removed != null) {
            val nextUnread = if (!removed.read) (cached.unreadCount - 1).coerceAtLeast(0) else cached.unreadCount
            local.write(userId, CachedNotificationsDto(cached.items.filterNot { it.id == id }, nextUnread))
        }
        return safeApiCall { remote.deleteNotification(id) }
    }

    override fun realtimeTriggers(): Flow<Unit> {
        val notifEvents = stomp.subscribe(QueueNotifications).map { }
        val callInviteEvents = stomp.subscribe(QueueCalls)
            .filter { frame ->
                val json = runCatching { LeoJson.parseToJsonElement(frame.body) as? JsonObject }.getOrNull()
                json?.string("action")?.trim()?.equals("INVITE", ignoreCase = true) == true
            }
            .map { }
        return merge(notifEvents, callInviteEvents)
    }
}
