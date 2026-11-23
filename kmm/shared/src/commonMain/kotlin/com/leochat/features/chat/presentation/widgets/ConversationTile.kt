package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.utils.TimeFormat
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.features.chat.domain.entities.Conversation

internal val GroupGradient = Brush.linearGradient(listOf(Color(0xFFFF7A1A), Color(0xFFE53926)), Offset.Zero, Offset.Infinite)

/** Conversation list row. Mirrors `LeoConversationTile`. */
@Composable
fun ConversationTile(
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = conversation.effectiveTitle
    val snippet = if (conversation.isTyping) AppStrings.typingSuffix else conversation.lastMessageSnippet ?: AppStrings.noMessagesYet
    val time = TimeFormat.relative(conversation.lastMessageAt ?: conversation.createdAt, yesterday = AppStrings.yesterday)
    val hasUnread = conversation.unreadCount > 0
    val shape = RoundedCornerShape(16.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) LeoColors.input else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        if (conversation.isGroup && conversation.preferredAvatarUrl == null) {
            Box(Modifier.size(52.dp).background(GroupGradient, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        } else {
            LeoAvatar(
                radius = 26.dp,
                imageUrl = conversation.preferredAvatarUrl,
                fallbackText = conversationInitials(title),
                hasOnlineBadge = !conversation.isGroup && conversation.isOnline,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.textPrimary)
            Spacer(Modifier.height(3.dp))
            Text(
                snippet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontWeight = if (conversation.isTyping) FontWeight.Bold else FontWeight.Medium,
                color = if (conversation.isTyping) LeoColors.primary else LeoColors.textSecondary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                time,
                fontSize = 12.sp,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                color = if (hasUnread) LeoColors.primary else LeoColors.textSecondary,
            )
            Spacer(Modifier.height(4.dp))
            when {
                hasUnread -> Box(Modifier.size(20.dp).background(LeoColors.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${conversation.unreadCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                conversation.isMuted -> Icon(Icons.AutoMirrored.Rounded.VolumeOff, null, tint = LeoColors.textMuted, modifier = Modifier.size(16.dp))
                else -> Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/** First letters of the first and last words, e.g. `Maya Chen` → `MC`. */
fun conversationInitials(value: String): String {
    val words = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}
