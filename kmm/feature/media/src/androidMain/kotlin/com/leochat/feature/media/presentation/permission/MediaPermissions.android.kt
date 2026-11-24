package com.leochat.feature.media.presentation.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private fun MediaPermission.androidName(): String = when (this) {
    MediaPermission.Microphone -> Manifest.permission.RECORD_AUDIO
    MediaPermission.Camera -> Manifest.permission.CAMERA
}

@Composable
actual fun rememberMediaPermissionRequester(): MediaPermissionRequester {
    val context = LocalContext.current
    val pending = remember { arrayOfNulls<(Boolean) -> Unit>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val callback = pending[0]
        pending[0] = null
        callback?.invoke(results.isNotEmpty() && results.values.all { it })
    }
    return remember(context, launcher) {
        MediaPermissionRequester { permissions, onResult ->
            val names = permissions.map { it.androidName() }
            val missing = names.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
            if (missing.isEmpty()) {
                onResult(true)
            } else {
                pending[0] = onResult
                launcher.launch(missing.toTypedArray())
            }
        }
    }
}
