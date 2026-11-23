package com.leochat.features.auth.data.datasources

import com.leochat.features.auth.data.models.AuthResponseDto
import com.leochat.features.auth.data.models.ChangePasswordRequestDto
import com.leochat.features.auth.data.models.EmailRequestDto
import com.leochat.features.auth.data.models.LoginRequestDto
import com.leochat.features.auth.data.models.RegisterRequestDto
import com.leochat.features.auth.data.models.ResetPasswordRequestDto
import com.leochat.features.auth.data.models.TokenRequestDto
import com.leochat.features.auth.data.models.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

/** Auth REST endpoints. Throws; the repository maps errors to AppFailure. */
interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequestDto): AuthResponseDto
    suspend fun register(request: RegisterRequestDto): AuthResponseDto
    suspend fun getMe(): UserDto
    suspend fun logout()
    suspend fun logoutAll()
    suspend fun forgotPassword(email: String)
    suspend fun resetPassword(token: String, newPassword: String)
    suspend fun changePassword(currentPassword: String, newPassword: String)
    suspend fun verifyEmail(token: String)
    suspend fun resendVerification(email: String?)
}

class AuthRemoteDataSourceImpl(private val http: HttpClient) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequestDto): AuthResponseDto =
        AuthResponseDto.fromJson(http.post("auth/login") { json(request) }.body<JsonObject>())

    override suspend fun register(request: RegisterRequestDto): AuthResponseDto =
        AuthResponseDto.fromJson(http.post("auth/register") { json(request) }.body<JsonObject>())

    override suspend fun getMe(): UserDto = UserDto.fromJson(http.get("users/me").body<JsonObject>())

    override suspend fun logout() {
        http.post("auth/logout")
    }

    override suspend fun logoutAll() {
        http.post("auth/logout-all")
    }

    override suspend fun forgotPassword(email: String) {
        http.post("auth/forgot-password") { json(EmailRequestDto(email)) }
    }

    override suspend fun resetPassword(token: String, newPassword: String) {
        http.post("auth/reset-password") { json(ResetPasswordRequestDto(token, newPassword)) }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        http.post("auth/change-password") { json(ChangePasswordRequestDto(currentPassword, newPassword)) }
    }

    override suspend fun verifyEmail(token: String) {
        http.post("auth/verify-email") { json(TokenRequestDto(token)) }
    }

    override suspend fun resendVerification(email: String?) {
        http.post("auth/resend-verification") { if (email != null) json(EmailRequestDto(email)) }
    }

    private inline fun <reified T> io.ktor.client.request.HttpRequestBuilder.json(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
