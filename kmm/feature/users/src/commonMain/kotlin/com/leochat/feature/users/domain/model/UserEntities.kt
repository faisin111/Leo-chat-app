package com.leochat.feature.users.domain.model

import com.leochat.core.common.constants.AppStrings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/** Live user presence returned by `/api/v1/users/presence`. */
data class UserPresence(
    val userId: String,
    val online: Boolean? = null,
    val status: String = "ACTIVE",
    val lastSeenAt: Instant? = null,
) {
    fun isOnline(now: Instant = Clock.System.now()): Boolean {
        if (!status.equals("ACTIVE", ignoreCase = true)) return false
        if (online != null) return online
        val seen = lastSeenAt ?: return false
        val diff = now - seen
        return !diff.isNegative() && diff < 5.minutes
    }

    fun statusLabel(now: Instant = Clock.System.now()): String {
        if (isOnline(now)) return AppStrings.activeNow
        val seen = lastSeenAt ?: return AppStrings.offline
        val diffMin = (now - seen).inWholeMinutes
        if (diffMin < 0) return AppStrings.offline
        return when {
            diffMin < 1L -> AppStrings.activeJustNow
            diffMin < 60L -> AppStrings.activeMinutesAgo(diffMin)
            diffMin < 24L * 60L -> AppStrings.activeHoursAgo(diffMin / 60L)
            else -> AppStrings.activeDaysAgo(diffMin / (24L * 60L))
        }
    }
}

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
    val profilePictureStorageKey: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val region: String? = null,
)
