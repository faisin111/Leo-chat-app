package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.presentation.controllers.NewGroupController
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.NewGroupState
import com.leochat.features.chat.presentation.widgets.ContactsCenteredPane
import com.leochat.features.chat.presentation.widgets.ContactsLoader
import com.leochat.features.chat.presentation.widgets.ContactsTitle
import com.leochat.features.chat.presentation.widgets.ContactsTopBar
import com.leochat.features.chat.presentation.widgets.GroupInputField
import com.leochat.features.chat.presentation.widgets.SelectableUserRow
import com.leochat.features.chat.presentation.widgets.SelectedMemberChip
import org.koin.compose.viewmodel.koinViewModel

/**
 * Create a group: title, member search and selection, create button. Mirrors `NewGroupPage`.
 *
 * @param onBack pops the screen.
 * @param onGroupCreated the group exists; the caller pops and opens/selects it.
 */
@Composable
fun NewGroupPage(
    onBack: () -> Unit,
    onGroupCreated: (Conversation) -> Unit,
    modifier: Modifier = Modifier,
    controller: NewGroupController = koinViewModel(),
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

    Box(modifier.fillMaxSize().background(LeoColors.background)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContactsTopBar(onBack = onBack) {
                ContactsTitle(ContactsStrings.newGroup, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
            }
            TitleCard(state.title, controller::setTitle)
            if (state.selected.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    items(state.selected, key = { it.id }) { member -> SelectedMemberChip(member, onRemove = { controller.toggle(member) }) }
                }
            }
            GroupInputField(
                value = state.search.query,
                onValueChange = controller::search,
                placeholder = ContactsStrings.searchPeopleToAdd,
                leadingIcon = Icons.Rounded.Search,
                leadingTint = LeoColors.textSecondary,
                textStyle = TextStyle(fontSize = 16.sp, color = LeoColors.textPrimary),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(LeoColors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, LeoColors.border, RoundedCornerShape(16.dp))
                    .padding(vertical = 2.dp),
            )
            Box(Modifier.weight(1f)) { Results(state, controller) }
            LeoPrimaryButton(
                label = if (state.selected.isEmpty()) ContactsStrings.selectAtLeastOneMember else ContactsStrings.createGroupCount(state.selected.size),
                icon = Icons.Rounded.Check,
                isLoading = state.isCreating,
                onClick = if (state.canCreate) controller::create else null,
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(52.dp),
            )
        }
    }
}

@Composable
private fun TitleCard(title: String, onTitle: (String) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(LeoColors.surface, shape)
            .border(1.dp, LeoColors.border, shape)
            .padding(16.dp),
    ) {
        Text(ContactsStrings.groupDetailsLabel, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp, color = LeoColors.textSecondary)
        Spacer(Modifier.height(10.dp))
        GroupInputField(
            value = title,
            onValueChange = onTitle,
            placeholder = ContactsStrings.groupTitleHint,
            leadingIcon = Icons.Rounded.Groups,
            leadingTint = LeoColors.primary,
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textPrimary),
        )
    }
}

@Composable
private fun Results(state: NewGroupState, controller: NewGroupController) {
    val search = state.search
    when {
        !search.isActive -> ContactsCenteredPane {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.PersonSearch, contentDescription = null, tint = LeoColors.textMuted.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(ContactsStrings.searchUsersPrompt, fontSize = 14.sp, color = LeoColors.textSecondary, fontWeight = FontWeight.Medium)
            }
        }
        search.isLoading -> ContactsCenteredPane { ContactsLoader() }
        search.errorMessage != null -> ContactsCenteredPane {
            Text(ContactsStrings.groupSearchFailed(search.errorMessage), fontSize = 14.sp, color = LeoColors.textPrimary)
        }
        search.results.isEmpty() -> ContactsCenteredPane {
            Text(ContactsStrings.noUsersMatching(search.query), fontSize = 14.sp, color = LeoColors.textSecondary)
        }
        else -> LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(search.results, key = { it.id }) { user ->
                SelectableUserRow(user, selected = state.isSelected(user), onClick = { controller.toggle(user) })
            }
        }
    }
}
