package com.leochat.feature.auth.data.remote

import com.leochat.core.network.string
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class EmailRequestDto(val email: String)

@Serializable
data class TokenRequestDto(val token: String)

@Serializable
data class ResetPasswordRequestDto(val token: String, val newPassword: String)

@Serializable
data class ChangePasswordRequestDto(val currentPassword: String, val newPassword: String)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val status: String,
) {
    fun toDomain() = User(id, username, email, displayName, avatarUrl, role, status)

    companion object {
        /** Lenient parse matching `UserDto.fromJson`: accepts `avatar` and missing display names. */
        fun fromJson(json: JsonObject): UserDto {
            val username = json.string("username").orEmpty()
            return UserDto(
                id = json.string("id").orEmpty(),
                username = username,
                email = json.string("email").orEmpty(),
                displayName = json.string("displayName") ?: username,
                avatarUrl = json.string("avatarUrl") ?: json.string("avatar"),
                role = json.string("role") ?: "USER",
                status = json.string("status") ?: "ACTIVE",
            )
        }
    }
}

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val user: UserDto,
) {
    fun toDomain() = AuthSession(accessToken, refreshToken, user.toDomain(), tokenType)

    companion object {
        /** Accepts `accessToken`, `token`, or `jwt`, and a nested or flattened user. */
        fun fromJson(json: JsonObject): AuthResponseDto = AuthResponseDto(
            accessToken = json.string("accessToken") ?: json.string("token") ?: json.string("jwt").orEmpty(),
            refreshToken = json.string("refreshToken").orEmpty(),
            tokenType = json.string("tokenType") ?: "Bearer",
            user = UserDto.fromJson(json["user"] as? JsonObject ?: json),
        )
    }
}
