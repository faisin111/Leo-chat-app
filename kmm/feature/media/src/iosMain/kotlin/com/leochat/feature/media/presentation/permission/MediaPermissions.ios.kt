package com.leochat.feature.media.presentation.permission

import androidx.compose.runtime.Composable

/**
 * iOS stub: grants immediately. AVAudioSession prompts on first microphone use;
 * add explicit `requestRecordPermission` handling when the iOS recorder lands.
 */
@Composable
actual fun rememberMediaPermissionRequester(): MediaPermissionRequester = GrantedMediaPermissionRequester
