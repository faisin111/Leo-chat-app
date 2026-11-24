package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.lineHeight
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.core.widgets.common.feedback.LeoSkeleton
import com.leochat.core.widgets.common.feedback.LeoSkeletonCircle
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.users.domain.entities.UserSearchItem

/** People search hit: avatar, name, @username, chat icon. */
@Composable
internal fun UserResultRow(user: UserSearchItem, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().contactsTap(onClick = onClick).padding(vertical = 12.dp),
    ) {
        LeoAvatar(radius = 24.dp, name = user.effectiveName, imageUrl = user.profilePictureUrl)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(user.effectiveName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(ContactsStrings.handle(user.username), fontSize = 13.sp, color = LeoColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
    }
}

/** "Create a group" action card. */
@Composable
internal fun CreateGroupCard(onClick: () -> Unit) {
    ContactsCard(withShadow = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().contactsTap(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(Modifier.size(44.dp).background(LeoColors.primarySoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(ContactsStrings.createAGroup, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(ContactsStrings.createAGroupSubtitle, fontSize = 13.sp, color = LeoColors.textSecondary)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = LeoColors.textSecondary, modifier = Modifier.size(22.dp))
        }
    }
}

/** RECENT CONTACTS header and card, a skeleton while the first load runs, or the start-a-chat empty card. */
@Composable
internal fun RecentContactsSection(chats: List<Conversation>, isLoading: Boolean, onOpen: (Conversation) -> Unit) {
    when {
        chats.isNotEmpty() -> Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ContactsStrings.recentContacts, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp, color = LeoColors.textSecondary)
                Text(ContactsStrings.peopleCount(chats.size), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textSecondary)
            }
            Spacer(Modifier.height(12.dp))
            ContactsCard {
                chats.forEachIndexed { index, chat ->
                    if (index > 0) ContactsDivider(64.dp)
                    RecentContactRow(chat, onClick = { onOpen(chat) })
                }
            }
        }
        isLoading -> RecentContactsSkeleton()
        else -> ContactsCard {
            ContactsEmptyState(
                icon = Icons.Rounded.PeopleOutline,
                title = ContactsStrings.startNewConversation,
                subtitle = ContactsStrings.startNewConversationHint,
                discColor = LeoColors.primarySoft,
                iconColor = LeoColors.primary,
                subtitleLineHeight = lineHeight(13, 1.4),
                modifier = Modifier.fillMaxWidth().padding(32.dp),
            )
        }
    }
}

@Composable
private fun RecentContactRow(chat: Conversation, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().contactsTap(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LeoAvatar(radius = 22.dp, name = chat.effectiveTitle, imageUrl = chat.preferredAvatarUrl, hasOnlineBadge = chat.isOnline)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.effectiveTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val snippet = chat.lastMessageSnippet
            if (!snippet.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(snippet, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = LeoColors.textSecondary)
            }
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = ContactsTokens.chevronMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RecentContactsSkeleton() {
    ContactsCard {
        repeat(3) { index ->
            if (index > 0) ContactsDivider(64.dp)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                LeoSkeletonCircle(44.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    LeoSkeleton(Modifier.width(140.dp).height(14.dp), cornerRadius = 6.dp)
                    Spacer(Modifier.height(8.dp))
                    LeoSkeleton(Modifier.width(200.dp).height(11.dp), cornerRadius = 6.dp)
                }
            }
        }
    }
}
