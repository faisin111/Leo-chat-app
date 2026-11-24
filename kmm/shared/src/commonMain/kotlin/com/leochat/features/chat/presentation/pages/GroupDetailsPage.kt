package com.leochat.features.chat.presentation.pages

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.entities.ConversationMember
import com.leochat.features.chat.presentation.controllers.GroupDetailsController
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.GroupDetailsState
import com.leochat.features.chat.presentation.widgets.AddMemberSheet
import com.leochat.features.chat.presentation.widgets.ContactsCard
import com.leochat.features.chat.presentation.widgets.ContactsDivider
import com.leochat.features.chat.presentation.widgets.ContactsLoader
import com.leochat.features.chat.presentation.widgets.ContactsTitle
import com.leochat.features.chat.presentation.widgets.ContactsTopBar
import com.leochat.features.chat.presentation.widgets.GroupDetailRow
import com.leochat.features.chat.presentation.widgets.GroupHeroIcon
import com.leochat.features.chat.presentation.widgets.GroupMemberRow
import com.leochat.features.chat.presentation.widgets.GroupQuickActionButton
import com.leochat.features.chat.presentation.widgets.contactsTap
import org.koin.compose.viewmodel.koinViewModel

/**
 * Group header, mute/media/add actions, shared media entry, and members. Mirrors `GroupDetailsPage`.
 *
 * @param conversationId the group conversation to show.
 * @param onBack pops the screen.
 * @param onOpenSharedMedia opens the shared media screen for the given conversation id.
 */
@Composable
fun GroupDetailsPage(
    conversationId: String,
    onBack: () -> Unit,
    onOpenSharedMedia: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    controller: GroupDetailsController = koinViewModel(),
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
    val openMedia = { onOpenSharedMedia(conversationId) }

    Box(modifier.fillMaxSize().background(LeoColors.background)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContactsTopBar(onBack = onBack) { ContactsTitle(ContactsStrings.groupDetails) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Header(state)
                Spacer(Modifier.height(20.dp))
                QuickActions(state, onMute = controller::toggleMute, onMedia = openMedia, onAdd = controller::openAddSheet)
                Spacer(Modifier.height(20.dp))
                ContactsCard {
                    GroupDetailRow(Icons.Outlined.PhotoLibrary, ContactsStrings.sharedMediaAndFiles, onClick = openMedia)
                }
                Spacer(Modifier.height(24.dp))
                MembersHeader(state, onAdd = controller::openAddSheet)
                Spacer(Modifier.height(14.dp))
                MembersCard(state, onRemove = controller::removeMember)
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (state.isAddSheetOpen) {
        AddMemberSheet(
            search = state.addSearch,
            eligible = state.eligibleToAdd,
            onQuery = controller::searchToAdd,
            onAdd = controller::addMember,
            onDismiss = controller::closeAddSheet,
        )
    }
}

@Composable
private fun Header(state: GroupDetailsState) {
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
private fun QuickActions(state: GroupDetailsState, onMute: () -> Unit, onMedia: () -> Unit, onAdd: () -> Unit) {
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
private fun MembersHeader(state: GroupDetailsState, onAdd: () -> Unit) {
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
private fun MembersCard(state: GroupDetailsState, onRemove: (ConversationMember) -> Unit) {
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
                    member = member,
                    isSelf = state.isSelf(member),
                    canRemove = state.canRemove(member),
                    onRemove = { onRemove(member) },
                )
            }
        }
    }
}
