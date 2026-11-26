package com.leochat.feature.users.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.model.UserPresence
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem

interface UsersRepository {
    suspend fun getMe(): AppResult<UserProfile>
    suspend fun getUserById(id: String): AppResult<UserProfile>
    suspend fun searchUsers(query: String, page: Int = 0, size: Int = 20): AppResult<List<UserSearchItem>>
    suspend fun updateProfile(update: ProfileUpdate): AppResult<Unit>
    suspend fun deleteProfilePicture(): AppResult<Unit>
    suspend fun getSessions(): AppResult<List<UserSessionItem>>
    suspend fun revokeSession(sessionId: String): AppResult<Unit>
    suspend fun deactivateAccount(): AppResult<Unit>
    suspend fun getPresence(ids: List<String>): AppResult<List<UserPresence>>
}
