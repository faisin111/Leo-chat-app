package com.leochat.feature.splash.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.auth.domain.usecase.HasActiveSessionUseCase
import com.leochat.feature.splash.presentation.SplashDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Holds the splash for three seconds, then reads the secure token
 * (500 ms budget) and picks the next route. Mirrors `SplashScreen._finishSplash`.
 */
class SplashViewModel(
    private val hasActiveSession: HasActiveSessionUseCase,
    private val holdMillis: Long = 3_000,
    private val tokenTimeoutMillis: Long = 500,
) : ViewModel() {
    private val mutableDestination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = mutableDestination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(holdMillis)
            val signedIn = withTimeoutOrNull(tokenTimeoutMillis) { hasActiveSession() } ?: false
            mutableDestination.value = if (signedIn) SplashDestination.Home else SplashDestination.Onboarding
        }
    }
}
