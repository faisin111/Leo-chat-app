package com.leochat.feature.notifications

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.notifications.domain.model.AppNotification
import com.leochat.feature.notifications.domain.model.CachedNotificationsSnapshot
import com.leochat.feature.notifications.domain.model.NotificationType
import com.leochat.feature.notifications.domain.model.NotificationsPage
import com.leochat.feature.notifications.domain.repository.NotificationRepository
import com.leochat.feature.notifications.domain.usecase.DeleteNotificationUseCase
import com.leochat.feature.notifications.domain.usecase.GetNotificationUnreadCountUseCase
import com.leochat.feature.notifications.domain.usecase.GetNotificationsPageUseCase
import com.leochat.feature.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import com.leochat.feature.notifications.domain.usecase.MarkNotificationReadUseCase
import com.leochat.feature.notifications.domain.usecase.NotificationUseCaseBundle
import com.leochat.feature.notifications.presentation.NotificationsReadFilter
import com.leochat.feature.notifications.presentation.NotificationsTypeFilter
import com.leochat.feature.notifications.presentation.NotificationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    private fun TestScope.createVm(repo: FakeNotificationRepo): NotificationsViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return NotificationsViewModel(
            useCases = NotificationUseCaseBundle(
                getPage = GetNotificationsPageUseCase(repo),
                getUnreadCount = GetNotificationUnreadCountUseCase(repo),
                markRead = MarkNotificationReadUseCase(repo),
                markAllRead = MarkAllNotificationsReadUseCase(repo),
                delete = DeleteNotificationUseCase(repo),
            ),
            repo = repo,
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsCachedFirstThenRefreshesFiltersMarksReadAndDeletes() = runTest {
        val repo = FakeNotificationRepo()
        val vm = createVm(repo)
        advanceUntilIdle()

        val initial = vm.state.value
        assertFalse(initial.isLoading)
        assertEquals(3, initial.items.size)
        assertEquals(2, initial.unreadCount)

        // Type filter works client-side like Web
        vm.setTypeFilter(NotificationsTypeFilter.Calls)
        assertEquals(listOf("n3"), vm.state.value.visibleItems.map { it.id })
        vm.setTypeFilter(NotificationsTypeFilter.All)

        // Opening unread notification marks read & emits conversation ID
        var openedConv: String? = null
        vm.openNotification(initial.items[0]) { openedConv = it }
        advanceUntilIdle()
        assertEquals("conv-1", openedConv)
        assertEquals(1, vm.state.value.unreadCount)
        assertTrue(vm.state.value.items.first { it.id == "n1" }.isRead)

        // Deleting unread item updates list & decrements unread count
        vm.delete("n3")
        advanceUntilIdle()
        assertEquals(0, vm.state.value.unreadCount)
        assertEquals(listOf("n1", "n2"), vm.state.value.items.map { it.id })
    }

    @Test
    fun preservesCachedDataWhenRefreshFailsOffline() = runTest {
        val repo = FakeNotificationRepo(failList = true)
        val vm = createVm(repo)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(1, s.items.size)
        assertEquals("cached-1", s.items.single().id)
        assertEquals(1, s.unreadCount)
        assertEquals("Offline", s.error)
    }

    @Test
    fun markAllReadClearsUnreadFilterAndBadgeAndSyncsRealtime() = runTest {
        val repo = FakeNotificationRepo()
        val vm = createVm(repo)
        advanceUntilIdle()

        vm.setReadFilter(NotificationsReadFilter.Unread)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.items.size)

        vm.markAllRead()
        advanceUntilIdle()
        assertEquals(0, vm.state.value.unreadCount)
        assertTrue(vm.state.value.items.isEmpty())

        // Incoming realtime trigger refreshes automatically
        repo.seedUnread("n4")
        repo.triggers.emit(Unit)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.unreadCount)
        assertTrue(vm.state.value.items.any { it.id == "n4" })
    }

    private class FakeNotificationRepo(private val failList: Boolean = false) : NotificationRepository {
        val triggers = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
        private val now = Instant.parse("2026-11-26T05:00:00Z")
        private val store = mutableListOf(
            AppNotification("n1", NotificationType.NewMessage, "Maya", "Hello!", "m1", "conv-1", now, isRead = false),
            AppNotification("n2", NotificationType.MessageReaction, "Alex", "Reacted ❤️", "m2", "conv-2", now, isRead = true),
            AppNotification("n3", NotificationType.IncomingCall, "Incoming call", "Sam is calling you", "c1", "conv-3", now, isRead = false),
        )

        override suspend fun cachedSnapshot(): CachedNotificationsSnapshot = CachedNotificationsSnapshot(
            items = listOf(AppNotification("cached-1", NotificationType.NewMessage, "Cached", "Offline copy", createdAt = now, isRead = false)),
            unreadCount = 1,
        )

        override suspend fun list(page: Int, size: Int, unreadOnly: Boolean): AppResult<NotificationsPage> {
            if (failList) return Either.Left(AppFailure.Network("Offline"))
            val filtered = if (unreadOnly) store.filterNot { it.isRead } else store.toList()
            return Either.Right(NotificationsPage(filtered, filtered.size, 1, page, size, page == 0, true))
        }

        override suspend fun unreadCount(): AppResult<Int> =
            if (failList) Either.Left(AppFailure.Network("Offline"))
            else Either.Right(store.count { !it.isRead })

        override suspend fun markRead(id: String): AppResult<Unit> {
            val idx = store.indexOfFirst { it.id == id }
            if (idx >= 0) store[idx] = store[idx].copy(isRead = true)
            return Either.Right(Unit)
        }

        override suspend fun markAllRead(): AppResult<Int> {
            val changed = store.count { !it.isRead }
            for (i in store.indices) store[i] = store[i].copy(isRead = true)
            return Either.Right(changed)
        }

        override suspend fun deleteNotification(id: String): AppResult<Unit> {
            store.removeAll { it.id == id }
            return Either.Right(Unit)
        }

        fun seedUnread(id: String) {
            store.add(0, AppNotification(id, NotificationType.NewMessage, "New", "Realtime alert", conversationId = "conv-9", createdAt = now, isRead = false))
        }

        override fun realtimeTriggers(): Flow<Unit> = triggers
    }
}
