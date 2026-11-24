package com.leochat.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.chat.presentation.ContactsEffect
import com.leochat.feature.chat.presentation.UserSearchUiState
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Base for contacts controllers: immutable state plus one-shot [ContactsEffect]s. */
abstract class ContactsEffectViewModel<S>(initial: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<ContactsEffect>(Channel.BUFFERED)
    val effects: Flow<ContactsEffect> = effectChannel.receiveAsFlow()

    protected fun update(transform: (S) -> S) = mutableState.update(transform)

    protected fun emit(effect: ContactsEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}

/**
 * Debounced people search shared by the contacts controllers. A new query cancels the
 * previous request; a blank query clears results without a network call.
 */
internal class UserSearchRunner(
    private val scope: CoroutineScope,
    private val searchUsers: SearchUsersUseCase,
    private val update: ((UserSearchUiState) -> UserSearchUiState) -> Unit,
) {
    private var job: Job? = null

    fun search(query: String) {
        job?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            update { UserSearchUiState(query = query) }
            return
        }
        update { it.copy(query = query, isLoading = true, errorMessage = null) }
        job = scope.launch {
            delay(DEBOUNCE_MILLIS)
            searchUsers(trimmed).fold(
                { failure -> update { it.copy(isLoading = false, results = emptyList(), errorMessage = failure.message) } },
                { users -> update { it.copy(isLoading = false, results = users, errorMessage = null) } },
            )
        }
    }

    fun clear() = search("")

    companion object {
        const val DEBOUNCE_MILLIS = 250L
    }
}
