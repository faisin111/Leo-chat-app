package com.leochat.core.theme

import com.leochat.core.constants.StorageKeys
import com.leochat.core.storage.KeyValueStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists the selected theme mode. Mirrors `ThemeModeNotifier`. Defaults to light. */
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
        "dark" -> LeoThemeMode.Dark
        "system" -> LeoThemeMode.System
        else -> LeoThemeMode.Light
    }
}
