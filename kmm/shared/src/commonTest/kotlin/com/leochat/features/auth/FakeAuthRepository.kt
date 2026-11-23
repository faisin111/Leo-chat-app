package com.leochat.features.auth

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.auth.domain.entities.AuthSession
import com.leochat.features.auth.domain.entities.User
import com.leochat.features.auth.domain.repositories.AuthRepository

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
