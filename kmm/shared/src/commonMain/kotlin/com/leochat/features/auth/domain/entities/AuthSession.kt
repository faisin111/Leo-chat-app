package com.leochat.features.auth.domain.entities

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
    val tokenType: String = "Bearer",
)
