package com.leochat.core.designsystem.component.chat

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.chat.model.ConversationTileUi
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Conversation list row. Mirrors `LeoConversationTile`. */
@Composable
fun ConversationTile(
    tile: ConversationTileUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) palette.input else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        if (tile.showsGroupIcon) {
            Box(Modifier.size(52.dp).background(ChatTokens.groupGradient, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        } else {
            LeoAvatar(
                radius = 26.dp,
                imageUrl = tile.avatarUrl,
                fallbackText = tile.initials,
                hasOnlineBadge = tile.hasOnlineBadge,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(tile.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
            Spacer(Modifier.height(3.dp))
            Text(
                tile.snippet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontWeight = if (tile.isTyping) FontWeight.Bold else FontWeight.Medium,
                color = if (tile.isTyping) LeoColors.primary else palette.textSecondary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                tile.time,
                fontSize = 12.sp,
                fontWeight = if (tile.hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                color = if (tile.hasUnread) LeoColors.primary else palette.textSecondary,
            )
            Spacer(Modifier.height(4.dp))
            when {
                tile.hasUnread -> Box(Modifier.size(20.dp).background(LeoColors.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${tile.unreadCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                tile.isMuted -> Icon(Icons.AutoMirrored.Rounded.VolumeOff, null, tint = palette.textMuted, modifier = Modifier.size(16.dp))
                else -> Spacer(Modifier.height(20.dp))
            }
        }
    }
}
