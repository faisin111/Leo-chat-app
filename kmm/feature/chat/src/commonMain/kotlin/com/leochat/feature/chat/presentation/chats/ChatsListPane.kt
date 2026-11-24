package com.leochat.feature.chat.presentation.chats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import com.leochat.core.designsystem.component.chat.ArchivedConversationsBar
import com.leochat.core.designsystem.component.chat.ChatSearchField
import com.leochat.core.designsystem.component.chat.ChatsEmptyState
import com.leochat.core.designsystem.component.chat.ChatsListHeader
import com.leochat.core.designsystem.component.chat.ConversationActionSheet
import com.leochat.core.designsystem.component.chat.ConversationSkeletonList
import com.leochat.core.designsystem.component.chat.ConversationTile
import com.leochat.core.designsystem.component.chat.FilterBubbleBar
import com.leochat.core.designsystem.component.chat.MadeForConnectionFooter
import com.leochat.core.designsystem.component.dialog.LeoConfirmationDialog
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.RealtimeStatus
import com.leochat.feature.chat.presentation.ChatFilter
import com.leochat.feature.chat.presentation.ChatUiState
import com.leochat.feature.chat.presentation.mapper.chatFilterOptions
import com.leochat.feature.chat.presentation.mapper.chatsEmptyMessage
import com.leochat.feature.chat.presentation.mapper.toTileUi
import kotlinx.coroutines.launch

/** "Your chats" list: header, search, filters, conversations. Mirrors `_ChatsContent`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListPane(
    state: ChatUiState,
    onOpenConversation: (Conversation) -> Unit,
    onOpenSettings: () -> Unit,
    onCompose: () -> Unit,
    onSearch: (String) -> Unit,
    onFilter: (ChatFilter) -> Unit,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    onTogglePin: (String) -> Unit = {},
    onToggleMute: (String) -> Unit = {},
    onToggleArchive: (String) -> Unit = {},
    onDeleteConversation: (String) -> Unit = {},
) {
    val palette = LeoThemeTokens.palette
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var actionConversation by remember { mutableStateOf<Conversation?>(null) }
    var confirmDeleteConversation by remember { mutableStateOf<Conversation?>(null) }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onRefresh()
                refreshing = false
            }
        },
        modifier = modifier.fillMaxSize().background(palette.background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(palette.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
        ) {
            ChatsListHeader(
                userName = state.userDisplayName,
                avatarUrl = state.currentUser?.avatarUrl,
                onOpenSettings = onOpenSettings,
                onCompose = onCompose,
                isConnected = state.realtimeStatus == RealtimeStatus.Connected,
            )
            Spacer(Modifier.height(20.dp))
            ChatSearchField(state.searchQuery, onSearch)
            Spacer(Modifier.height(18.dp))
            FilterBubbleBar(chatFilterOptions(state.totalUnreadCount), state.filter, onFilter)
            Spacer(Modifier.height(20.dp))
            ConversationsSection(
                state = state,
                onOpen = onOpenConversation,
                onLongClick = { actionConversation = it },
            )
            Spacer(Modifier.height(16.dp))
            ArchivedConversationsBar(
                isActive = state.filter == ChatFilter.Archived,
                onClick = {
                    onFilter(if (state.filter == ChatFilter.Archived) ChatFilter.All else ChatFilter.Archived)
                },
            )
            Spacer(Modifier.height(24.dp))
            MadeForConnectionFooter()
        }
    }

    actionConversation?.let { conv ->
        val isArchived = conv.status.equals("ARCHIVED", ignoreCase = true)
        ConversationActionSheet(
            conversationTitle = conv.effectiveTitle,
            avatarUrl = conv.avatarUrl,
            lastSnippet = conv.lastMessageSnippet,
            isPinned = conv.isPinned,
            isMuted = conv.isMuted,
            isArchived = isArchived,
            isGroup = conv.isGroup,
            onTogglePin = {
                val id = conv.id
                actionConversation = null
                onTogglePin(id)
            },
            onToggleMute = {
                val id = conv.id
                actionConversation = null
                onToggleMute(id)
            },
            onToggleArchive = {
                val id = conv.id
                actionConversation = null
                onToggleArchive(id)
            },
            onDelete = {
                actionConversation = null
                confirmDeleteConversation = conv
            },
            onDismiss = { actionConversation = null },
        )
    }

    confirmDeleteConversation?.let { conv ->
        val (titleText, body, action) = when {
            conv.isGroup -> Triple(AppStrings.leaveGroupTitle, AppStrings.leaveGroupBody, AppStrings.leave)
            else -> Triple(AppStrings.deleteConversationTitle, AppStrings.deleteConversationBody, AppStrings.delete)
        }
        LeoConfirmationDialog(
            title = titleText,
            message = body,
            confirmText = action,
            cancelText = AppStrings.cancel,
            onConfirm = {
                val id = conv.id
                confirmDeleteConversation = null
                onDeleteConversation(id)
            },
            onDismiss = { confirmDeleteConversation = null },
        )
    }
}

@Composable
private fun ConversationsSection(
    state: ChatUiState,
    onOpen: (Conversation) -> Unit,
    onLongClick: (Conversation) -> Unit,
) {
    val conversations = state.displayedConversations
    val pane = when {
        state.isLoadingConversations -> "loading"
        conversations.isEmpty() -> "empty"
        else -> "list:${state.filter}:${state.searchQuery}"
    }
    AnimatedContent(
        targetState = pane,
        transitionSpec = {
            (fadeIn(tween(280, easing = EaseOutCubic)) + slideInHorizontally(tween(280, easing = EaseOutCubic)) { it / 50 }) togetherWith
                fadeOut(tween(280, easing = EaseInCubic))
        },
        label = "conversations",
    ) { target ->
        when {
            target == "loading" -> ConversationSkeletonList()
            target == "empty" -> ChatsEmptyState(chatsEmptyMessage(state.searchQuery))
            else -> Column {
                conversations.forEach { conversation ->
                    ConversationTile(
                        tile = conversation.toTileUi(),
                        isSelected = conversation.id == state.selectedConversationId,
                        onClick = { onOpen(conversation) },
                        onLongClick = { onLongClick(conversation) },
                    )
                }
            }
        }
    }
}
