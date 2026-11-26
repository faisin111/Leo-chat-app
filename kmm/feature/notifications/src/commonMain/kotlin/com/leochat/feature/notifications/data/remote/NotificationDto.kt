package com.leochat.feature.notifications.data.remote

import com.leochat.core.common.util.parseInstant
import com.leochat.core.network.array
import com.leochat.core.network.bool
import com.leochat.core.network.int
import com.leochat.core.network.obj
import com.leochat.core.network.objects
import com.leochat.core.network.string
import com.leochat.feature.notifications.domain.model.AppNotification
import com.leochat.feature.notifications.domain.model.NotificationType
import com.leochat.feature.notifications.domain.model.NotificationsPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class AppNotificationDto(
    val id: String,
    val type: String = "NEW_MESSAGE",
    val title: String = "",
    val body: String = "",
    val referenceId: String? = null,
    val conversationId: String? = null,
    val createdAt: String? = null,
    val readAt: String? = null,
    val read: Boolean = readAt != null,
) {
    fun toDomain() = AppNotification(
        id = id,
        type = NotificationType.fromWire(type),
        title = title,
        body = body,
        referenceId = referenceId,
        conversationId = conversationId,
        createdAt = parseInstant(createdAt),
        readAt = parseInstant(readAt),
        isRead = read || readAt != null,
    )

    companion object {
        fun fromDomain(n: AppNotification) = AppNotificationDto(
            id = n.id,
            type = n.type.wireValue,
            title = n.title,
            body = n.body,
            referenceId = n.referenceId,
            conversationId = n.conversationId,
            createdAt = n.createdAt?.toString(),
            readAt = n.readAt?.toString(),
            read = n.isRead,
        )

        fun fromJson(json: JsonObject): AppNotificationDto {
            val readAt = json.string("readAt")
            return AppNotificationDto(
                id = json.string("id").orEmpty(),
                type = json.string("type") ?: "NEW_MESSAGE",
                title = json.string("title").orEmpty(),
                body = json.string("body").orEmpty(),
                referenceId = json.string("referenceId"),
                conversationId = json.string("conversationId"),
                createdAt = json.string("createdAt"),
                readAt = readAt,
                read = json.bool("read") ?: (readAt != null),
            )
        }

        fun pageFromJson(element: JsonElement): Pair<List<AppNotificationDto>, NotificationsPageMeta> {
            if (element is JsonArray) {
                val items = element.objects().map(::fromJson)
                return items to NotificationsPageMeta(
                    totalElements = items.size,
                    totalPages = if (items.isNotEmpty()) 1 else 0,
                    pageNumber = 0,
                    pageSize = items.size,
                    isFirst = true,
                    isLast = true,
                )
            }
            val obj = element as? JsonObject ?: return emptyList<AppNotificationDto>() to NotificationsPageMeta()
            val nestedPage = obj.obj("page")
            val content = (obj.array("content") ?: obj.array("items") ?: obj.array("data")).objects().map(::fromJson)
            val number = obj.int("number") ?: nestedPage?.int("number") ?: 0
            val size = obj.int("size") ?: nestedPage?.int("size") ?: content.size
            val totalElements = obj.int("totalElements") ?: nestedPage?.int("totalElements") ?: content.size
            val totalPages = obj.int("totalPages") ?: nestedPage?.int("totalPages") ?: if (content.isNotEmpty()) 1 else 0
            val isLast = obj.bool("last") ?: (totalPages == 0 || number + 1 >= totalPages)
            val isFirst = obj.bool("first") ?: (number == 0)
            return content to NotificationsPageMeta(
                totalElements = totalElements,
                totalPages = totalPages,
                pageNumber = number,
                pageSize = size,
                isFirst = isFirst,
                isLast = isLast,
            )
        }
    }
}

data class NotificationsPageMeta(
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val isFirst: Boolean = true,
    val isLast: Boolean = true,
) {
    fun toPage(items: List<AppNotification>) = NotificationsPage(
        items = items,
        totalElements = totalElements,
        totalPages = totalPages,
        pageNumber = pageNumber,
        pageSize = pageSize,
        isFirst = isFirst,
        isLast = isLast,
    )
}

@Serializable
data class CachedNotificationsDto(
    val items: List<AppNotificationDto> = emptyList(),
    val unreadCount: Int = 0,
)
