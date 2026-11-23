package com.leochat.core.constants

/** Keys for secure storage (tokens and identity) and preferences. */
object StorageKeys {
    const val accessToken = "accessToken"
    const val refreshToken = "refreshToken"
    const val tokenType = "tokenType"
    const val userId = "userId"
    const val username = "username"
    const val email = "email"
    const val displayName = "displayName"
    const val avatarUrl = "avatarUrl"
    const val role = "role"
    const val status = "status"

    val sessionKeys = listOf(accessToken, refreshToken, tokenType, userId, username, email, displayName, avatarUrl, role, status)

    const val themeMode = "leo_theme_mode"
}
