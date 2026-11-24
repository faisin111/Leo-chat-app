package com.leochat.features.admin.domain.entities

import kotlinx.datetime.Instant

/** Platform-wide counters from `/admin/stats/overview`. */
data class AdminStats(
    val totalUsers: Int = 0,
    val totalConversations: Int = 0,
    val totalReports: Int = 0,
)

/** Account as seen by the admin console. Mirrors `AdminUser` in Flutter. */
data class AdminUser(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String? = null,
    val role: String = AdminUserRole.USER,
    val status: String = AdminUserStatus.ACTIVE,
    val profilePictureUrl: String? = null,
    val createdAt: Instant? = null,
    val lastSeenAt: Instant? = null,
) {
    val effectiveName: String get() = displayName?.trim()?.takeIf { it.isNotEmpty() } ?: username
    val isActive: Boolean get() = status == AdminUserStatus.ACTIVE
    val isDisabled: Boolean get() = status == AdminUserStatus.DISABLED
    val isBanned: Boolean get() = status == AdminUserStatus.BANNED
    val isAdmin: Boolean get() = role == AdminUserRole.ADMIN
}

/** User or content report awaiting moderation. Mirrors `AdminReport` in Flutter. */
data class AdminReport(
    val id: String,
    val reporterId: String,
    val targetType: String = AdminReportTarget.MESSAGE,
    val targetId: String,
    val reason: String = AdminReportReason.OTHER,
    val details: String? = null,
    val status: String = AdminReportStatus.OPEN,
    val createdAt: Instant? = null,
    val resolvedAt: Instant? = null,
    val resolvedBy: String? = null,
    val resolution: String? = null,
) {
    val isOpen: Boolean get() = status == AdminReportStatus.OPEN
    val isResolved: Boolean get() = status == AdminReportStatus.RESOLVED
    val isMessageTarget: Boolean get() = targetType == AdminReportTarget.MESSAGE
    val hasDetails: Boolean get() = !details.isNullOrEmpty()
}

/** Backend role values. */
object AdminUserRole {
    const val USER = "ROLE_USER"
    const val ADMIN = "ROLE_ADMIN"
}

/** Backend account status values; [ALL] is the client-side "no filter" value. */
object AdminUserStatus {
    const val ALL = "ALL"
    const val ACTIVE = "ACTIVE"
    const val DISABLED = "DISABLED"
    const val BANNED = "BANNED"

    val filters = listOf(ALL, ACTIVE, DISABLED, BANNED)
}

/** Backend report status values; [ALL] is the client-side "no filter" value. */
object AdminReportStatus {
    const val ALL = "ALL"
    const val OPEN = "OPEN"
    const val INVESTIGATING = "INVESTIGATING"
    const val RESOLVED = "RESOLVED"
    const val DISMISSED = "DISMISSED"

    val filters = listOf(ALL, OPEN, RESOLVED, DISMISSED)
}

object AdminReportTarget {
    const val MESSAGE = "MESSAGE"
    const val USER = "USER"
    const val CONVERSATION = "CONVERSATION"
}

object AdminReportReason {
    const val OTHER = "OTHER"
}
