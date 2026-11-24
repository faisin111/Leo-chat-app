package com.leochat.feature.chat.presentation.newgroup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.chat.ContactsCenteredPane
import com.leochat.core.designsystem.component.chat.ContactsLoader
import com.leochat.core.designsystem.component.chat.ContactsTitle
import com.leochat.core.designsystem.component.chat.ContactsTopBar
import com.leochat.core.designsystem.component.chat.GroupInputField
import com.leochat.core.designsystem.component.chat.SelectableUserRow
import com.leochat.core.designsystem.component.chat.SelectedMemberChip
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.chat.presentation.NewGroupUiState
import com.leochat.feature.chat.presentation.mapper.toPersonRowUi
import com.leochat.feature.users.domain.model.UserSearchItem

/** Stateless new group UI driven by [NewGroupUiState]. */
@Composable
fun NewGroupContent(
    state: NewGroupUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onToggleMember: (UserSearchItem) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    Box(modifier.fillMaxSize().background(palette.background)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContactsTopBar(onBack = onBack) {
                ContactsTitle(ContactsStrings.newGroup, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
            }
            TitleCard(state.title, onTitleChange)
            if (state.selected.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    items(state.selected, key = { it.id }) { member ->
                        SelectedMemberChip(member.toPersonRowUi(), onRemove = { onToggleMember(member) })
                    }
                }
            }
            GroupInputField(
                value = state.search.query,
                onValueChange = onSearch,
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
            Box(Modifier.weight(1f)) { Results(state, onToggleMember) }
            LeoPrimaryButton(
                label = if (state.selected.isEmpty()) ContactsStrings.selectAtLeastOneMember else ContactsStrings.createGroupCount(state.selected.size),
                icon = Icons.Rounded.Check,
                isLoading = state.isCreating,
                onClick = if (state.canCreate) onCreate else null,
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
private fun Results(state: NewGroupUiState, onToggleMember: (UserSearchItem) -> Unit) {
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
                SelectableUserRow(user.toPersonRowUi(), selected = state.isSelected(user), onClick = { onToggleMember(user) })
            }
        }
    }
}
