package com.leochat.feature.chat.presentation.newconversation

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.chat.ContactsCenteredPane
import com.leochat.core.designsystem.component.chat.ContactsDivider
import com.leochat.core.designsystem.component.chat.ContactsEmptyState
import com.leochat.core.designsystem.component.chat.ContactsIconButton
import com.leochat.core.designsystem.component.chat.ContactsLoader
import com.leochat.core.designsystem.component.chat.ContactsSearchPill
import com.leochat.core.designsystem.component.chat.ContactsTitle
import com.leochat.core.designsystem.component.chat.ContactsTopBar
import com.leochat.core.designsystem.component.chat.CreateGroupCard
import com.leochat.core.designsystem.component.chat.RecentContactsSection
import com.leochat.core.designsystem.component.chat.UserResultRow
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.presentation.NewConversationUiState
import com.leochat.feature.chat.presentation.mapper.toPersonRowUi
import com.leochat.feature.chat.presentation.mapper.toRecentContactUi
import com.leochat.feature.users.domain.model.UserSearchItem

/**
 * Stateless new conversation UI driven by [NewConversationUiState].
 * [onBack] `null` hides the back and close buttons and only pads for the keyboard (Contacts tab use).
 */
@Composable
fun NewConversationContent(
    state: NewConversationUiState,
    onBack: (() -> Unit)?,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onStartChat: (UserSearchItem) -> Unit,
    onNewGroup: () -> Unit,
    onOpenExisting: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val insets = if (onBack != null) Modifier.safeDrawingPadding() else Modifier.imePadding()

    Box(modifier.fillMaxSize().background(palette.background)) {
        Column(Modifier.fillMaxSize().background(palette.background).then(insets)) {
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
                onValueChange = onSearch,
                placeholder = ContactsStrings.searchByNameOrUsername,
                onClear = onClearSearch,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            if (state.isStartingChat) {
                LinearProgressIndicator(color = LeoColors.primary, trackColor = Color.Transparent, modifier = Modifier.fillMaxWidth().height(2.dp))
            }
            Box(Modifier.weight(1f).background(palette.background)) {
                if (state.search.isActive) {
                    SearchResults(state, onStartChat)
                } else {
                    DefaultContent(state, onNewGroup, onOpenExisting)
                }
            }
        }
    }
}

@Composable
private fun SearchResults(state: NewConversationUiState, onStartChat: (UserSearchItem) -> Unit) {
    val palette = LeoThemeTokens.palette
    val search = state.search
    when {
        search.isLoading -> ContactsCenteredPane { ContactsLoader() }
        search.errorMessage != null -> ContactsCenteredPane {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = LeoColors.destructive, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(ContactsStrings.searchFailed(search.errorMessage), textAlign = TextAlign.Center, color = palette.textSecondary, fontSize = 14.sp)
            }
        }
        search.results.isEmpty() -> ContactsCenteredPane {
            ContactsEmptyState(Icons.Rounded.PersonSearch, ContactsStrings.noPeopleFound, ContactsStrings.noPeopleFoundHint)
        }
        else -> LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), modifier = Modifier.fillMaxSize().background(palette.background)) {
            itemsIndexed(search.results, key = { _, u -> u.id }) { index, user ->
                if (index > 0) ContactsDivider(64.dp)
                UserResultRow(user.toPersonRowUi(), onClick = { onStartChat(user) })
            }
        }
    }
}

@Composable
private fun DefaultContent(state: NewConversationUiState, onNewGroup: () -> Unit, onOpen: (Conversation) -> Unit) {
    val palette = LeoThemeTokens.palette
    val chats = state.recentDirectChats
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), modifier = Modifier.fillMaxSize().background(palette.background)) {
        item { CreateGroupCard(onNewGroup) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            RecentContactsSection(
                contacts = chats.map { it.toRecentContactUi() },
                isLoading = state.isLoadingRecent,
                onOpen = { contact -> chats.firstOrNull { it.id == contact.id }?.let(onOpen) },
            )
        }
    }
}
