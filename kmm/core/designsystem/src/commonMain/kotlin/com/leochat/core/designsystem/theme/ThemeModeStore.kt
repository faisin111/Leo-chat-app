package com.leochat.core.designsystem.theme

import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.platform.storage.KeyValueStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists the selected theme mode. Mirrors `ThemeModeNotifier`. Defaults to dark (pure OLED). */
class ThemeModeStore(private val storage: KeyValueStorage) {
    private val mutableMode = MutableStateFlow(read())
    val mode: StateFlow<LeoThemeMode> = mutableMode.asStateFlow()

    fun setMode(mode: LeoThemeMode) {
        mutableMode.value = mode
        storage.putString(
            StorageKeys.themeMode,
            when (mode) {
                LeoThemeMode.Dark -> "dark"
                LeoThemeMode.Light -> "light"
                LeoThemeMode.System -> "system"
            },
        )
    }

    private fun read(): LeoThemeMode = when (storage.getString(StorageKeys.themeMode)) {
        "light" -> LeoThemeMode.Light
        "system" -> LeoThemeMode.System
        else -> LeoThemeMode.Dark
    }
}
