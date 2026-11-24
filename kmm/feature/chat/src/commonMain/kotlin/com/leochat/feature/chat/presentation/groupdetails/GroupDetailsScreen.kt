package com.leochat.feature.chat.presentation.groupdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.chat.presentation.ContactsEffect
import org.koin.compose.viewmodel.koinViewModel

/**
 * Group header, mute/media/add actions, shared media entry, and members. Mirrors `GroupDetailsScreen`.
 *
 * @param conversationId the group conversation to show.
 * @param onBack pops the screen.
 * @param onOpenSharedMedia opens the shared media screen for the given conversation id.
 */
@Composable
fun GroupDetailsScreen(
    conversationId: String,
    onBack: () -> Unit,
    onOpenSharedMedia: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    controller: GroupDetailsViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    LaunchedEffect(conversationId) { controller.load(conversationId) }
    LaunchedEffect(controller) {
        controller.effects.collect { effect ->
            when (effect) {
                is ContactsEffect.ShowError -> snackBar.showError(effect.message)
                is ContactsEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is ContactsEffect.ConversationOpened -> Unit
            }
        }
    }

    GroupDetailsContent(
        state = state,
        onBack = onBack,
        onOpenSharedMedia = { onOpenSharedMedia(conversationId) },
        onToggleMute = controller::toggleMute,
        onOpenAddSheet = controller::openAddSheet,
        onRemoveMember = controller::removeMember,
        onSearchToAdd = controller::searchToAdd,
        onAddMember = controller::addMember,
        onCloseAddSheet = controller::closeAddSheet,
        modifier = modifier,
    )
}
