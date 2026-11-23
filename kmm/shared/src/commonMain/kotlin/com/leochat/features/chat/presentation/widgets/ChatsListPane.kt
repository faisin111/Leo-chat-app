package com.leochat.features.chat.presentation.widgets

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.presentation.states.ChatFilter
import com.leochat.features.chat.presentation.states.ChatState
import kotlinx.coroutines.launch

private val ComposeGlow = Color(0x33E64A19)

/** "Your chats" list: header, search, filters, conversations. Mirrors `_ChatsContent`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListPane(
    state: ChatState,
    onOpenConversation: (Conversation) -> Unit,
    onOpenSettings: () -> Unit,
    onCompose: () -> Unit,
    onSearch: (String) -> Unit,
    onFilter: (ChatFilter) -> Unit,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onRefresh()
                refreshing = false
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LeoLogo()
                Spacer(Modifier.weight(1f))
                val interaction = remember { MutableInteractionSource() }
                LeoAvatar(
                    radius = 20.dp,
                    name = state.userDisplayName,
                    imageUrl = state.currentUser?.avatarUrl,
                    modifier = Modifier.leoPressable(interaction).clickable(interactionSource = interaction, indication = null, onClick = onOpenSettings),
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        AppStrings.yourChats,
                        fontSize = 34.sp,
                        lineHeight = (34 * 1.1).sp,
                        letterSpacing = (-1.2).sp,
                        fontWeight = FontWeight.Black,
                        color = LeoColors.textPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(AppStrings.yourChatsSubtitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textSecondary)
                }
                ComposeButton(onCompose)
            }
            Spacer(Modifier.height(20.dp))
            SearchField(state.searchQuery, onSearch)
            Spacer(Modifier.height(18.dp))
            FilterBubbleBar(state.filter, state.totalUnreadCount, onFilter)
            Spacer(Modifier.height(20.dp))
            ConversationsSection(state, onOpenConversation)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FavoriteBorder, null, tint = LeoColors.textMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(AppStrings.madeForConnection, fontSize = 12.sp, color = LeoColors.textSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ComposeButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .leoPressable(interaction)
            .size(48.dp)
            .shadow(8.dp, shape, ambientColor = ComposeGlow, spotColor = ComposeGlow)
            .background(LeoColors.primary, shape)
            .clip(shape)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        Icon(Icons.Rounded.EditNote, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SearchField(query: String, onSearch: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(50.dp).background(LeoColors.input, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = LeoColors.textSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(AppStrings.searchPeopleOrMessages, color = LeoColors.textSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            BasicTextField(
                value = query,
                onValueChange = onSearch,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LeoColors.textPrimary),
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(Icons.Rounded.Close, null, tint = LeoColors.textSecondary, modifier = Modifier.size(20.dp).clickable { onSearch("") })
        }
    }
}

@Composable
private fun ConversationsSection(state: ChatState, onOpen: (Conversation) -> Unit) {
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
            target == "empty" -> Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Rounded.ChatBubbleOutline, null, tint = LeoColors.textMuted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    if (state.searchQuery.isNotEmpty()) "${AppStrings.noChatsMatching} \"${state.searchQuery}\"" else AppStrings.noConversations,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LeoColors.textSecondary,
                )
            }
            else -> Column {
                conversations.forEach { conversation ->
                    ConversationTile(
                        conversation = conversation,
                        isSelected = conversation.id == state.selectedConversationId,
                        onClick = { onOpen(conversation) },
                    )
                }
            }
        }
    }
}
