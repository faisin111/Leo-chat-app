package com.leochat.features.users.domain.usecases

import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.entities.UserSessionItem
import com.leochat.features.users.domain.repositories.UsersRepository

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
