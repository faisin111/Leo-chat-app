package com.leochat.core.designsystem.component.call.model

/** Render-ready caller details for `IncomingCallBanner`. */
data class IncomingCallUi(
    val callerName: String,
    val callerAvatarUrl: String? = null,
    val isVideo: Boolean = false,
)
