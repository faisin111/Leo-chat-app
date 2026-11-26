package com.leochat.feature.settings.data.remote

import com.leochat.core.common.util.parseInstant
import com.leochat.core.network.string
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class BlockedUserEntryDto(
    val userId: String,
    val blockedAt: Instant? = null,
)

/**
 * Fetches blocked user IDs via `POST /ai/actions` (`GET_BLOCKED_USERS`),
 * matching `BlockedUsersPage.tsx` on Web (`AiActionService.java`).
 */
class BlockedUsersRemoteDataSource(private val http: HttpClient) {
    suspend fun getBlockedUsers(): List<BlockedUserEntryDto> {
        val body = http.post("ai/actions") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("action", "GET_BLOCKED_USERS")
                },
            )
        }.body<JsonElement>()

        val array = when (body) {
            is JsonArray -> body
            is JsonObject -> body["data"] as? JsonArray
            else -> null
        } ?: return emptyList()

        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val userId = obj.string("userId")?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            BlockedUserEntryDto(
                userId = userId,
                blockedAt = parseInstant(obj.string("blockedAt")),
            )
        }
    }
}
