package com.leochat.feature.settings.presentation.blocked

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BlockedUsersScreen(
    onBack: () -> Unit,
    controller: BlockedUsersViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    BlockedUsersContent(
        state = state,
        onBack = onBack,
        onRetry = controller::refresh,
    )
}
