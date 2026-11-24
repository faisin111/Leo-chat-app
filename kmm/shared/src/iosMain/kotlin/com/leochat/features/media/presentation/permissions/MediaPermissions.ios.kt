package com.leochat.features.media.presentation.permissions

import androidx.compose.runtime.Composable

/**
 * iOS stub: grants immediately. AVAudioSession prompts on first microphone use;
 * add explicit `requestRecordPermission` handling when the iOS recorder lands.
 */
@Composable
actual fun rememberMediaPermissionRequester(): MediaPermissionRequester = GrantedMediaPermissionRequester
