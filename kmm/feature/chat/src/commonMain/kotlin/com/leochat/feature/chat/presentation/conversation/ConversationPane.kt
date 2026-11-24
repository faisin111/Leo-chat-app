package com.leochat.feature.chat.presentation.conversation

import androidx.compose.runtime.Composable
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.presentation.ChatViewModel
import com.leochat.feature.chat.presentation.ChatUiState

/**
 * Where the conversation header can take the user. The app supplies these, so chat
 * never depends on the calls feature or on navigation.
 */
class ConversationNavigator(
    val onGroupDetails: (conversationId: String) -> Unit,
    val onSharedMedia: (conversationId: String) -> Unit,
    val onStartCall: (conversation: Conversation, video: Boolean) -> Unit,
)

/** Header, messages, and composer for the selected conversation, bound to [ChatViewModel]. */
@Composable
fun ConversationPane(
    state: ChatUiState,
    controller: ChatViewModel,
    nav: ConversationNavigator,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onComingSoon: (String) -> Unit,
) {
    ConversationContent(
        state = state,
        showBackButton = showBackButton,
        onBack = onBack,
        onSend = { text, replyToId -> controller.sendMessage(text, replyToId = replyToId) },
        onTyping = controller::sendTyping,
        onRetry = controller::retry,
        onLeave = controller::leaveConversation,
        onComingSoon = onComingSoon,
        // Flutter: groups open group details; direct chats open shared media.
        onOpenDetails = { c -> if (c.isGroup) nav.onGroupDetails(c.id) else nav.onSharedMedia(c.id) },
        onCall = nav.onStartCall,
        onVoiceRecorded = controller::sendVoice,
        onTogglePin = controller::togglePin,
        onToggleMute = controller::toggleMute,
        onToggleArchive = controller::toggleArchive,
        onEditMessage = controller::editMessage,
        onDeleteMessage = controller::deleteMessage,
        onToggleReaction = controller::toggleReaction,
    )
}
