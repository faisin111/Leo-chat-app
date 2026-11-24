package com.leochat.feature.auth

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.auth.domain.repository.AuthRepository

class FakeAuthRepository(
    var loginResult: AppResult<AuthSession> = Either.Right(sampleSession),
    var hasSession: Boolean = false,
) : AuthRepository {
    val calls = mutableListOf<String>()

    override suspend fun login(username: String, password: String): AppResult<AuthSession> {
        calls += "login:$username"
        return loginResult
    }

    override suspend fun register(username: String, email: String, password: String, displayName: String): AppResult<AuthSession> {
        calls += "register:$username:$email:$displayName"
        return loginResult
    }

    override suspend fun logout(): AppResult<Unit> = Either.Right(Unit).also { calls += "logout" }
    override suspend fun logoutAll(): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getCurrentUser(): AppResult<User?> = Either.Right(sampleSession.user)
    override suspend fun hasActiveSession(): Boolean = hasSession
    override suspend fun forgotPassword(email: String): AppResult<Unit> = Either.Right(Unit).also { calls += "forgot:$email" }
    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> = Either.Right(Unit).also { calls += "reset:$token" }
    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun verifyEmail(token: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun resendVerification(email: String?): AppResult<Unit> = Either.Right(Unit)

    companion object {
        val sampleSession = AuthSession("access", "refresh", User("1", "alex", "alex@leo.chat", "Alex"))
        val apiFailure = Either.Left(AppFailure.Api("Invalid credentials", statusCode = 401))
    }
}
