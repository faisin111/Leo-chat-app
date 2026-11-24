package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/**
 * Modern, beautifully styled bottom sheet for conversation long-press actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationActionSheet(
    conversationTitle: String,
    avatarUrl: String?,
    lastSnippet: String?,
    isPinned: Boolean,
    isMuted: Boolean,
    isArchived: Boolean,
    isGroup: Boolean,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val containerBg = if (palette.isDark) Color(0xFF101014) else palette.surface
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = containerBg,
        shape = sheetShape,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            // Drag handle pill
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .background(
                            if (palette.isDark) Color(0xFF2C2C34) else palette.border,
                            RoundedCornerShape(2.dp),
                        ),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Conversation Preview Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (palette.isDark) Color(0xFF16161B) else palette.input.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (palette.isDark) Color(0xFF222228) else palette.border.copy(alpha = 0.4f),
                        RoundedCornerShape(18.dp),
                    )
                    .padding(14.dp),
            ) {
                LeoAvatar(
                    radius = 22.dp,
                    name = conversationTitle,
                    imageUrl = avatarUrl,
                    hasOnlineBadge = false,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversationTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isPinned) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = LeoColors.primary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        if (isMuted) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    if (!lastSnippet.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = lastSnippet,
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Items
            ConversationSheetActionRow(
                icon = Icons.Rounded.PushPin,
                title = if (isPinned) AppStrings.unpinChat else AppStrings.pinChat,
                iconTint = if (isPinned) LeoColors.primary else palette.textPrimary,
                onClick = onTogglePin,
            )

            ConversationSheetActionRow(
                icon = if (isMuted) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                title = if (isMuted) AppStrings.unmuteChat else AppStrings.muteChat,
                onClick = onToggleMute,
            )

            ConversationSheetActionRow(
                icon = Icons.Rounded.Archive,
                title = if (isArchived) AppStrings.unarchiveChat else AppStrings.archiveChat,
                onClick = onToggleArchive,
            )

            HorizontalDivider(
                color = if (palette.isDark) Color(0xFF1E1E24) else palette.border.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 6.dp),
            )

            ConversationSheetActionRow(
                icon = Icons.Rounded.DeleteOutline,
                title = if (isGroup) AppStrings.leaveGroup else AppStrings.deleteChat,
                isDestructive = true,
                onClick = onDelete,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConversationSheetActionRow(
    icon: ImageVector,
    title: String,
    iconTint: Color = LeoThemeTokens.palette.textPrimary,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val textColor = if (isDestructive) LeoColors.destructive else palette.textPrimary
    val tint = if (isDestructive) LeoColors.destructive else iconTint
    val badgeBg = if (isDestructive) {
        LeoColors.destructive.copy(alpha = 0.12f)
    } else if (palette.isDark) {
        Color(0xFF1A1A20)
    } else {
        palette.input
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}
