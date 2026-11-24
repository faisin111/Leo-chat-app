package com.leochat.features.admin.data.datasources

import com.leochat.features.admin.data.models.AdminDtoMapper
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminReportStatus
import com.leochat.features.admin.domain.entities.AdminStats
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.entities.AdminUserStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** `/admin` endpoints. Throws on transport/HTTP errors; the repository maps them. */
interface AdminRemoteDataSource {
    suspend fun getStatsOverview(): AdminStats
    suspend fun getUsers(query: String?, status: String?, cursor: Long?, limit: Int): List<AdminUser>
    suspend fun updateUserStatus(userId: String, status: String, reason: String?)
    suspend fun forceLogoutUser(userId: String)
    suspend fun deleteUser(userId: String)
    suspend fun getReports(status: String?, cursor: Long?): List<AdminReport>
    suspend fun resolveReport(reportId: String, resolution: String, action: String?)
    suspend fun deleteMessage(messageId: String, reason: String)
}

/** Mirrors `AdminRemoteDataSourceImpl` in Flutter, endpoint for endpoint. */
class AdminRemoteDataSourceImpl(private val http: HttpClient) : AdminRemoteDataSource {

    override suspend fun getStatsOverview(): AdminStats =
        AdminDtoMapper.stats(http.get("admin/stats/overview").body<JsonObject>())

    override suspend fun getUsers(query: String?, status: String?, cursor: Long?, limit: Int): List<AdminUser> {
        val envelope = http.get("admin/users") {
            parameter("limit", limit)
            query?.trim()?.takeIf { it.isNotEmpty() }?.let { parameter("q", it) }
            status?.takeIf { it != AdminUserStatus.ALL }?.let { parameter("status", it) }
            cursor?.let { parameter("cursor", it) }
        }.body<JsonObject>()
        return AdminDtoMapper.users(envelope)
    }

    override suspend fun updateUserStatus(userId: String, status: String, reason: String?) {
        http.patch("admin/users/$userId/status") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("status", status)
                    reason?.let { put("reason", it) }
                },
            )
        }
    }

    override suspend fun forceLogoutUser(userId: String) {
        http.post("admin/users/$userId/force-logout")
    }

    override suspend fun deleteUser(userId: String) {
        http.delete("admin/users/$userId")
    }

    override suspend fun getReports(status: String?, cursor: Long?): List<AdminReport> {
        val envelope = http.get("admin/reports") {
            status?.takeIf { it != AdminReportStatus.ALL }?.let { parameter("status", it) }
            cursor?.let { parameter("cursor", it) }
        }.body<JsonObject>()
        return AdminDtoMapper.reports(envelope)
    }

    override suspend fun resolveReport(reportId: String, resolution: String, action: String?) {
        http.patch("admin/reports/$reportId") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("resolution", resolution)
                    action?.let { put("action", it) }
                },
            )
        }
    }

    override suspend fun deleteMessage(messageId: String, reason: String) {
        http.delete("admin/messages/$messageId") { parameter("reason", reason) }
    }
}
