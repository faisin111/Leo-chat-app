package com.leochat.features.admin

import com.leochat.core.constants.AdminStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.Either
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminReportStatus
import com.leochat.features.admin.domain.entities.AdminReportTarget
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.entities.AdminUserStatus
import com.leochat.features.admin.domain.usecases.CheckAdminAccessUseCase
import com.leochat.features.admin.domain.usecases.DeleteReportedMessageUseCase
import com.leochat.features.admin.domain.usecases.ForceLogoutUserUseCase
import com.leochat.features.admin.domain.usecases.GetAdminReportsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminStatsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminUsersUseCase
import com.leochat.features.admin.domain.usecases.ResolveReportUseCase
import com.leochat.features.admin.domain.usecases.UpdateUserStatusUseCase
import com.leochat.features.admin.presentation.controllers.AdminDashboardController
import com.leochat.features.admin.presentation.controllers.AdminReportsController
import com.leochat.features.admin.presentation.controllers.AdminUsersController
import com.leochat.features.admin.presentation.states.AdminEffect
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdminControllersTest {
    private val repo = FakeAdminRepository()
    private val users = FakeUsersRepository()
    private val access get() = CheckAdminAccessUseCase(GetMyProfileUseCase(users))

    private fun TestScope.main() = Dispatchers.setMain(StandardTestDispatcher(testScheduler))

    private fun TestScope.dashboard(): AdminDashboardController {
        main()
        return AdminDashboardController(access, GetAdminStatsUseCase(repo))
    }

    private fun TestScope.usersController(): AdminUsersController {
        main()
        return AdminUsersController(access, GetAdminUsersUseCase(repo), UpdateUserStatusUseCase(repo), ForceLogoutUserUseCase(repo))
    }

    private fun TestScope.reportsController(): AdminReportsController {
        main()
        return AdminReportsController(access, GetAdminReportsUseCase(repo), ResolveReportUseCase(repo), DeleteReportedMessageUseCase(repo))
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // Dashboard

    @Test
    fun dashboardLoadsStats() = runTest {
        val c = dashboard()
        assertTrue(c.state.value.isLoading)
        advanceUntilIdle()
        val s = c.state.value
        assertFalse(s.isLoading)
        assertEquals(12, s.stats?.totalUsers)
        assertEquals(34, s.stats?.totalConversations)
        assertEquals(5, s.stats?.totalReports)
    }

    @Test
    fun dashboardShowsErrorThenRecoversOnRefresh() = runTest {
        repo.stats = Either.Left(AppFailure.Network("offline"))
        val c = dashboard()
        advanceUntilIdle()
        assertEquals("offline", c.state.value.errorMessage)
        assertNull(c.state.value.stats)

        repo.stats = Either.Right(com.leochat.features.admin.domain.entities.AdminStats(1, 2, 3))
        c.refresh()
        advanceUntilIdle()
        assertNull(c.state.value.errorMessage)
        assertEquals(3, c.state.value.stats?.totalReports)
    }

    @Test
    fun refreshKeepsExistingStatsVisible() = runTest {
        val c = dashboard()
        advanceUntilIdle()
        c.refresh()
        assertTrue(c.state.value.isRefreshing)
        assertFalse(c.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(c.state.value.isRefreshing)
        assertEquals(2, repo.statsCalls)
    }

    @Test
    fun nonAdminIsForbiddenWithoutCallingAdminApi() = runTest {
        users.me = Either.Right(UserProfile("me", "joe", role = "ROLE_USER"))
        val c = dashboard()
        advanceUntilIdle()
        assertTrue(c.state.value.isForbidden)
        assertFalse(c.state.value.isLoading)
        assertEquals(0, repo.statsCalls)
    }

    @Test
    fun api403MarksDashboardForbidden() = runTest {
        repo.stats = Either.Left(AppFailure.Forbidden("nope"))
        val c = dashboard()
        advanceUntilIdle()
        assertTrue(c.state.value.isForbidden)
    }

    @Test
    fun profileFailureDefersToAdminApi() = runTest {
        users.me = Either.Left(AppFailure.Network())
        val c = dashboard()
        advanceUntilIdle()
        assertFalse(c.state.value.isForbidden)
        assertEquals(12, c.state.value.stats?.totalUsers)
    }

    // Users

    @Test
    fun usersLoadWithDefaultFilter() = runTest {
        val c = usersController()
        advanceUntilIdle()
        assertEquals(listOf("u1"), c.state.value.users.map { it.id })
        assertEquals(listOf<Pair<String?, String?>>("" to AdminUserStatus.ALL), repo.userQueries)
    }

    @Test
    fun filterChangeReloadsAndSameFilterIsIgnored() = runTest {
        val c = usersController()
        advanceUntilIdle()
        c.selectFilter(AdminUserStatus.BANNED)
        assertTrue(c.state.value.isLoading)
        advanceUntilIdle()
        c.selectFilter(AdminUserStatus.BANNED)
        advanceUntilIdle()
        assertEquals(AdminUserStatus.BANNED, c.state.value.filter)
        assertEquals(2, repo.userQueries.size)
        assertEquals(AdminUserStatus.BANNED, repo.userQueries.last().second)
    }

    @Test
    fun searchIsDebounced() = runTest {
        val c = usersController()
        advanceUntilIdle()
        c.onQueryChange("a")
        c.onQueryChange("al")
        c.onQueryChange("ali")
        advanceTimeBy(AdminUsersController.SEARCH_DEBOUNCE_MILLIS - 1)
        assertEquals(1, repo.userQueries.size)
        advanceUntilIdle()
        assertEquals(2, repo.userQueries.size)
        assertEquals("ali", repo.userQueries.last().first)
        assertEquals("ali", c.state.value.query)
    }

    @Test
    fun emptyAndErrorStates() = runTest {
        repo.users = Either.Right(emptyList())
        val c = usersController()
        advanceUntilIdle()
        assertTrue(c.state.value.isEmpty)

        repo.users = Either.Left(AppFailure.Api("boom"))
        c.retry()
        advanceUntilIdle()
        assertEquals("boom", c.state.value.errorMessage)
        assertFalse(c.state.value.isEmpty)
    }

    @Test
    fun statusChangeEmitsSuccessAndReloads() = runTest {
        val c = usersController()
        advanceUntilIdle()
        val user = AdminUser("u1", "alex", "a@x.io")
        c.markBanned(user)
        advanceUntilIdle()
        assertEquals(listOf("u1" to AdminUserStatus.BANNED), repo.statusChanges)
        assertEquals(AdminEffect.ShowSuccess(AdminStrings.userMarkedBanned), c.effects.first())
        assertEquals(2, repo.userQueries.size)
    }

    @Test
    fun forceLogoutFailureEmitsError() = runTest {
        repo.mutation = Either.Left(AppFailure.Api("denied"))
        val c = usersController()
        advanceUntilIdle()
        c.forceLogout(AdminUser("u1", "alex", "a@x.io"))
        advanceUntilIdle()
        assertEquals(listOf("u1"), repo.forceLogouts)
        assertEquals(AdminEffect.ShowError("denied"), c.effects.first())
        assertEquals(1, repo.userQueries.size)
    }

    @Test
    fun forbiddenMutationFlipsUsersScreen() = runTest {
        val c = usersController()
        advanceUntilIdle()
        repo.mutation = Either.Left(AppFailure.Forbidden("no"))
        c.markActive(AdminUser("u1", "alex", "a@x.io", status = AdminUserStatus.BANNED))
        advanceUntilIdle()
        assertTrue(c.state.value.isForbidden)
    }

    // Reports

    @Test
    fun reportsLoadAndFilter() = runTest {
        val c = reportsController()
        advanceUntilIdle()
        assertEquals(listOf("r1"), c.state.value.reports.map { it.id })
        c.selectFilter(AdminReportStatus.OPEN)
        advanceUntilIdle()
        assertEquals(listOf<String?>(AdminReportStatus.ALL, AdminReportStatus.OPEN), repo.reportQueries)
    }

    @Test
    fun blankResolutionIsIgnored() = runTest {
        val c = reportsController()
        advanceUntilIdle()
        assertFalse(c.resolve(AdminReport("r1", "u9", targetId = "m1"), "   "))
        advanceUntilIdle()
        assertTrue(repo.resolved.isEmpty())
    }

    @Test
    fun resolveSendsTrimmedNoteAndReloads() = runTest {
        val c = reportsController()
        advanceUntilIdle()
        assertTrue(c.resolve(AdminReport("r1", "u9", targetId = "m1"), "  Warning issued "))
        advanceUntilIdle()
        assertEquals(listOf("r1" to "Warning issued"), repo.resolved)
        assertEquals(AdminEffect.ShowSuccess(AdminStrings.reportResolved), c.effects.first())
        assertEquals(2, repo.reportQueries.size)
    }

    @Test
    fun deleteMessageUsesModeratorReasonOnlyForMessageTargets() = runTest {
        val c = reportsController()
        advanceUntilIdle()
        c.deleteMessage(AdminReport("r2", "u9", targetType = AdminReportTarget.USER, targetId = "u5"))
        c.deleteMessage(AdminReport("r1", "u9", targetId = "m1", reason = "SPAM"))
        advanceUntilIdle()
        assertEquals(listOf("m1" to AdminStrings.deletedByModerator("SPAM")), repo.deletedMessages)
        assertEquals(AdminEffect.ShowSuccess(AdminStrings.reportedMessageDeleted), c.effects.first())
    }

    @Test
    fun reports403IsForbidden() = runTest {
        repo.reports = Either.Left(AppFailure.Forbidden("no"))
        val c = reportsController()
        advanceUntilIdle()
        assertTrue(c.state.value.isForbidden)
        assertFalse(c.state.value.isLoading)
    }
}
