package com.leochat.feature.chat.presentation.newconversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.presentation.ContactsEffect
import org.koin.compose.viewmodel.koinViewModel

/**
 * New conversation screen and Contacts tab content. Mirrors `NewConversationScreen`.
 *
 * @param onBack pops the screen; `null` hides the back and close buttons (Contacts tab use).
 * @param onConversationOpened a direct chat was started or a recent chat tapped.
 * @param onNewGroup opens the new group screen.
 */
@Composable
fun NewConversationScreen(
    onBack: (() -> Unit)?,
    onConversationOpened: (Conversation) -> Unit,
    onNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
    controller: NewConversationViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    LaunchedEffect(controller) {
        controller.effects.collect { effect ->
            when (effect) {
                is ContactsEffect.ShowError -> snackBar.showError(effect.message)
                is ContactsEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is ContactsEffect.ConversationOpened -> onConversationOpened(effect.conversation)
            }
        }
    }

    NewConversationContent(
        state = state,
        onBack = onBack,
        onSearch = controller::search,
        onClearSearch = controller::clearSearch,
        onStartChat = controller::startChat,
        onNewGroup = onNewGroup,
        onOpenExisting = controller::openExisting,
        modifier = modifier,
    )
}
