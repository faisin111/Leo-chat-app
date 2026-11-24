package com.leochat.feature.auth.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): AppResult<AuthSession>
    suspend fun register(username: String, email: String, password: String, displayName: String): AppResult<AuthSession>
    suspend fun logout(): AppResult<Unit>
    suspend fun logoutAll(): AppResult<Unit>
    suspend fun getCurrentUser(): AppResult<User?>
    suspend fun hasActiveSession(): Boolean
    suspend fun forgotPassword(email: String): AppResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit>
    suspend fun verifyEmail(token: String): AppResult<Unit>
    suspend fun resendVerification(email: String? = null): AppResult<Unit>
}
