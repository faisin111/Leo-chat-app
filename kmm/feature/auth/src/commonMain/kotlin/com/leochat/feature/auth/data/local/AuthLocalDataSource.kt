package com.leochat.feature.auth.data.local

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User

/** Persists the session (tokens + identity) in secure storage only. */
class AuthLocalDataSource(
    private val storage: SecureStorage,
    private val tokens: TokenStore,
) {
    suspend fun saveSession(session: AuthSession) {
        tokens.saveTokens(session.accessToken, session.refreshToken)
        storage.write(StorageKeys.tokenType, session.tokenType)
        saveUser(session.user)
    }

    suspend fun saveUser(user: User) {
        if (user.id.isNotEmpty()) storage.write(StorageKeys.userId, user.id)
        if (user.username.isNotEmpty()) storage.write(StorageKeys.username, user.username)
        storage.write(StorageKeys.email, user.email)
        storage.write(StorageKeys.displayName, user.displayName)
        storage.write(StorageKeys.role, user.role)
        storage.write(StorageKeys.status, user.status)
        user.avatarUrl?.let { storage.write(StorageKeys.avatarUrl, it) } ?: storage.delete(StorageKeys.avatarUrl)
    }

    suspend fun accessToken(): String? = tokens.accessToken()

    suspend fun readUser(): User? {
        val id = storage.read(StorageKeys.userId) ?: return null
        val username = storage.read(StorageKeys.username) ?: return null
        return User(
            id = id,
            username = username,
            email = storage.read(StorageKeys.email).orEmpty(),
            displayName = storage.read(StorageKeys.displayName) ?: username,
            avatarUrl = storage.read(StorageKeys.avatarUrl),
            role = storage.read(StorageKeys.role) ?: "USER",
            status = storage.read(StorageKeys.status) ?: "ACTIVE",
        )
    }

    suspend fun clear() = tokens.clearSession()
}
