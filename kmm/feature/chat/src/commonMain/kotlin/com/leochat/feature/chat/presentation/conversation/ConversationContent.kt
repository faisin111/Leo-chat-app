package com.leochat.feature.chat.presentation.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.chat.ChatDayLabel
import com.leochat.core.designsystem.component.chat.ChatEmptyPane
import com.leochat.core.designsystem.component.chat.JumpToLatestPill
import com.leochat.core.designsystem.component.chat.MessageSkeletonList
import com.leochat.core.designsystem.component.chat.ReconnectingBanner
import com.leochat.core.designsystem.component.chat.TypingIndicator
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.presentation.ChatUiState
import com.leochat.feature.media.domain.model.VoiceRecording
import kotlinx.coroutines.launch

private val QUICK_REACTION_EMOJIS = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")

/** Stateless conversation UI: header, reconnect banner, message list, and composer. Mirrors `_ConversationDetailView`. */
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

    var replyingTo by remember { mutableStateOf<Message?>(null) }
    var actionMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var editContent by remember { mutableStateOf("") }
    var confirmDeleteMessage by remember { mutableStateOf<Message?>(null) }

    Column(modifier.fillMaxSize()) {
        ConversationHeaderSection(
            conversation = conversation,
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
                        onRetry = onRetry,
                        onMessageLongClick = { actionMessage = it },
                        onReactionClick = { messageId, emoji -> onToggleReaction(messageId, emoji) },
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
            onAttachmentClick = { onComingSoon(AppStrings.attachmentsComingSoon) },
            onVoiceClick = { onComingSoon(AppStrings.voiceComingSoon) },
            onVoiceRecorded = onVoiceRecorded,
            replyingTo = replyingTo,
            onCancelReply = { replyingTo = null },
        )
    }

    // Message Actions dialog
    actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            title = {
                // Quick emoji reactions row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QUICK_REACTION_EMOJIS.forEach { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LeoThemeTokens.palette.input)
                                .clickable {
                                    actionMessage = null
                                    onToggleReaction(msg.id, emoji)
                                },
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    MessageActionItem(
                        icon = Icons.AutoMirrored.Rounded.Reply,
                        text = AppStrings.reply,
                        onClick = {
                            actionMessage = null
                            replyingTo = msg
                        },
                    )
                    if (msg.isOwn && !msg.deleted) {
                        MessageActionItem(
                            icon = Icons.Rounded.Edit,
                            text = AppStrings.editMessage,
                            onClick = {
                                actionMessage = null
                                editContent = msg.content.orEmpty()
                                editingMessage = msg
                            },
                        )
                    }
                    if (msg.isOwn && !msg.deleted) {
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        MessageActionItem(
                            icon = Icons.Rounded.DeleteOutline,
                            text = AppStrings.deleteMessage,
                            textColor = LeoColors.destructive,
                            onClick = {
                                actionMessage = null
                                confirmDeleteMessage = msg
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { actionMessage = null }) {
                    Text(AppStrings.cancel)
                }
            },
        )
    }

    // Edit message dialog
    editingMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text(AppStrings.editMessage, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text(AppStrings.cancel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val text = editContent.trim()
                        if (text.isNotEmpty()) {
                            onEditMessage(msg.id, text)
                        }
                        editingMessage = null
                    },
                ) {
                    Text(AppStrings.save, color = LeoColors.primary, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    // Delete message confirmation dialog
    confirmDeleteMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { confirmDeleteMessage = null },
            title = { Text(AppStrings.deleteMessageTitle) },
            text = { Text(AppStrings.deleteMessageBody) },
            dismissButton = {
                TextButton(onClick = { confirmDeleteMessage = null }) {
                    Text(AppStrings.cancel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = msg.id
                        confirmDeleteMessage = null
                        onDeleteMessage(id)
                    },
                ) {
                    Text(AppStrings.delete, color = LeoColors.destructive, fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

@Composable
private fun MessageActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    textColor: androidx.compose.ui.graphics.Color = LeoThemeTokens.palette.textPrimary,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun MessageList(
    state: ChatUiState,
    avatarUrl: String?,
    onRetry: (Message) -> Unit,
    onMessageLongClick: (Message) -> Unit = {},
    onReactionClick: (messageId: String, emoji: String) -> Unit = { _, _ -> },
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
                    onRetry = { onRetry(message) },
                    onLongClick = { onMessageLongClick(message) },
                    onReactionClick = { emoji -> onReactionClick(message.id, emoji) },
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
