package com.leochat.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.auth.presentation.AuthEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Base for auth controllers: immutable state plus one-shot effects. */
abstract class EffectViewModel<S>(initial: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<AuthEffect>(Channel.BUFFERED)
    val effects: Flow<AuthEffect> = effectChannel.receiveAsFlow()

    protected fun update(transform: (S) -> S) = mutableState.update(transform)

    protected fun emit(effect: AuthEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
