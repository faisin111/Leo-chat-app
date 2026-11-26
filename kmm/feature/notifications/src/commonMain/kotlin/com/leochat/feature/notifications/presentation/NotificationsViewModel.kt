package com.leochat.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.result.Either
import com.leochat.feature.notifications.domain.model.AppNotification
import com.leochat.feature.notifications.domain.repository.NotificationRepository
import com.leochat.feature.notifications.domain.usecase.NotificationUseCaseBundle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 30

/**
 * Notification Center state holder: cache-first hydration, background refresh,
 * read/type filtering, pagination, optimistic mark-read/delete, and STOMP live sync.
 */
class NotificationsViewModel(
    private val useCases: NotificationUseCaseBundle,
    private val repo: NotificationRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val cached = repo.cachedSnapshot()
            if (cached.items.isNotEmpty() || cached.unreadCount > 0) {
                mutableState.update {
                    it.copy(
                        items = cached.items,
                        unreadCount = cached.unreadCount,
                        totalElements = cached.items.size,
                        isLoading = false,
                    )
                }
            }
            refresh()
        }
        viewModelScope.launch {
            repo.realtimeTriggers().collect {
                refresh(showSpinner = false)
            }
        }
    }

    fun setReadFilter(filter: NotificationsReadFilter) {
        if (mutableState.value.readFilter == filter) return
        mutableState.update { it.copy(readFilter = filter, error = null) }
        refresh(showSpinner = mutableState.value.items.isEmpty())
    }

    fun setTypeFilter(filter: NotificationsTypeFilter) {
        mutableState.update { it.copy(typeFilter = filter) }
    }

    fun refresh(showSpinner: Boolean = false) {
        val current = mutableState.value
        val unreadOnly = current.readFilter == NotificationsReadFilter.Unread
        mutableState.update {
            it.copy(
                isLoading = current.items.isEmpty() && showSpinner,
                isRefreshing = showSpinner || current.items.isNotEmpty(),
                error = null,
            )
        }
        viewModelScope.launch {
            coroutineScope {
                val pageDeferred = async { useCases.getPage(page = 0, size = PAGE_SIZE, unreadOnly = unreadOnly) }
                val countDeferred = async { useCases.getUnreadCount() }
                val pageResult = pageDeferred.await()
                val countResult = countDeferred.await()

                val countUpdate = (countResult as? Either.Right)?.value
                when (pageResult) {
                    is Either.Right -> {
                        val p = pageResult.value
                        mutableState.update {
                            it.copy(
                                items = p.items,
                                totalElements = p.totalElements,
                                pageNumber = p.pageNumber,
                                hasMore = !p.isLast,
                                unreadCount = countUpdate ?: it.unreadCount,
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                            )
                        }
                    }
                    is Either.Left -> {
                        mutableState.update {
                            it.copy(
                                unreadCount = countUpdate ?: it.unreadCount,
                                isLoading = false,
                                isRefreshing = false,
                                error = pageResult.value.message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        val current = mutableState.value
        if (current.isLoadingMore || !current.hasMore) return
        val nextPage = current.pageNumber + 1
        val unreadOnly = current.readFilter == NotificationsReadFilter.Unread
        mutableState.update { it.copy(isLoadingMore = true, error = null) }

        viewModelScope.launch {
            when (val result = useCases.getPage(page = nextPage, size = PAGE_SIZE, unreadOnly = unreadOnly)) {
                is Either.Right -> {
                    val p = result.value
                    mutableState.update { state ->
                        val existingIds = state.items.mapTo(HashSet()) { it.id }
                        val merged = state.items + p.items.filterNot { it.id in existingIds }
                        state.copy(
                            items = merged,
                            totalElements = p.totalElements,
                            pageNumber = p.pageNumber,
                            hasMore = !p.isLast,
                            isLoadingMore = false,
                        )
                    }
                }
                is Either.Left -> {
                    mutableState.update {
                        it.copy(isLoadingMore = false, error = result.value.message)
                    }
                }
            }
        }
    }

    fun markRead(id: String) {
        val target = mutableState.value.items.firstOrNull { it.id == id }
        val wasUnread = target != null && !target.isRead
        mutableState.update { state ->
            val updatedItems = if (state.readFilter == NotificationsReadFilter.Unread) {
                state.items.filterNot { it.id == id }
            } else {
                state.items.map { item -> if (item.id == id) item.copy(isRead = true) else item }
            }
            state.copy(
                items = updatedItems,
                unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount,
                busyIds = state.busyIds + id,
            )
        }
        viewModelScope.launch {
            useCases.markRead(id)
            mutableState.update { it.copy(busyIds = it.busyIds - id) }
        }
    }

    fun markAllRead() {
        val current = mutableState.value
        if (current.unreadCount == 0 || current.isMarkingAllRead) return
        mutableState.update { state ->
            val updatedItems = if (state.readFilter == NotificationsReadFilter.Unread) {
                emptyList()
            } else {
                state.items.map { it.copy(isRead = true) }
            }
            state.copy(
                items = updatedItems,
                unreadCount = 0,
                totalElements = if (state.readFilter == NotificationsReadFilter.Unread) 0 else state.totalElements,
                isMarkingAllRead = true,
                error = null,
            )
        }
        viewModelScope.launch {
            val result = useCases.markAllRead()
            mutableState.update {
                it.copy(
                    isMarkingAllRead = false,
                    error = (result as? Either.Left)?.value?.message,
                )
            }
            if (result is Either.Left) refresh(showSpinner = false)
        }
    }

    fun delete(id: String) {
        val target = mutableState.value.items.firstOrNull { it.id == id }
        val wasUnread = target != null && !target.isRead
        mutableState.update { state ->
            state.copy(
                items = state.items.filterNot { it.id == id },
                unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount,
                totalElements = (state.totalElements - 1).coerceAtLeast(0),
                busyIds = state.busyIds + id,
            )
        }
        viewModelScope.launch {
            useCases.delete(id)
            mutableState.update { it.copy(busyIds = it.busyIds - id) }
        }
    }

    fun openNotification(
        item: AppNotification,
        onOpenConversation: (conversationId: String) -> Unit,
    ) {
        if (!item.isRead) markRead(item.id)
        item.conversationId?.takeIf { it.isNotBlank() }?.let(onOpenConversation)
    }

    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
}
