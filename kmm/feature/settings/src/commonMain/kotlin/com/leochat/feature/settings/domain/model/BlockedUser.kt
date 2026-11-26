package com.leochat.feature.settings.domain.model

import com.leochat.core.common.constants.SettingsStrings
import kotlinx.datetime.Instant

/** User entry returned by `GET_BLOCKED_USERS` and enriched with `/users/{id}`. */
data class BlockedUserItem(
    val userId: String,
    val displayName: String,
    val username: String? = null,
    val avatarUrl: String? = null,
    val blockedAt: Instant? = null,
) {
    val effectiveName: String
        get() = displayName.trim().ifEmpty { username?.trim().orEmpty().ifEmpty { SettingsStrings.unknownUser } }
}
