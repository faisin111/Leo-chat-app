package com.leochat.features.admin.data.repositories

import com.leochat.core.network.safeApiCall
import com.leochat.features.admin.data.datasources.AdminRemoteDataSource
import com.leochat.features.admin.domain.repositories.AdminRepository

/** Network-only: admin data is moderation-sensitive and never cached. */
class AdminRepositoryImpl(private val remote: AdminRemoteDataSource) : AdminRepository {
    override suspend fun getStatsOverview() = safeApiCall { remote.getStatsOverview() }

    override suspend fun getUsers(query: String?, status: String?, cursor: Long?, limit: Int) =
        safeApiCall { remote.getUsers(query, status, cursor, limit) }

    override suspend fun updateUserStatus(userId: String, status: String, reason: String?) =
        safeApiCall { remote.updateUserStatus(userId, status, reason) }

    override suspend fun forceLogoutUser(userId: String) = safeApiCall { remote.forceLogoutUser(userId) }

    override suspend fun deleteUser(userId: String) = safeApiCall { remote.deleteUser(userId) }

    override suspend fun getReports(status: String?, cursor: Long?) = safeApiCall { remote.getReports(status, cursor) }

    override suspend fun resolveReport(reportId: String, resolution: String, action: String?) =
        safeApiCall { remote.resolveReport(reportId, resolution, action) }

    override suspend fun deleteMessage(messageId: String, reason: String) =
        safeApiCall { remote.deleteMessage(messageId, reason) }
}
