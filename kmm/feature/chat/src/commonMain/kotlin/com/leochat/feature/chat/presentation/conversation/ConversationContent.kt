package com.leochat.feature.chat.presentation.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.chat.ChatDayLabel
import com.leochat.core.designsystem.component.chat.ChatEmptyPane
import com.leochat.core.designsystem.component.chat.JumpToLatestPill
import com.leochat.core.designsystem.component.chat.MessageSkeletonList
import com.leochat.core.designsystem.component.chat.ReconnectingBanner
import com.leochat.core.designsystem.component.chat.TypingIndicator
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.presentation.ChatUiState
import com.leochat.feature.media.domain.model.VoiceRecording
import kotlinx.coroutines.launch

/** Stateless conversation UI: header, reconnect banner, message list, and composer. Mirrors `_ConversationDetailView`. */
@Composable
fun ConversationContent(
    state: ChatUiState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
    onRetry: (Message) -> Unit,
    onLeave: (String) -> Unit,
    onComingSoon: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDetails: (Conversation) -> Unit = { onComingSoon(AppStrings.detailsComingSoon) },
    onCall: (Conversation, video: Boolean) -> Unit = { _, _ -> onComingSoon(AppStrings.callsComingSoon) },
    onVoiceRecorded: (VoiceRecording) -> Unit = {},
) {
    val conversation = state.selectedConversation
    if (conversation == null) {
        ChatEmptyPane(Icons.Outlined.Forum, 72, 32, AppStrings.noConversationSelected, AppStrings.chooseChat, modifier)
        return
    }

    Column(modifier.fillMaxSize()) {
        ConversationHeaderSection(
            conversation = conversation,
            showBackButton = showBackButton,
            onBack = onBack,
            onOpenDetails = { onOpenDetails(conversation) },
            onCall = { video -> onCall(conversation, video) },
            onLeave = { onLeave(conversation.id) },
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
                    else -> MessageList(state, conversation.preferredAvatarUrl, onRetry)
                }
            }
        }
        ChatComposerSection(
            onSend = onSend,
            onTyping = onTyping,
            onAttachmentClick = { onComingSoon(AppStrings.attachmentsComingSoon) },
            onVoiceClick = { onComingSoon(AppStrings.voiceComingSoon) },
            onVoiceRecorded = onVoiceRecorded,
        )
    }
}

@Composable
private fun MessageList(state: ChatUiState, avatarUrl: String?, onRetry: (Message) -> Unit) {
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
                MessageBubble(message, onRetry = { onRetry(message) })
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
