package com.leochat.features.chat.presentation.widgets

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.presentation.states.ChatState
import kotlinx.coroutines.launch

/** Header, reconnect banner, message list, and composer. Mirrors `_ConversationDetailView`. */
@Composable
fun ConversationDetailView(
    state: ChatState,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
    onRetry: (Message) -> Unit,
    onLeave: (String) -> Unit,
    onComingSoon: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversation = state.selectedConversation
    if (conversation == null) {
        EmptyPane(Icons.Outlined.Forum, 72, 32, AppStrings.noConversationSelected, AppStrings.chooseChat, modifier)
        return
    }

    Column(modifier.fillMaxSize()) {
        ConversationHeader(
            conversation = conversation,
            showBackButton = showBackButton,
            onBack = onBack,
            onOpenDetails = { onComingSoon(AppStrings.detailsComingSoon) },
            onCall = { onComingSoon(AppStrings.callsComingSoon) },
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
                    1 -> EmptyPane(Icons.Rounded.ChatBubbleOutline, 56, 26, AppStrings.noMessagesYet, AppStrings.sayHello)
                    else -> MessageList(state, conversation.preferredAvatarUrl, onRetry)
                }
            }
        }
        ChatComposer(
            onSend = onSend,
            onTyping = onTyping,
            onAttachmentClick = { onComingSoon(AppStrings.attachmentsComingSoon) },
            onVoiceClick = { onComingSoon(AppStrings.voiceComingSoon) },
        )
    }
}

@Composable
private fun MessageList(state: ChatState, avatarUrl: String?, onRetry: (Message) -> Unit) {
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
            item(key = "today") {
                Text(
                    AppStrings.today,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF8D827B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                )
            }
            items(messages, key = { it.id.ifEmpty { it.clientMessageId } }) { message ->
                ChatBubble(message, onRetry = { onRetry(message) })
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x1F000000), spotColor = Color(0x1F000000))
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, LeoColors.border, RoundedCornerShape(24.dp))
                    .clickable { scope.launch { listState.animateScrollToItem(lastIndex) } }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Rounded.ArrowDownward, null, tint = LeoColors.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(AppStrings.latest, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.primary)
            }
        }
    }
}

@Composable
private fun ReconnectingBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEEE3)).padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Icon(Icons.Rounded.WifiOff, null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(AppStrings.reconnecting, color = LeoColors.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun EmptyPane(icon: ImageVector, circle: Int, iconSize: Int, title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(circle.dp).background(LeoColors.input, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = LeoColors.textMuted, modifier = Modifier.size(iconSize.dp))
        }
        Spacer(Modifier.height(if (circle > 60) 16.dp else 12.dp))
        Text(title, fontSize = if (circle > 60) 18.sp else 16.sp, fontWeight = if (circle > 60) FontWeight.ExtraBold else FontWeight.Bold, color = LeoColors.textPrimary)
        Spacer(Modifier.height(if (circle > 60) 6.dp else 4.dp))
        Text(body, textAlign = TextAlign.Center, fontSize = if (circle > 60) 14.sp else 13.sp, fontWeight = FontWeight.Medium, color = LeoColors.textSecondary)
    }
}
