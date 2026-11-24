package com.leochat.feature.chat.presentation.newgroup

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
 * Create a group: title, member search and selection, create button. Mirrors `NewGroupScreen`.
 *
 * @param onBack pops the screen.
 * @param onGroupCreated the group exists; the caller pops and opens/selects it.
 */
@Composable
fun NewGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
    controller: NewGroupViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    LaunchedEffect(controller) {
        controller.effects.collect { effect ->
            when (effect) {
                is ContactsEffect.ShowError -> snackBar.showError(effect.message)
                is ContactsEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is ContactsEffect.ConversationOpened -> onGroupCreated(effect.conversation)
            }
        }
    }

    NewGroupContent(
        state = state,
        onBack = onBack,
        onTitleChange = controller::setTitle,
        onSearch = controller::search,
        onToggleMember = controller::toggle,
        onCreate = controller::create,
        modifier = modifier,
    )
}
