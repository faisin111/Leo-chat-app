package com.leochat.features.admin.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AdminStrings
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.admin.domain.entities.AdminUser
import com.leochat.features.admin.presentation.controllers.AdminUsersController
import com.leochat.features.admin.presentation.states.AdminUsersState
import com.leochat.features.admin.presentation.widgets.AdminCenteredMessage
import com.leochat.features.admin.presentation.widgets.AdminContentFrame
import com.leochat.features.admin.presentation.widgets.AdminErrorState
import com.leochat.features.admin.presentation.widgets.AdminFilterPills
import com.leochat.features.admin.presentation.widgets.AdminForbiddenState
import com.leochat.features.admin.presentation.widgets.AdminLoading
import com.leochat.features.admin.presentation.widgets.AdminSearchField
import com.leochat.features.admin.presentation.widgets.AdminTopBar
import com.leochat.features.admin.presentation.widgets.AdminUserActions
import com.leochat.features.admin.presentation.widgets.AdminUserActionsSheet
import com.leochat.features.admin.presentation.widgets.AdminUserTile
import com.leochat.features.admin.presentation.widgets.CollectAdminEffects
import org.koin.compose.viewmodel.koinViewModel

/** User management (`/admin/users`). Mirrors `AdminUsersPage`. */
@Composable
fun AdminUsersPage(
    onBack: () -> Unit,
    controller: AdminUsersController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAdminEffects(controller.effects)
    AdminUsersContent(
        state = state,
        onBack = onBack,
        onQueryChange = controller::onQueryChange,
        onSelectFilter = controller::selectFilter,
        onRetry = controller::retry,
        actions = AdminUserActions(
            onMarkActive = controller::markActive,
            onMarkDisabled = controller::markDisabled,
            onMarkBanned = controller::markBanned,
            onForceLogout = controller::forceLogout,
        ),
    )
}

@Composable
fun AdminUsersContent(
    state: AdminUsersState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (String) -> Unit,
    onRetry: () -> Unit,
    actions: AdminUserActions,
) {
    var sheetUser by remember { mutableStateOf<AdminUser?>(null) }
    LeoScreen(background = LeoThemeTokens.palette.background) {
        Column(Modifier.fillMaxSize()) {
            AdminTopBar(title = AdminStrings.userManagement, onBack = onBack)
            if (state.isForbidden) {
                AdminForbiddenState(onBack = onBack)
            } else {
                AdminContentFrame {
                    Column(Modifier.fillMaxSize()) {
                        AdminSearchField(
                            query = state.query,
                            onQueryChange = onQueryChange,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                        )
                        AdminFilterPills(
                            filters = state.filters,
                            selected = state.filter,
                            onSelect = onSelectFilter,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                            UsersList(state, onRetry, onOpen = { sheetUser = it })
                        }
                    }
                }
            }
        }
    }
    sheetUser?.let { user ->
        AdminUserActionsSheet(user = user, actions = actions, onDismiss = { sheetUser = null })
    }
}

@Composable
private fun UsersList(state: AdminUsersState, onRetry: () -> Unit, onOpen: (AdminUser) -> Unit) {
    when {
        state.isLoading -> AdminLoading()
        state.errorMessage != null -> AdminErrorState(AdminStrings.usersLoadFailedPrefix + state.errorMessage, onRetry)
        state.users.isEmpty() -> AdminCenteredMessage(AdminStrings.noUsersFound)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.users) { user ->
                AdminUserTile(user = user, onClick = { onOpen(user) })
            }
        }
    }
}
