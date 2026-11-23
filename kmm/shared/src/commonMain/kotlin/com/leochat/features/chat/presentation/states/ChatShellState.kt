package com.leochat.features.chat.presentation.states

import com.leochat.features.auth.domain.entities.User

data class ChatShellState(
    val user: User? = null,
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false,
)
