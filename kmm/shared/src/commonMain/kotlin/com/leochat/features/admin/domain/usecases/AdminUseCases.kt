package com.leochat.features.admin.domain.usecases

import com.leochat.core.constants.AdminStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.repositories.AdminRepository
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase

/**
 * Resolves whether the signed-in user may open the admin console.
 * Returns `false` only when the profile loads and is not an admin; when the
 * profile cannot be loaded the admin API's own 403 remains the source of truth.
 */
class CheckAdminAccessUseCase(private val getMyProfile: GetMyProfileUseCase) {
    suspend operator fun invoke(): Boolean = when (val result = getMyProfile()) {
        is Either.Right -> result.value.isAdmin
        is Either.Left -> result.value !is AppFailure.Forbidden
    }
}

class GetAdminStatsUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(): AppResult<AdminStats> = repository.getStatsOverview()
}

class GetAdminUsersUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(query: String, status: String): AppResult<List<AdminUser>> =
        repository.getUsers(query = query, status = status)
}

class UpdateUserStatusUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(userId: String, status: String, reason: String? = null): AppResult<Unit> =
        repository.updateUserStatus(userId, status, reason)
}

class ForceLogoutUserUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(userId: String): AppResult<Unit> = repository.forceLogoutUser(userId)
}

class DeleteUserUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(userId: String): AppResult<Unit> = repository.deleteUser(userId)
}

class GetAdminReportsUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(status: String): AppResult<List<AdminReport>> = repository.getReports(status = status)
}

/** Blank resolution notes are rejected without a network call. */
class ResolveReportUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(reportId: String, resolution: String, action: String? = null): AppResult<Unit> {
        val note = resolution.trim()
        if (note.isEmpty()) return Either.Left(AppFailure.Validation(AdminStrings.resolutionRequired))
        return repository.resolveReport(reportId, note, action)
    }
}

class DeleteReportedMessageUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(messageId: String, reason: String): AppResult<Unit> =
        repository.deleteMessage(messageId, reason)
}
