package com.leochat.feature.notifications.data.remote

import com.leochat.core.network.int
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** REST data source for `/api/v1/notifications`. Throws on transport/HTTP errors; repository wraps in `safeApiCall`. */
interface NotificationRemoteDataSource {
    suspend fun list(page: Int, size: Int, unreadOnly: Boolean): Pair<List<AppNotificationDto>, NotificationsPageMeta>
    suspend fun unreadCount(): Int
    suspend fun markRead(id: String)
    suspend fun markAllRead(): Int
    suspend fun deleteNotification(id: String)
}

class NotificationRemoteDataSourceImpl(private val http: HttpClient) : NotificationRemoteDataSource {
    override suspend fun list(
        page: Int,
        size: Int,
        unreadOnly: Boolean,
    ): Pair<List<AppNotificationDto>, NotificationsPageMeta> {
        val element = http.get("notifications") {
            parameter("page", page)
            parameter("size", size)
            parameter("unreadOnly", unreadOnly)
        }.body<JsonElement>()
        return AppNotificationDto.pageFromJson(element)
    }

    override suspend fun unreadCount(): Int =
        http.get("notifications/unread-count").body<JsonObject>().int("unreadCount") ?: 0

    override suspend fun markRead(id: String) {
        http.patch("notifications/${id.encodeURLPathPart()}/read")
    }

    override suspend fun markAllRead(): Int =
        http.patch("notifications/read-all").body<JsonObject>().int("markedRead") ?: 0

    override suspend fun deleteNotification(id: String) {
        http.delete("notifications/${id.encodeURLPathPart()}")
    }
}
