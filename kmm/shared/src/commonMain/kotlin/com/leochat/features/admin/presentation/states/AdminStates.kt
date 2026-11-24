package com.leochat.features.admin.presentation.states

import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminReportStatus
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.entities.AdminUserStatus

/** One-shot UI events (snack bars) emitted by admin controllers. */
sealed interface AdminEffect {
    data class ShowSuccess(val message: String) : AdminEffect
    data class ShowError(val message: String) : AdminEffect
}

/**
 * Screen-level status shared by every admin page.
 * [isForbidden] wins over everything else: the user is not an admin or the API answered 403.
 */
interface AdminScreenStatus {
    val isLoading: Boolean
    val errorMessage: String?
    val isForbidden: Boolean
}

data class AdminDashboardState(
    val stats: AdminStats? = null,
    override val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    override val errorMessage: String? = null,
    override val isForbidden: Boolean = false,
) : AdminScreenStatus

data class AdminUsersState(
    val query: String = "",
    val filter: String = AdminUserStatus.ALL,
    val users: List<AdminUser> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null,
    override val isForbidden: Boolean = false,
) : AdminScreenStatus {
    val filters: List<String> get() = AdminUserStatus.filters
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && users.isEmpty()
}

data class AdminReportsState(
    val filter: String = AdminReportStatus.ALL,
    val reports: List<AdminReport> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null,
    override val isForbidden: Boolean = false,
) : AdminScreenStatus {
    val filters: List<String> get() = AdminReportStatus.filters
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && reports.isEmpty()
}
