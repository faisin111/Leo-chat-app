package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.chat.model.MemberRoleUi
import com.leochat.core.designsystem.component.chat.model.MemberRowUi
import com.leochat.core.designsystem.component.chat.model.PersonRowUi
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors

/** 76dp sun-gradient squircle with the groups icon. */
@Composable
fun GroupHeroIcon() {
    val shape = RoundedCornerShape(24.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .shadow(10.dp, shape, ambientColor = ChatTokens.heroGlow, spotColor = ChatTokens.heroGlow)
            .background(ChatTokens.heroGradient, shape),
    ) {
        Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
    }
}

/** Card button with icon over label. Mirrors `_QuickActionButton`. */
@Composable
fun GroupQuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .contactsTap(onClick = onClick)
            .shadow(2.dp, shape, ambientColor = ChatTokens.cardShadow, spotColor = ChatTokens.cardShadow)
            .background(LeoColors.surface, shape)
            .border(1.dp, LeoColors.border, shape)
            .padding(vertical = 12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary)
    }
}

/** Icon tile, title, chevron. Mirrors `_DetailRow`. */
@Composable
fun GroupDetailRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().contactsTap(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(Modifier.size(36.dp).background(LeoColors.input, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = LeoColors.textPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = LeoColors.textSecondary, modifier = Modifier.size(22.dp))
    }
}

/** Member row with role badge and optional Remove action. */
@Composable
fun GroupMemberRow(member: MemberRowUi, onRemove: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        LeoAvatar(radius = 22.dp, name = member.avatarName)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                member.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LeoColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            member.handle?.let {
                Text(it, fontSize = 12.sp, color = LeoColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (member.role != MemberRoleUi.Member) {
            RoleBadge(isOwner = member.role == MemberRoleUi.Owner)
            Spacer(Modifier.width(8.dp))
        }
        if (member.canRemove) {
            Text(
                ContactsStrings.remove,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ChatTokens.removeText,
                modifier = Modifier.contactsTap(onClick = onRemove).padding(4.dp),
            )
        }
    }
}

@Composable
private fun RoleBadge(isOwner: Boolean) {
    Text(
        if (isOwner) ContactsStrings.owner else ContactsStrings.admin,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (isOwner) ChatTokens.ownerBadgeText else ChatTokens.adminBadgeText,
        modifier = Modifier
            .background(if (isOwner) ChatTokens.ownerBadgeBackground else ChatTokens.adminBadgeBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/** Bottom sheet to search and add one member. Mirrors `_AddMemberSheet`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberSheet(
    query: String,
    isLoading: Boolean,
    errorMessage: String?,
    eligible: List<PersonRowUi>,
    onQuery: (String) -> Unit,
    onAdd: (PersonRowUi) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = LeoColors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .background(ChatTokens.chevronMuted, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.height(16.dp))
            Text(ContactsStrings.addGroupMember, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.textPrimary)
            Spacer(Modifier.height(12.dp))
            ContactsSearchPill(
                value = query,
                onValueChange = onQuery,
                placeholder = ContactsStrings.searchPeopleToAdd,
                height = 46.dp,
                horizontalPadding = 14.dp,
                iconSize = 20.dp,
                gap = 8.dp,
                textStyle = TextStyle(fontSize = 16.sp, color = LeoColors.textPrimary),
                placeholderColor = LeoColors.textMuted,
            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                when {
                    isLoading -> ContactsLoader()
                    errorMessage != null -> Text(ContactsStrings.addSearchFailed(errorMessage), fontSize = 14.sp, color = LeoColors.textPrimary)
                    eligible.isEmpty() -> Text(ContactsStrings.noMatchingUsersToAdd, color = LeoColors.textSecondary, fontSize = 14.sp)
                    else -> LazyColumn(Modifier.fillMaxWidth().height(260.dp)) {
                        itemsIndexed(eligible, key = { _, u -> u.id }) { index, user ->
                            if (index > 0) ContactsDivider(56.dp)
                            AddMemberRow(user, onClick = { onAdd(user) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMemberRow(user: PersonRowUi, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().contactsTap(onClick = onClick).padding(vertical = 10.dp)) {
        LeoAvatar(radius = 20.dp, name = user.name, imageUrl = user.avatarUrl)
        Spacer(Modifier.width(12.dp))
        Text(user.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
    }
}
