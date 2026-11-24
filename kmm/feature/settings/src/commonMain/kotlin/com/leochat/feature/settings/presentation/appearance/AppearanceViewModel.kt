package com.leochat.feature.settings.presentation.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.core.designsystem.theme.ThemeModeStore
import com.leochat.feature.settings.presentation.settings.AppearanceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Theme mode (persisted via [ThemeModeStore]) and preview accent (local only, as in Flutter). */
class AppearanceViewModel(
    private val themeStore: ThemeModeStore,
) : ViewModel() {
    private val accentIndex = MutableStateFlow(0)

    val state: StateFlow<AppearanceUiState> = combine(themeStore.mode, accentIndex) { mode, accent ->
        AppearanceUiState(mode = mode, accentIndex = accent)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceUiState(themeStore.mode.value, 0))

    fun setMode(mode: LeoThemeMode) = themeStore.setMode(mode)

    fun selectAccent(index: Int) {
        accentIndex.value = index
    }
}
