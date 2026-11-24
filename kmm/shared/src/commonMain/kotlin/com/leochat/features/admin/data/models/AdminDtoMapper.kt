package com.leochat.features.admin.data.models

import com.leochat.core.network.array
import com.leochat.core.network.int
import com.leochat.core.network.objects
import com.leochat.core.network.string
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminReportReason
import com.leochat.features.admin.domain.entities.AdminReportStatus
import com.leochat.features.admin.domain.entities.AdminReportTarget
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.entities.AdminUserRole
import com.leochat.features.admin.domain.entities.AdminUserStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

/** Lenient JSON → entity mapping. Mirrors the parsing in `AdminRemoteDataSourceImpl`. */
object AdminDtoMapper {
    private const val ITEMS = "items"

    fun stats(json: JsonObject) = AdminStats(
        totalUsers = json.int("totalUsers") ?: 0,
        totalConversations = json.int("totalConversations") ?: 0,
        totalReports = json.int("totalReports") ?: 0,
    )

    fun user(json: JsonObject) = AdminUser(
        id = json.string("id").orEmpty(),
        username = json.string("username").orEmpty(),
        email = json.string("email").orEmpty(),
        displayName = json.string("displayName"),
        role = json.string("role") ?: AdminUserRole.USER,
        status = json.string("status") ?: AdminUserStatus.ACTIVE,
        profilePictureUrl = json.string("profilePictureUrl"),
        createdAt = instant(json.string("createdAt")),
        lastSeenAt = instant(json.string("lastSeenAt")),
    )

    fun report(json: JsonObject) = AdminReport(
        id = json.string("id").orEmpty(),
        reporterId = json.string("reporterId").orEmpty(),
        targetType = json.string("targetType") ?: AdminReportTarget.MESSAGE,
        targetId = json.string("targetId").orEmpty(),
        reason = json.string("reason") ?: AdminReportReason.OTHER,
        details = json.string("details"),
        status = json.string("status") ?: AdminReportStatus.OPEN,
        createdAt = instant(json.string("createdAt")),
        resolvedAt = instant(json.string("resolvedAt")),
        resolvedBy = json.string("resolvedBy"),
        resolution = json.string("resolution"),
    )

    /** Paged `{ "items": [...] }` envelope; anything else yields an empty list. */
    fun users(envelope: JsonObject): List<AdminUser> = envelope.array(ITEMS).objects().map(::user)

    fun reports(envelope: JsonObject): List<AdminReport> = envelope.array(ITEMS).objects().map(::report)

    private fun instant(value: String?): Instant? = value?.let { runCatching { Instant.parse(it) }.getOrNull() }
}
