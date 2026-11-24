package com.leochat.features.users.domain.entities

import kotlinx.datetime.Instant

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val email: String? = null,
    val role: String? = null,
    val profilePictureUrl: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val region: String? = null,
    val isEmailVerified: Boolean = false,
) {
    val effectiveName: String get() = displayName?.trim()?.takeIf { it.isNotEmpty() } ?: username
    val isAdmin: Boolean get() = role?.uppercase().let { it == "ADMIN" || it == "ROLE_ADMIN" }
}

data class UserSearchItem(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val region: String? = null,
    val age: Int? = null,
    val status: String = "ACTIVE",
    val lastSeenAt: Instant? = null,
) {
    val effectiveName: String get() = displayName?.trim()?.takeIf { it.isNotEmpty() } ?: username
}

data class UserSessionItem(
    val sessionId: String,
    val createdAt: Instant,
    val lastUsedAt: Instant? = null,
    val expiresAt: Instant? = null,
    val logoutAt: Instant? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null,
    val deviceName: String? = null,
    val platform: String? = null,
    val appVersion: String? = null,
    val active: Boolean = true,
) {
    val isCurrentDevice: Boolean get() = active && logoutAt == null
}

/** Fields the profile editor may change; null means "leave unchanged". */
data class ProfileUpdate(
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val region: String? = null,
)
