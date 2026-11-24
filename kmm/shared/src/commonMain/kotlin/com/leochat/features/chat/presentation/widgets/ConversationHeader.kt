package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.constants.CallStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.media.presentation.permissions.MediaPermission
import com.leochat.features.media.presentation.permissions.rememberMediaPermissionRequester

private val AiGradient = Brush.linearGradient(listOf(Color(0xFFFF6D00), LeoColors.primary), Offset.Zero, Offset.Infinite)

/**
 * Top bar of an open conversation. Mirrors `_ConversationHeader`.
 * Call buttons ask for the microphone first, then call [onCall] (`video` = true for video).
 */
@Composable
fun ConversationHeader(
    conversation: Conversation,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenDetails: () -> Unit,
    onCall: (video: Boolean) -> Unit,
    onLeave: () -> Unit,
) {
    val palette = LocalLeoPalette.current
    val isAi = conversation.isAi
    val title = if (isAi) AppStrings.leoAiAssistant else conversation.effectiveTitle
    val subtitle = when {
        isAi -> AppStrings.aiSubtitleHeader
        conversation.isGroup -> AppStrings.groupConversation
        conversation.isOnline -> AppStrings.online
        else -> AppStrings.offline
    }
    val primaryText = if (palette.isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary
    val secondaryText = if (palette.isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary
    val border = if (palette.isDark) LeoColors.darkBorder else LeoColors.border
    var confirmLeave by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val permissions = rememberMediaPermissionRequester()
    val snackBar = LocalLeoSnackBar.current
    fun startCall(video: Boolean) {
        permissions.request(setOf(MediaPermission.Microphone)) { granted ->
            if (granted) onCall(video) else snackBar.showError(CallStrings.microphonePermissionRequired)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (palette.isDark) LeoColors.darkSurface else Color.White)
            .drawBehind { drawLine(border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .padding(start = if (showBackButton) 8.dp else 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
    ) {
        if (showBackButton) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = primaryText) }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAi, onClick = onOpenDetails).padding(vertical = 4.dp),
        ) {
            if (isAi) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = LeoColors.primary.copy(alpha = 0.3f), spotColor = LeoColors.primary.copy(alpha = 0.3f))
                        .background(AiGradient, RoundedCornerShape(16.dp)),
                ) { Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
            } else {
                LeoAvatar(radius = 26.dp, name = title, imageUrl = conversation.preferredAvatarUrl, hasOnlineBadge = conversation.isOnline)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp, fontWeight = FontWeight.Black, color = primaryText, modifier = Modifier.weight(1f, fill = false))
                    if (isAi) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            AppStrings.aiBadge,
                            color = LeoColors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.background(LeoColors.primarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp),
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = secondaryText)
            }
        }
        Spacer(Modifier.width(8.dp))
        if (isAi) {
            IconButton(onClick = { confirmLeave = true }) {
                Icon(Icons.Rounded.DeleteOutline, AppStrings.clearConversation, tint = secondaryText, modifier = Modifier.size(22.dp))
            }
        } else {
            HeaderIcon(Icons.Outlined.Phone, primaryText, 22, CallStrings.voiceCall) { startCall(video = false) }
            Spacer(Modifier.width(16.dp))
            HeaderIcon(Icons.Outlined.Videocam, primaryText, 24, CallStrings.videoCall) { startCall(video = true) }
            Spacer(Modifier.width(6.dp))
            Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Rounded.MoreVert, null, tint = secondaryText) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (conversation.isGroup) AppStrings.leaveGroup else AppStrings.deleteChat,
                                color = LeoColors.destructive,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = LeoColors.destructive, modifier = Modifier.size(20.dp)) },
                        onClick = {
                            menuOpen = false
                            confirmLeave = true
                        },
                    )
                }
            }
        }
    }

    if (confirmLeave) {
        val (titleText, body, action) = when {
            isAi -> Triple(AppStrings.clearAiTitle, AppStrings.clearAiBody, AppStrings.delete)
            conversation.isGroup -> Triple(AppStrings.leaveGroupTitle, AppStrings.leaveGroupBody, AppStrings.leave)
            else -> Triple(AppStrings.deleteConversationTitle, AppStrings.deleteConversationBody, AppStrings.delete)
        }
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text(titleText) },
            text = { Text(body) },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text(AppStrings.cancel) } },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    onLeave()
                }) { Text(action, color = LeoColors.destructive) }
            },
        )
    }
}

@Composable
private fun HeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, size: Int, contentDescription: String? = null, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Icon(
        icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.leoPressable(interaction).clickable(interactionSource = interaction, indication = null, onClick = onClick).size(size.dp),
    )
}
