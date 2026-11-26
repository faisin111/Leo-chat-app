package com.leochat.feature.notifications.presentation

import com.leochat.feature.notifications.domain.model.AppNotification
import com.leochat.feature.notifications.domain.model.NotificationType

enum class NotificationsReadFilter { All, Unread }

enum class NotificationsTypeFilter { All, Messages, Reactions, Calls }

data class NotificationsUiState(
    val readFilter: NotificationsReadFilter = NotificationsReadFilter.All,
    val typeFilter: NotificationsTypeFilter = NotificationsTypeFilter.All,
    val items: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val totalElements: Int = 0,
    val pageNumber: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val busyIds: Set<String> = emptySet(),
    val error: String? = null,
) {
    val visibleItems: List<AppNotification>
        get() = items.filter { item ->
            val matchesRead = readFilter == NotificationsReadFilter.All || !item.isRead
            val matchesType = when (typeFilter) {
                NotificationsTypeFilter.All -> true
                NotificationsTypeFilter.Messages -> item.type == NotificationType.NewMessage
                NotificationsTypeFilter.Reactions -> item.type == NotificationType.MessageReaction
                NotificationsTypeFilter.Calls -> item.type == NotificationType.IncomingCall
            }
            matchesRead && matchesType
        }
}
