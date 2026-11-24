package com.leochat.features.admin.domain.repositories

import com.leochat.core.errors.AppResult
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser

/** Admin console contract. A 403 surfaces as `AppFailure.Forbidden`. */
interface AdminRepository {
    suspend fun getStatsOverview(): AppResult<AdminStats>

    suspend fun getUsers(
        query: String? = null,
        status: String? = null,
        cursor: Long? = null,
        limit: Int = DEFAULT_USER_PAGE_SIZE,
    ): AppResult<List<AdminUser>>

    suspend fun updateUserStatus(userId: String, status: String, reason: String? = null): AppResult<Unit>

    suspend fun forceLogoutUser(userId: String): AppResult<Unit>

    suspend fun deleteUser(userId: String): AppResult<Unit>

    suspend fun getReports(status: String? = null, cursor: Long? = null): AppResult<List<AdminReport>>

    suspend fun resolveReport(reportId: String, resolution: String, action: String? = null): AppResult<Unit>

    suspend fun deleteMessage(messageId: String, reason: String): AppResult<Unit>

    companion object {
        const val DEFAULT_USER_PAGE_SIZE = 50
    }
}
