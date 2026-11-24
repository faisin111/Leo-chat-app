package com.leochat.feature.media.presentation.permission

import androidx.compose.runtime.Composable

enum class MediaPermission { Microphone, Camera }

/** Requests runtime media permissions; [onResult] gets true only when all are granted. */
fun interface MediaPermissionRequester {
    fun request(permissions: Set<MediaPermission>, onResult: (granted: Boolean) -> Unit)
}

/** Granted-always requester for previews, tests, and platforms without runtime prompts. */
val GrantedMediaPermissionRequester = MediaPermissionRequester { _, onResult -> onResult(true) }

@Composable
expect fun rememberMediaPermissionRequester(): MediaPermissionRequester
