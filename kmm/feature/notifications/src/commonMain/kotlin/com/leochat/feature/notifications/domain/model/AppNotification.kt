package com.leochat.feature.notifications.domain.model

import kotlinx.datetime.Instant

enum class NotificationType(val wireValue: String) {
    NewMessage("NEW_MESSAGE"),
    MessageReaction("MESSAGE_REACTION"),
    IncomingCall("INCOMING_CALL"),
    Unknown("UNKNOWN");

    companion object {
        fun fromWire(value: String?): NotificationType = when (value?.trim()?.uppercase()) {
            "NEW_MESSAGE" -> NewMessage
            "MESSAGE_REACTION" -> MessageReaction
            "INCOMING_CALL" -> IncomingCall
            else -> Unknown
        }
    }
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val referenceId: String? = null,
    val conversationId: String? = null,
    val createdAt: Instant? = null,
    val readAt: Instant? = null,
    val isRead: Boolean = readAt != null,
)

data class NotificationsPage(
    val items: List<AppNotification>,
    val totalElements: Int,
    val totalPages: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
)

data class CachedNotificationsSnapshot(
    val items: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
)
