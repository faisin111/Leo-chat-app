package com.leochat.core.designsystem.component.chat

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

val QUICK_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")

/**
 * Modern, beautifully styled bottom sheet for message long-press actions and emoji reactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionSheet(
    messageText: String?,
    isOwn: Boolean,
    isDeleted: Boolean,
    onSelectReaction: (String) -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val containerBg = if (palette.isDark) Color(0xFF101014) else palette.surface
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val clipboard = LocalClipboardManager.current
    val snackBar = LocalLeoSnackBar.current

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

            // Quick Emoji Reaction Bar (Floating pill with spring scale feedback)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (palette.isDark) Color(0xFF16161B) else palette.input.copy(alpha = 0.7f))
                    .border(
                        1.dp,
                        if (palette.isDark) Color(0xFF222228) else palette.border.copy(alpha = 0.4f),
                        RoundedCornerShape(22.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QUICK_REACTIONS.forEach { emoji ->
                    QuickReactionItem(
                        emoji = emoji,
                        onClick = {
                            onDismiss()
                            onSelectReaction(emoji)
                        },
                    )
                }
            }

            // Message text preview snippet
            if (!messageText.isNullOrBlank() && !isDeleted) {
                Spacer(Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (palette.isDark) Color(0xFF16161B) else palette.input.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(LeoColors.primary, RoundedCornerShape(1.5.dp)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = messageText,
                        fontSize = 13.sp,
                        color = palette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Reply action
            MessageSheetActionRow(
                icon = Icons.AutoMirrored.Rounded.Reply,
                title = AppStrings.reply,
                onClick = onReply,
            )

            // Copy action (if text is available)
            if (!messageText.isNullOrBlank() && !isDeleted) {
                MessageSheetActionRow(
                    icon = Icons.Rounded.ContentCopy,
                    title = "Copy Text",
                    onClick = {
                        clipboard.setText(AnnotatedString(messageText))
                        snackBar.showSuccess("Message copied to clipboard")
                        onDismiss()
                    },
                )
            }

            // Edit action (if own message)
            if (isOwn && !isDeleted) {
                MessageSheetActionRow(
                    icon = Icons.Rounded.Edit,
                    title = AppStrings.editMessage,
                    onClick = onEdit,
                )

                HorizontalDivider(
                    color = if (palette.isDark) Color(0xFF1E1E24) else palette.border.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                // Delete action
                MessageSheetActionRow(
                    icon = Icons.Rounded.DeleteOutline,
                    title = AppStrings.deleteMessage,
                    isDestructive = true,
                    onClick = onDelete,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuickReactionItem(
    emoji: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "emojiScale",
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 23.sp)
    }
}

@Composable
private fun MessageSheetActionRow(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val textColor = if (isDestructive) LeoColors.destructive else palette.textPrimary
    val tint = if (isDestructive) LeoColors.destructive else palette.textPrimary
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
