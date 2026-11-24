package com.leochat.core.network.auth

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.platform.storage.SecureStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Token access for the network layer. Tokens live only in [SecureStorage]. */
class TokenStore(private val storage: SecureStorage) {
    suspend fun accessToken(): String? = storage.read(StorageKeys.accessToken)?.takeIf { it.isNotBlank() }
    suspend fun refreshToken(): String? = storage.read(StorageKeys.refreshToken)?.takeIf { it.isNotBlank() }

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        storage.write(StorageKeys.accessToken, accessToken)
        if (!refreshToken.isNullOrBlank()) storage.write(StorageKeys.refreshToken, refreshToken)
    }

    /** Removes tokens and cached identity. Mirrors `AuthSessionManager.clearLocalSession`. */
    suspend fun clearSession() {
        StorageKeys.sessionKeys.forEach { storage.delete(it) }
    }
}

/** Broadcasts forced sign-outs (refresh failed) so the app shell can route to login. */
class SessionEvents {
    private val expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = expired.asSharedFlow()

    fun notifyExpired() {
        expired.tryEmit(Unit)
    }
}
