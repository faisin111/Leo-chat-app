package com.leochat.feature.chat.presentation.groupdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.chat.AddMemberSheet
import com.leochat.core.designsystem.component.chat.ContactsCard
import com.leochat.core.designsystem.component.chat.ContactsDivider
import com.leochat.core.designsystem.component.chat.ContactsLoader
import com.leochat.core.designsystem.component.chat.ContactsTitle
import com.leochat.core.designsystem.component.chat.ContactsTopBar
import com.leochat.core.designsystem.component.chat.GroupDetailRow
import com.leochat.core.designsystem.component.chat.GroupHeroIcon
import com.leochat.core.designsystem.component.chat.GroupMemberRow
import com.leochat.core.designsystem.component.chat.GroupQuickActionButton
import com.leochat.core.designsystem.component.chat.contactsTap
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.presentation.GroupDetailsUiState
import com.leochat.feature.chat.presentation.mapper.toMemberRowUi
import com.leochat.feature.chat.presentation.mapper.toPersonRowUi
import com.leochat.feature.users.domain.model.UserSearchItem

/** Stateless group details UI driven by [GroupDetailsUiState]. */
@Composable
fun GroupDetailsContent(
    state: GroupDetailsUiState,
    onBack: () -> Unit,
    onOpenSharedMedia: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenAddSheet: () -> Unit,
    onRemoveMember: (ConversationMember) -> Unit,
    onSearchToAdd: (String) -> Unit,
    onAddMember: (UserSearchItem) -> Unit,
    onCloseAddSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(LeoColors.background)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContactsTopBar(onBack = onBack) { ContactsTitle(ContactsStrings.groupDetails) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Header(state)
                Spacer(Modifier.height(20.dp))
                QuickActions(state, onMute = onToggleMute, onMedia = onOpenSharedMedia, onAdd = onOpenAddSheet)
                Spacer(Modifier.height(20.dp))
                ContactsCard {
                    GroupDetailRow(Icons.Outlined.PhotoLibrary, ContactsStrings.sharedMediaAndFiles, onClick = onOpenSharedMedia)
                }
                Spacer(Modifier.height(24.dp))
                MembersHeader(state, onAdd = onOpenAddSheet)
                Spacer(Modifier.height(14.dp))
                MembersCard(state, onRemove = onRemoveMember)
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (state.isAddSheetOpen) {
        val eligible = state.eligibleToAdd
        AddMemberSheet(
            query = state.addSearch.query,
            isLoading = state.addSearch.isLoading,
            errorMessage = state.addSearch.errorMessage,
            eligible = eligible.map { it.toPersonRowUi() },
            onQuery = onSearchToAdd,
            onAdd = { person -> eligible.firstOrNull { it.id == person.id }?.let(onAddMember) },
            onDismiss = onCloseAddSheet,
        )
    }
}

@Composable
private fun Header(state: GroupDetailsUiState) {
    GroupHeroIcon()
    Spacer(Modifier.height(14.dp))
    Text(
        state.title,
        textAlign = TextAlign.Center,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp,
        color = LeoColors.textPrimary,
    )
    if (state.description.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(state.description, textAlign = TextAlign.Center, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = LeoColors.textSecondary)
    }
}

@Composable
private fun QuickActions(state: GroupDetailsUiState, onMute: () -> Unit, onMedia: () -> Unit, onAdd: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GroupQuickActionButton(
            icon = if (state.isMuted) Icons.Rounded.NotificationsOff else Icons.Outlined.NotificationsOff,
            label = if (state.isMuted) ContactsStrings.muted else ContactsStrings.mute,
            onClick = onMute,
            modifier = Modifier.weight(1f),
        )
        GroupQuickActionButton(Icons.Outlined.PhotoLibrary, ContactsStrings.media, onMedia, Modifier.weight(1f))
        if (state.isOwnerOrAdmin) {
            GroupQuickActionButton(Icons.Outlined.PersonAdd, ContactsStrings.add, onAdd, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MembersHeader(state: GroupDetailsUiState, onAdd: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        val count = if (state.isLoadingMembers) ContactsStrings.membersLoading else state.members.size.toString()
        Text(ContactsStrings.membersHeader(count), fontSize = 16.sp, fontWeight = FontWeight.Black, color = LeoColors.textPrimary)
        if (state.isOwnerOrAdmin) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.contactsTap(onClick = onAdd)) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(ContactsStrings.add, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.primary)
            }
        }
    }
}

@Composable
private fun MembersCard(state: GroupDetailsUiState, onRemove: (ConversationMember) -> Unit) {
    ContactsCard {
        when {
            state.isLoadingMembers -> ContactsLoader(padding = 24.dp)
            state.members.isEmpty() -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    state.membersErrorMessage ?: ContactsStrings.noMembersFound,
                    color = LeoColors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
            else -> state.members.forEachIndexed { index, member ->
                if (index > 0) ContactsDivider(68.dp)
                GroupMemberRow(
                    member = member.toMemberRowUi(isSelf = state.isSelf(member), canRemove = state.canRemove(member)),
                    onRemove = { onRemove(member) },
                )
            }
        }
    }
}
