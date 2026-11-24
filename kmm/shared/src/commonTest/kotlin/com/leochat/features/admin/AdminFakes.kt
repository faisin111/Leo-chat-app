package com.leochat.features.admin

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.repositories.AdminRepository
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.entities.UserSessionItem
import com.leochat.features.users.domain.repositories.UsersRepository

class FakeAdminRepository : AdminRepository {
    var stats: AppResult<AdminStats> = Either.Right(AdminStats(12, 34, 5))
    var users: AppResult<List<AdminUser>> = Either.Right(listOf(AdminUser("u1", "alex", "a@x.io")))
    var reports: AppResult<List<AdminReport>> = Either.Right(listOf(AdminReport("r1", "u9", targetId = "m1", reason = "SPAM")))
    var mutation: AppResult<Unit> = Either.Right(Unit)

    val userQueries = mutableListOf<Pair<String?, String?>>()
    val reportQueries = mutableListOf<String?>()
    val statusChanges = mutableListOf<Pair<String, String>>()
    val forceLogouts = mutableListOf<String>()
    val resolved = mutableListOf<Pair<String, String>>()
    val deletedMessages = mutableListOf<Pair<String, String>>()
    var statsCalls = 0

    override suspend fun getStatsOverview(): AppResult<AdminStats> {
        statsCalls++
        return stats
    }

    override suspend fun getUsers(query: String?, status: String?, cursor: Long?, limit: Int): AppResult<List<AdminUser>> {
        userQueries += query to status
        return users
    }

    override suspend fun updateUserStatus(userId: String, status: String, reason: String?): AppResult<Unit> {
        statusChanges += userId to status
        return mutation
    }

    override suspend fun forceLogoutUser(userId: String): AppResult<Unit> {
        forceLogouts += userId
        return mutation
    }

    override suspend fun deleteUser(userId: String): AppResult<Unit> = mutation

    override suspend fun getReports(status: String?, cursor: Long?): AppResult<List<AdminReport>> {
        reportQueries += status
        return reports
    }

    override suspend fun resolveReport(reportId: String, resolution: String, action: String?): AppResult<Unit> {
        resolved += reportId to resolution
        return mutation
    }

    override suspend fun deleteMessage(messageId: String, reason: String): AppResult<Unit> {
        deletedMessages += messageId to reason
        return mutation
    }
}

class FakeUsersRepository(var me: AppResult<UserProfile> = Either.Right(UserProfile("me", "root", role = "ROLE_ADMIN"))) : UsersRepository {
    override suspend fun getMe() = me
    override suspend fun getUserById(id: String): AppResult<UserProfile> = Either.Left(AppFailure.Unknown())
    override suspend fun searchUsers(query: String, page: Int, size: Int): AppResult<List<UserSearchItem>> = Either.Right(emptyList())
    override suspend fun updateProfile(update: ProfileUpdate): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getSessions(): AppResult<List<UserSessionItem>> = Either.Right(emptyList())
    override suspend fun revokeSession(sessionId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun deactivateAccount(): AppResult<Unit> = Either.Right(Unit)
}
