package com.leochat.feature.chat.presentation.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.designsystem.component.chat.ChatDayLabel
import com.leochat.core.designsystem.component.chat.ChatEmptyPane
import com.leochat.core.designsystem.component.chat.JumpToLatestPill
import com.leochat.core.designsystem.component.chat.MessageActionSheet
import com.leochat.core.designsystem.component.chat.MessageSkeletonList
import com.leochat.core.designsystem.component.chat.ReconnectingBanner
import com.leochat.core.designsystem.component.chat.TypingIndicator
import com.leochat.core.designsystem.component.dialog.LeoConfirmationDialog
import com.leochat.core.designsystem.component.dialog.LeoInputDialog
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.LocalLeoPalette
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.presentation.ChatUiState
import com.leochat.feature.media.domain.model.PickedAttachment
import com.leochat.feature.media.domain.model.VoiceRecording
import com.leochat.feature.media.presentation.picker.rememberAttachmentPicker
import kotlinx.coroutines.launch

/** Stateless conversation UI: header, reconnect banner, message list, and composer. Mirrors `_ConversationDetailView`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    state: ChatUiState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSend: (text: String, replyToId: String?) -> Unit,
    onTyping: (Boolean) -> Unit,
    onRetry: (Message) -> Unit,
    onLeave: (String) -> Unit,
    onComingSoon: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDetails: (Conversation) -> Unit = { onComingSoon(AppStrings.detailsComingSoon) },
    onCall: (Conversation, video: Boolean) -> Unit = { _, _ -> onComingSoon(AppStrings.callsComingSoon) },
    onVoiceRecorded: (VoiceRecording) -> Unit = {},
    onSendAttachment: (PickedAttachment, caption: String?, replyToId: String?) -> Unit = { _, _, _ -> },
    onTogglePin: (String) -> Unit = {},
    onToggleMute: (String) -> Unit = {},
    onToggleArchive: (String) -> Unit = {},
    onEditMessage: (String, String) -> Unit = { _, _ -> },
    onDeleteMessage: (String) -> Unit = {},
    onToggleReaction: (String, String) -> Unit = { _, _ -> },
) {
    val conversation = state.selectedConversation
    if (conversation == null) {
        ChatEmptyPane(Icons.Outlined.Forum, 72, 32, AppStrings.noConversationSelected, AppStrings.chooseChat, modifier)
        return
    }

    val snackBar = LocalLeoSnackBar.current
    var replyingTo by remember { mutableStateOf<Message?>(null) }
    var actionMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var editContent by remember { mutableStateOf("") }
    var confirmDeleteMessage by remember { mutableStateOf<Message?>(null) }
    var showAttachmentChooser by remember { mutableStateOf(false) }
    var pendingAttachment by remember { mutableStateOf<PickedAttachment?>(null) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val picker = rememberAttachmentPicker(
        onPicked = { picked -> pendingAttachment = picked },
        onError = { message -> snackBar.showError(message) },
    )

    Column(modifier.fillMaxSize()) {
        ConversationHeaderSection(
            conversation = conversation,
            presence = state.presenceFor(conversation),
            typingStatusText = state.typingStatusText,
            showBackButton = showBackButton,
            onBack = onBack,
            onOpenDetails = { onOpenDetails(conversation) },
            onCall = { video -> onCall(conversation, video) },
            onLeave = { onLeave(conversation.id) },
            onTogglePin = { onTogglePin(conversation.id) },
            onToggleMute = { onToggleMute(conversation.id) },
            onToggleArchive = { onToggleArchive(conversation.id) },
        )
        AnimatedVisibility(state.isReconnecting) { ReconnectingBanner() }
        Box(Modifier.weight(1f)) {
            Crossfade(
                targetState = when {
                    state.isLoadingMessages -> 0
                    state.messages.isEmpty() -> 1
                    else -> 2
                },
                animationSpec = tween(250),
                label = "messagesPane",
            ) { pane ->
                when (pane) {
                    0 -> MessageSkeletonList()
                    1 -> ChatEmptyPane(Icons.Rounded.ChatBubbleOutline, 56, 26, AppStrings.noMessagesYet, AppStrings.sayHello)
                    else -> MessageList(
                        state = state,
                        avatarUrl = conversation.preferredAvatarUrl,
                        fallbackSenderName = conversation.effectiveTitle,
                        onRetry = onRetry,
                        onMessageLongClick = { actionMessage = it },
                        onReactionClick = { messageId, emoji -> onToggleReaction(messageId, emoji) },
                        onImageClick = { url -> previewImageUrl = url },
                        onCallAgain = if (!conversation.isGroup && !conversation.isAi) {
                            { isVideo -> onCall(conversation, isVideo) }
                        } else null,
                    )
                }
            }
        }
        ChatComposerSection(
            onSend = { text ->
                onSend(text, replyingTo?.id)
                replyingTo = null
            },
            onTyping = onTyping,
            onAttachmentClick = { showAttachmentChooser = true },
            onVoiceClick = { onComingSoon(AppStrings.voiceComingSoon) },
            onVoiceRecorded = onVoiceRecorded,
            replyingTo = replyingTo,
            onCancelReply = { replyingTo = null },
        )
    }

    if (showAttachmentChooser) {
        val palette = LocalLeoPalette.current
        ModalBottomSheet(
            onDismissRequest = { showAttachmentChooser = false },
            containerColor = palette.surface,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    MediaStrings.attachTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                AttachmentSourceRow(
                    icon = Icons.Rounded.Image,
                    label = MediaStrings.photoFromGallery,
                    onClick = {
                        showAttachmentChooser = false
                        picker.pickImage()
                    },
                )
                Spacer(Modifier.height(8.dp))
                AttachmentSourceRow(
                    icon = Icons.AutoMirrored.Rounded.InsertDriveFile,
                    label = MediaStrings.documentOrFile,
                    onClick = {
                        showAttachmentChooser = false
                        picker.pickFile()
                    },
                )
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    pendingAttachment?.let { picked ->
        AttachmentPreviewDialog(
            picked = picked,
            onSend = { caption ->
                onSendAttachment(picked, caption, replyingTo?.id)
                replyingTo = null
                pendingAttachment = null
            },
            onChangeFile = {
                pendingAttachment = null
                showAttachmentChooser = true
            },
            onDismiss = { pendingAttachment = null },
        )
    }

    previewImageUrl?.let { imageUrl ->
        FullScreenImageViewerDialog(
            imageUrl = imageUrl,
            onDismiss = { previewImageUrl = null },
        )
    }

    // Message Actions bottom sheet
    actionMessage?.let { msg ->
        MessageActionSheet(
            messageText = msg.content,
            isOwn = msg.isOwn,
            isDeleted = msg.deleted,
            onSelectReaction = { emoji ->
                actionMessage = null
                onToggleReaction(msg.id, emoji)
            },
            onReply = {
                actionMessage = null
                replyingTo = msg
            },
            onEdit = {
                actionMessage = null
                editContent = msg.content.orEmpty()
                editingMessage = msg
            },
            onDelete = {
                actionMessage = null
                confirmDeleteMessage = msg
            },
            onDismiss = { actionMessage = null },
        )
    }

    // Edit message dialog
    editingMessage?.let { msg ->
        LeoInputDialog(
            title = AppStrings.editMessage,
            value = editContent,
            onValueChange = { editContent = it },
            onConfirm = {
                val text = editContent.trim()
                if (text.isNotEmpty()) {
                    onEditMessage(msg.id, text)
                }
                editingMessage = null
            },
            onDismiss = { editingMessage = null },
        )
    }

    // Delete message confirmation dialog
    confirmDeleteMessage?.let { msg ->
        LeoConfirmationDialog(
            title = AppStrings.deleteMessageTitle,
            message = AppStrings.deleteMessageBody,
            confirmText = AppStrings.delete,
            cancelText = AppStrings.cancel,
            onConfirm = {
                val id = msg.id
                confirmDeleteMessage = null
                onDeleteMessage(id)
            },
            onDismiss = { confirmDeleteMessage = null },
        )
    }
}

@Composable
private fun MessageList(
    state: ChatUiState,
    avatarUrl: String?,
    fallbackSenderName: String? = null,
    onRetry: (Message) -> Unit,
    onMessageLongClick: (Message) -> Unit = {},
    onReactionClick: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    onImageClick: ((String) -> Unit)? = null,
    onCallAgain: ((isVideo: Boolean) -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages = state.messages
    // +1 for the TODAY label, +1 when the typing row is shown.
    val lastIndex = messages.size + (if (state.typingStatusText != null) 1 else 0)
    val farFromBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            last.index < info.totalItemsCount - 1 ||
                (last.offset + last.size - info.viewportEndOffset) > 180
        }
    }

    // Jump on open; animate for new messages, status changes, and typing.
    val firstScroll = remember(state.selectedConversationId) { mutableStateOf(true) }
    LaunchedEffect(state.selectedConversationId, messages.size, messages.lastOrNull()?.id, messages.lastOrNull()?.sendStatus, state.typingStatusText != null) {
        if (firstScroll.value) listState.scrollToItem(lastIndex) else listState.animateScrollToItem(lastIndex)
        firstScroll.value = false
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 28.dp, top = 20.dp, end = 28.dp, bottom = 16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "today") { ChatDayLabel(AppStrings.today) }
            items(messages, key = { it.id.ifEmpty { it.clientMessageId } }) { message ->
                MessageBubble(
                    message = message,
                    fallbackSenderName = fallbackSenderName,
                    resolvedMediaUrls = state.resolvedMediaUrls,
                    onRetry = { onRetry(message) },
                    onLongClick = if (message.isSystem || message.isCall) null else { { onMessageLongClick(message) } },
                    onReactionClick = { emoji -> onReactionClick(message.id, emoji) },
                    onImageClick = onImageClick,
                    onCallAgain = if (message.isCall) onCallAgain else null,
                )
            }
            if (state.typingStatusText != null) {
                item(key = "typing") {
                    Spacer(Modifier.height(10.dp))
                    TypingIndicator(state.typingStatusText, avatarUrl)
                }
            }
        }
        AnimatedVisibility(
            visible = farFromBottom,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 12.dp),
        ) {
            JumpToLatestPill(onClick = { scope.launch { listState.animateScrollToItem(lastIndex) } })
        }
    }
}

@Composable
private fun AttachmentSourceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val palette = LocalLeoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LeoColors.primary.copy(alpha = 0.14f)),
        ) {
            Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
    }
}

@Composable
private fun AttachmentPreviewDialog(
    picked: PickedAttachment,
    onSend: (caption: String?) -> Unit,
    onChangeFile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalLeoPalette.current
    var caption by remember(picked) { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    MediaStrings.previewAttachment,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, AppStrings.cancel, tint = palette.textSecondary)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (picked.isImage) {
                SubcomposeAsyncImage(
                    model = picked.uriOrPath,
                    contentDescription = picked.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 230.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceElevated),
                    loading = {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    },
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LeoColors.primary.copy(alpha = 0.14f)),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            picked.fileName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary,
                        )
                        if (picked.sizeBytes > 0) {
                            Text(
                                formatPickedSize(picked.sizeBytes),
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text(MediaStrings.addCaptionHint) },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = onChangeFile) {
                    Text(MediaStrings.changeFile, color = palette.textSecondary)
                }
                Button(
                    onClick = { onSend(caption.trim().takeIf { it.isNotEmpty() }) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeoColors.primary, contentColor = Color.White),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(MediaStrings.sendAttachment, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FullScreenImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .safeDrawingPadding(),
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    IconButton(
                        onClick = { runCatching { uriHandler.openUri(imageUrl) } },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)),
                ) {
                    Icon(Icons.Rounded.Close, AppStrings.cancel, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun formatPickedSize(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${(bytes / 1024.0).let { ((it * 10).toInt() / 10.0) }} KB"
    else -> "${(bytes / (1024.0 * 1024.0)).let { ((it * 10).toInt() / 10.0) }} MB"
}
