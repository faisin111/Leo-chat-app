package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.presentation.controllers.NewConversationController
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.NewConversationState
import com.leochat.features.chat.presentation.widgets.ContactsCenteredPane
import com.leochat.features.chat.presentation.widgets.ContactsDivider
import com.leochat.features.chat.presentation.widgets.ContactsEmptyState
import com.leochat.features.chat.presentation.widgets.ContactsIconButton
import com.leochat.features.chat.presentation.widgets.ContactsLoader
import com.leochat.features.chat.presentation.widgets.ContactsSearchPill
import com.leochat.features.chat.presentation.widgets.ContactsTitle
import com.leochat.features.chat.presentation.widgets.ContactsTopBar
import com.leochat.features.chat.presentation.widgets.CreateGroupCard
import com.leochat.features.chat.presentation.widgets.RecentContactsSection
import com.leochat.features.chat.presentation.widgets.UserResultRow
import org.koin.compose.viewmodel.koinViewModel

/**
 * New conversation screen and Contacts tab content. Mirrors `NewConversationPage`.
 *
 * @param onBack pops the screen; `null` hides the back and close buttons (Contacts tab use).
 * @param onConversationOpened a direct chat was started or a recent chat tapped.
 * @param onNewGroup opens the new group screen.
 */
@Composable
fun NewConversationPage(
    onBack: (() -> Unit)?,
    onConversationOpened: (Conversation) -> Unit,
    onNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
    controller: NewConversationController = koinViewModel(),
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
    val insets = if (onBack != null) Modifier.safeDrawingPadding() else Modifier.imePadding()

    Box(modifier.fillMaxSize().background(LeoColors.background)) {
        Column(Modifier.fillMaxSize().then(insets)) {
            ContactsTopBar(
                onBack = onBack,
                actions = {
                    if (onBack != null) {
                        ContactsIconButton(Icons.Rounded.Close, ContactsStrings.close, onBack)
                        Spacer(Modifier.width(8.dp))
                    }
                },
            ) { ContactsTitle(ContactsStrings.newConversation) }
            ContactsSearchPill(
                value = state.search.query,
                onValueChange = controller::search,
                placeholder = ContactsStrings.searchByNameOrUsername,
                onClear = controller::clearSearch,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            if (state.isStartingChat) {
                LinearProgressIndicator(color = LeoColors.primary, trackColor = Color.Transparent, modifier = Modifier.fillMaxWidth().height(2.dp))
            }
            Box(Modifier.weight(1f)) {
                if (state.search.isActive) {
                    SearchResults(state, controller)
                } else {
                    DefaultContent(state, onNewGroup, controller::openExisting)
                }
            }
        }
    }
}

@Composable
private fun SearchResults(state: NewConversationState, controller: NewConversationController) {
    val search = state.search
    when {
        search.isLoading -> ContactsCenteredPane { ContactsLoader() }
        search.errorMessage != null -> ContactsCenteredPane {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = LeoColors.destructive, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(ContactsStrings.searchFailed(search.errorMessage), textAlign = TextAlign.Center, color = LeoColors.textSecondary, fontSize = 14.sp)
            }
        }
        search.results.isEmpty() -> ContactsCenteredPane {
            ContactsEmptyState(Icons.Rounded.PersonSearch, ContactsStrings.noPeopleFound, ContactsStrings.noPeopleFoundHint)
        }
        else -> LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(search.results, key = { _, u -> u.id }) { index, user ->
                if (index > 0) ContactsDivider(64.dp)
                UserResultRow(user, onClick = { controller.startChat(user) })
            }
        }
    }
}

@Composable
private fun DefaultContent(state: NewConversationState, onNewGroup: () -> Unit, onOpen: (Conversation) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), modifier = Modifier.fillMaxSize()) {
        item { CreateGroupCard(onNewGroup) }
        item { Spacer(Modifier.height(24.dp)) }
        item { RecentContactsSection(state.recentDirectChats, isLoading = state.isLoadingRecent, onOpen = onOpen) }
    }
}
