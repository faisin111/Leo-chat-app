package com.leochat.feature.auth.data.repository

import com.leochat.core.datastore.cache.CacheStore
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.core.network.push.PushTokenRegistry
import com.leochat.core.network.safeApiCall
import com.leochat.feature.auth.data.local.AuthLocalDataSource
import com.leochat.feature.auth.data.remote.AuthRemoteDataSource
import com.leochat.feature.auth.data.remote.LoginRequestDto
import com.leochat.feature.auth.data.remote.RegisterRequestDto
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
    private val cache: CacheStore,
    private val pushTokens: PushTokenRegistry? = null,
) : AuthRepository {

    override suspend fun login(username: String, password: String): AppResult<AuthSession> =
        persist(safeApiCall { remote.login(LoginRequestDto(username, password)).toDomain() })

    override suspend fun register(username: String, email: String, password: String, displayName: String): AppResult<AuthSession> =
        persist(safeApiCall { remote.register(RegisterRequestDto(username, email, password, displayName)).toDomain() })

    override suspend fun logout(): AppResult<Unit> {
        // Unregister push token while Bearer auth is still present; local session and cache always clear.
        runCatching { pushTokens?.unregisterCurrentToken() }
        safeApiCall { remote.logout() }
        clearLocal()
        return Either.Right(Unit)
    }

    override suspend fun logoutAll(): AppResult<Unit> {
        runCatching { pushTokens?.unregisterCurrentToken() }
        val result = safeApiCall { remote.logoutAll() }
        clearLocal()
        return result
    }

    override suspend fun getCurrentUser(): AppResult<User?> {
        if (local.accessToken() == null) return Either.Right(null)
        pushTokens?.syncInBackground()
        val cached = local.readUser()
        return when (val remoteUser = safeApiCall { remote.getMe().toDomain() }) {
            is Either.Right -> {
                local.saveUser(remoteUser.value)
                remoteUser
            }
            is Either.Left -> if (cached != null && remoteUser.value !is AppFailure.Unauthorized) Either.Right(cached) else remoteUser
        }
    }

    override suspend fun hasActiveSession(): Boolean = local.accessToken() != null

    override suspend fun forgotPassword(email: String) = safeApiCall { remote.forgotPassword(email) }
    override suspend fun resetPassword(token: String, newPassword: String) = safeApiCall { remote.resetPassword(token, newPassword) }
    override suspend fun changePassword(currentPassword: String, newPassword: String) =
        safeApiCall { remote.changePassword(currentPassword, newPassword) }
    override suspend fun verifyEmail(token: String) = safeApiCall { remote.verifyEmail(token) }
    override suspend fun resendVerification(email: String?) = safeApiCall { remote.resendVerification(email) }

    private suspend fun persist(result: AppResult<AuthSession>): AppResult<AuthSession> {
        if (result is Either.Right) {
            if (result.value.accessToken.isBlank()) return Either.Left(AppFailure.Unknown("Sign in response did not include a token."))
            local.saveSession(result.value)
            pushTokens?.syncInBackground()
        }
        return result
    }

    private suspend fun clearLocal() {
        local.readUser()?.let { cache.clearUserScope(it.id) }
        cache.clearAll()
        local.clear()
    }
}
