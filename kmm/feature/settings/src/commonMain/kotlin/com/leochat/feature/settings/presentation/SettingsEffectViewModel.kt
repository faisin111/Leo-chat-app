package com.leochat.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Base for settings controllers: immutable state plus one-shot [SettingsEffect]s. */
abstract class SettingsEffectViewModel<S>(initial: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    protected fun update(transform: (S) -> S) = mutableState.update(transform)

    protected fun emit(effect: SettingsEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
