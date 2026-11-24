package com.leochat.feature.ai.presentation.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Leo AI tab. Mirrors `AiScreen`: model picker, capability toggles, action confirmation,
 * message list with generating indicator, prompt suggestions, and composer.
 * Embedded as-is in the shell on mobile (above the bottom nav) and tablet/desktop (beside the rail).
 */
@Composable
fun AiScreen(
    modifier: Modifier = Modifier,
    controller: AiViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    AiContent(
        state = state,
        onSend = { controller.sendMessage(it) },
        onSelectModel = controller::selectModel,
        onToggleDeepThink = controller::toggleDeepThink,
        onToggleWebSearch = controller::toggleWebSearch,
        onDeleteConversation = { controller.deleteConversation() },
        onConfirmAction = { controller.confirmPendingAction() },
        onCancelAction = controller::cancelPendingAction,
        modifier = modifier,
    )
}
