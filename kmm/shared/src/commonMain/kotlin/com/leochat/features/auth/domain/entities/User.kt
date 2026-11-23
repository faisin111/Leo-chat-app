package com.leochat.features.auth.domain.entities

data class User(
    val id: String,
    val username: String,
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
) {
    val isAdmin: Boolean
        get() = role.uppercase() == "ROLE_ADMIN" || role.uppercase() == "ADMIN"

    val effectiveName: String
        get() = displayName.ifBlank { username }
}
