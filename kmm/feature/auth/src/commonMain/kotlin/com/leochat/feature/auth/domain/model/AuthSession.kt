package com.leochat.feature.auth.domain.model

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
    val tokenType: String = "Bearer",
)
