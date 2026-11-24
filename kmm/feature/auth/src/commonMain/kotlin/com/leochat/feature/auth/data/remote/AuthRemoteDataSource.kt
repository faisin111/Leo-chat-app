package com.leochat.feature.auth.data.remote

import com.leochat.feature.auth.data.remote.AuthResponseDto
import com.leochat.feature.auth.data.remote.ChangePasswordRequestDto
import com.leochat.feature.auth.data.remote.EmailRequestDto
import com.leochat.feature.auth.data.remote.LoginRequestDto
import com.leochat.feature.auth.data.remote.RegisterRequestDto
import com.leochat.feature.auth.data.remote.ResetPasswordRequestDto
import com.leochat.feature.auth.data.remote.TokenRequestDto
import com.leochat.feature.auth.data.remote.UserDto
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
