package com.leochat.features.users.domain.repositories

import com.leochat.core.errors.AppResult
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.entities.UserSessionItem

interface UsersRepository {
    suspend fun getMe(): AppResult<UserProfile>
    suspend fun getUserById(id: String): AppResult<UserProfile>
    suspend fun searchUsers(query: String, page: Int = 0, size: Int = 20): AppResult<List<UserSearchItem>>
    suspend fun updateProfile(update: ProfileUpdate): AppResult<Unit>
    suspend fun getSessions(): AppResult<List<UserSessionItem>>
    suspend fun revokeSession(sessionId: String): AppResult<Unit>
    suspend fun deactivateAccount(): AppResult<Unit>
}
