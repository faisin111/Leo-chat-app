package com.leochat.feature.users.domain.usecase

import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem
import com.leochat.feature.users.domain.repository.UsersRepository

class GetMyProfileUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(): AppResult<UserProfile> = repository.getMe()
}

class GetUserProfileUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(id: String): AppResult<UserProfile> = repository.getUserById(id)
}

/** Blank queries return an empty list without a network call. */
class SearchUsersUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(query: String): AppResult<List<UserSearchItem>> {
        val q = query.trim()
        return if (q.isEmpty()) Either.Right(emptyList()) else repository.searchUsers(q)
    }
}

class UpdateProfileUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(update: ProfileUpdate): AppResult<Unit> = repository.updateProfile(update)
}

class GetSessionsUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(): AppResult<List<UserSessionItem>> = repository.getSessions()
}

class RevokeSessionUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(sessionId: String): AppResult<Unit> = repository.revokeSession(sessionId)
}

class DeactivateAccountUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.deactivateAccount()
}
