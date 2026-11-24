package com.leochat.feature.users.data.remote

import com.leochat.feature.users.data.remote.UserDtoMapper
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** `/users` endpoints. Mirrors `UsersRemoteDataSourceImpl`. */
class UsersRemoteDataSource(private val http: HttpClient) {
    suspend fun getMe(): UserProfile = UserDtoMapper.profile(http.get("users/me").body<JsonObject>())

    suspend fun getUserById(id: String): UserProfile = UserDtoMapper.profile(http.get("users/$id").body<JsonObject>())

    suspend fun searchUsers(query: String, page: Int, size: Int): List<UserSearchItem> =
        http.get("users/search") {
            parameter("q", query.trim())
            parameter("page", page)
            parameter("size", size)
        }.body<JsonElement>().objects().map(UserDtoMapper::searchItem)

    suspend fun updateProfile(update: ProfileUpdate) {
        http.patch("users/me/profile") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    update.displayName?.let { put("displayName", it) }
                    update.profilePictureUrl?.let { put("profilePictureUrl", it) }
                    update.phoneNumber?.let { put("phoneNumber", it) }
                    update.bio?.let { put("bio", it) }
                    update.age?.let { put("age", it) }
                    update.region?.let { put("region", it) }
                },
            )
        }
    }

    suspend fun getSessions(): List<UserSessionItem> =
        http.get("users/me/sessions").body<JsonElement>().objects().map(UserDtoMapper::session)

    suspend fun revokeSession(sessionId: String) {
        http.delete("users/me/sessions/$sessionId")
    }

    suspend fun deactivateAccount() {
        http.delete("users/me")
    }

    /** Accepts a bare array or a paged `{items|content}` envelope. */
    private fun JsonElement.objects(): List<JsonObject> {
        val array = this as? JsonArray ?: (this as? JsonObject)?.let { (it["items"] ?: it["content"]) as? JsonArray }
        return array?.mapNotNull { it as? JsonObject }.orEmpty()
    }
}
