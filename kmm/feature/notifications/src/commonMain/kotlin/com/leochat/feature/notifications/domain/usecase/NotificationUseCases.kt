package com.leochat.feature.notifications.domain.usecase

import com.leochat.feature.notifications.domain.repository.NotificationRepository

class GetNotificationsPageUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(page: Int = 0, size: Int = 30, unreadOnly: Boolean = false) =
        repo.list(page = page, size = size, unreadOnly = unreadOnly)
}

class GetNotificationUnreadCountUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke() = repo.unreadCount()
}

class MarkNotificationReadUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(id: String) = repo.markRead(id)
}

class MarkAllNotificationsReadUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke() = repo.markAllRead()
}

class DeleteNotificationUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(id: String) = repo.deleteNotification(id)
}

data class NotificationUseCaseBundle(
    val getPage: GetNotificationsPageUseCase,
    val getUnreadCount: GetNotificationUnreadCountUseCase,
    val markRead: MarkNotificationReadUseCase,
    val markAllRead: MarkAllNotificationsReadUseCase,
    val delete: DeleteNotificationUseCase,
)
