package com.leochat.features.admin.presentation.controllers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.AdminStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.domain.entities.AdminUserStatus
import com.leochat.features.admin.domain.usecases.CheckAdminAccessUseCase
import com.leochat.features.admin.domain.usecases.DeleteReportedMessageUseCase
import com.leochat.features.admin.domain.usecases.ForceLogoutUserUseCase
import com.leochat.features.admin.domain.usecases.GetAdminReportsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminStatsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminUsersUseCase
import com.leochat.features.admin.domain.usecases.ResolveReportUseCase
import com.leochat.features.admin.domain.usecases.UpdateUserStatusUseCase
import com.leochat.features.admin.presentation.states.AdminDashboardState
import com.leochat.features.admin.presentation.states.AdminEffect
import com.leochat.features.admin.presentation.states.AdminReportsState
import com.leochat.features.admin.presentation.states.AdminUsersState
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

/** Base for admin controllers: immutable state, one-shot effects, and the admin access gate. */
abstract class AdminController<S>(
    initial: S,
    private val checkAccess: CheckAdminAccessUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<AdminEffect>(Channel.BUFFERED)
    val effects: Flow<AdminEffect> = effectChannel.receiveAsFlow()

    /** Cached once per controller; `null` until the first check completes. */
    private var hasAccess: Boolean? = null

    protected fun update(transform: (S) -> S) = mutableState.update(transform)

    protected fun emit(effect: AdminEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    /** Runs [block] only when the signed-in user is an admin; otherwise marks the screen forbidden. */
    protected suspend fun withAccess(block: suspend () -> Unit) {
        val allowed = hasAccess ?: checkAccess().also { hasAccess = it }
        if (allowed) block() else onForbidden()
    }

    protected abstract fun onForbidden()

    /** Emits the outcome of a mutation; a 403 also flips the screen into the forbidden state. */
    protected fun AppResult<Unit>.report(successMessage: String, onSuccess: () -> Unit = {}) = fold(
        ifLeft = { failure ->
            if (failure is AppFailure.Forbidden) onForbidden()
            emit(AdminEffect.ShowError(failure.message))
        },
        ifRight = {
            emit(AdminEffect.ShowSuccess(successMessage))
            onSuccess()
        },
    )
}

class AdminDashboardController(
    checkAccess: CheckAdminAccessUseCase,
    private val getStats: GetAdminStatsUseCase,
) : AdminController<AdminDashboardState>(AdminDashboardState(), checkAccess) {

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        update { it.copy(isLoading = it.stats == null, isRefreshing = it.stats != null, errorMessage = null) }
        viewModelScope.launch {
            withAccess {
                getStats().fold(
                    ifLeft = { failure ->
                        if (failure is AppFailure.Forbidden) {
                            onForbidden()
                        } else {
                            update { it.copy(isLoading = false, isRefreshing = false, errorMessage = failure.message) }
                        }
                    },
                    ifRight = { stats ->
                        update { it.copy(stats = stats, isLoading = false, isRefreshing = false, errorMessage = null) }
                    },
                )
            }
        }
    }

    override fun onForbidden() = update { it.copy(isForbidden = true, isLoading = false, isRefreshing = false) }
}

class AdminUsersController(
    checkAccess: CheckAdminAccessUseCase,
    private val getUsers: GetAdminUsersUseCase,
    private val updateUserStatus: UpdateUserStatusUseCase,
    private val forceLogoutUser: ForceLogoutUserUseCase,
) : AdminController<AdminUsersState>(AdminUsersState(), checkAccess) {

    private var loadJob: Job? = null

    init {
        load(showSpinner = true)
    }

    /** Search refreshes after a short pause so each keystroke does not hit the API. */
    fun onQueryChange(value: String) {
        update { it.copy(query = value) }
        load(showSpinner = true, debounceMillis = SEARCH_DEBOUNCE_MILLIS)
    }

    fun selectFilter(filter: String) {
        if (filter == state.value.filter) return
        update { it.copy(filter = filter) }
        load(showSpinner = true)
    }

    fun retry() = load(showSpinner = true)

    fun markActive(user: AdminUser) = changeStatus(user, AdminUserStatus.ACTIVE, AdminStrings.userMarkedActive)
    fun markDisabled(user: AdminUser) = changeStatus(user, AdminUserStatus.DISABLED, AdminStrings.userMarkedDisabled)
    fun markBanned(user: AdminUser) = changeStatus(user, AdminUserStatus.BANNED, AdminStrings.userMarkedBanned)

    fun forceLogout(user: AdminUser) {
        viewModelScope.launch {
            forceLogoutUser(user.id).report(AdminStrings.forceLoggedOut)
        }
    }

    private fun changeStatus(user: AdminUser, status: String, successMessage: String) {
        viewModelScope.launch {
            updateUserStatus(user.id, status).report(successMessage) { load(showSpinner = false) }
        }
    }

    private fun load(showSpinner: Boolean, debounceMillis: Long = 0) {
        loadJob?.cancel()
        if (showSpinner) update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            if (debounceMillis > 0) delay(debounceMillis)
            withAccess {
                val s = state.value
                getUsers(s.query, s.filter).fold(
                    ifLeft = { failure ->
                        if (failure is AppFailure.Forbidden) {
                            onForbidden()
                        } else {
                            update { it.copy(isLoading = false, errorMessage = failure.message) }
                        }
                    },
                    ifRight = { users -> update { it.copy(users = users, isLoading = false, errorMessage = null) } },
                )
            }
        }
    }

    override fun onForbidden() = update { it.copy(isForbidden = true, isLoading = false) }

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}

class AdminReportsController(
    checkAccess: CheckAdminAccessUseCase,
    private val getReports: GetAdminReportsUseCase,
    private val resolveReport: ResolveReportUseCase,
    private val deleteReportedMessage: DeleteReportedMessageUseCase,
) : AdminController<AdminReportsState>(AdminReportsState(), checkAccess) {

    private var loadJob: Job? = null

    init {
        load(showSpinner = true)
    }

    fun selectFilter(filter: String) {
        if (filter == state.value.filter) return
        update { it.copy(filter = filter) }
        load(showSpinner = true)
    }

    fun retry() = load(showSpinner = true)

    /** Returns `false` (and does nothing) for a blank note, matching Flutter's silent guard. */
    fun resolve(report: AdminReport, resolution: String): Boolean {
        if (resolution.isBlank()) return false
        viewModelScope.launch {
            resolveReport(report.id, resolution).report(AdminStrings.reportResolved) { load(showSpinner = false) }
        }
        return true
    }

    fun deleteMessage(report: AdminReport) {
        if (!report.isMessageTarget) return
        viewModelScope.launch {
            deleteReportedMessage(report.targetId, AdminStrings.deletedByModerator(report.reason))
                .report(AdminStrings.reportedMessageDeleted) { load(showSpinner = false) }
        }
    }

    private fun load(showSpinner: Boolean) {
        loadJob?.cancel()
        if (showSpinner) update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            withAccess {
                getReports(state.value.filter).fold(
                    ifLeft = { failure ->
                        if (failure is AppFailure.Forbidden) {
                            onForbidden()
                        } else {
                            update { it.copy(isLoading = false, errorMessage = failure.message) }
                        }
                    },
                    ifRight = { reports -> update { it.copy(reports = reports, isLoading = false, errorMessage = null) } },
                )
            }
        }
    }

    override fun onForbidden() = update { it.copy(isForbidden = true, isLoading = false) }
}
