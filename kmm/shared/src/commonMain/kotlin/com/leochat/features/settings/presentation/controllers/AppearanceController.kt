package com.leochat.features.settings.presentation.controllers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.theme.LeoThemeMode
import com.leochat.core.theme.ThemeModeStore
import com.leochat.features.settings.presentation.states.AppearanceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Theme mode (persisted via [ThemeModeStore]) and preview accent (local only, as in Flutter). */
class AppearanceController(
    private val themeStore: ThemeModeStore,
) : ViewModel() {
    private val accentIndex = MutableStateFlow(0)

    val state: StateFlow<AppearanceState> = combine(themeStore.mode, accentIndex) { mode, accent ->
        AppearanceState(mode = mode, accentIndex = accent)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceState(themeStore.mode.value, 0))

    fun setMode(mode: LeoThemeMode) = themeStore.setMode(mode)

    fun selectAccent(index: Int) {
        accentIndex.value = index
    }
}
