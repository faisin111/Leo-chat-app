package com.leochat.feature.home.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.component.navigation.ShellTab
import com.leochat.core.designsystem.responsive.LeoLayout
import com.leochat.core.designsystem.responsive.LocalLeoLayout
import com.leochat.core.navigation.AppDeepLink
import com.leochat.core.navigation.DeepLinkBus
import com.leochat.feature.ai.presentation.ai.AiScreen
import com.leochat.feature.calls.domain.model.CallType
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.presentation.ChatViewModel
import com.leochat.feature.chat.presentation.chats.ChatsListPane
import com.leochat.feature.chat.presentation.conversation.ConversationNavigator
import com.leochat.feature.chat.presentation.conversation.ConversationPane
import com.leochat.feature.chat.presentation.newconversation.NewConversationScreen
import com.leochat.feature.notifications.presentation.NotificationsScreen
import com.leochat.feature.notifications.presentation.NotificationsViewModel
import com.leochat.feature.settings.presentation.settings.SettingsScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Routes the shell and its conversation pane can open. Wired in `AppNavHost`. */
class ShellNavigator(
    val onOpenConversation: () -> Unit,
    val onSignedOut: () -> Unit,
    val onNewGroup: () -> Unit,
    val onGroupDetails: (conversationId: String) -> Unit,
    val onSharedMedia: (conversationId: String) -> Unit,
    val onCall: () -> Unit,
    val onEditProfile: () -> Unit,
    val onBlockedPeople: () -> Unit,
    val onSecurity: () -> Unit,
    val onSessions: () -> Unit,
    val onAppearance: () -> Unit,
)

/**
 * `/app`. Mobile: tab content + bottom bar, conversations open full screen.
 * Tablet/desktop: rail + 360dp list + detail. Mirrors `ChatShellPage`.
 */
@Composable
fun HomeScreen(
    controller: ChatViewModel,
    nav: ShellNavigator,
    notificationsController: NotificationsViewModel = koinViewModel(),
) {
    val conversationNav = rememberConversationNavigator(nav, chatController = controller)
    val state by controller.state.collectAsStateWithLifecycle()
    val notificationsState by notificationsController.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    var tab by rememberSaveable { mutableStateOf(ShellTab.Chats) }
    val isMobile = LocalLeoLayout.current == LeoLayout.Mobile
    LaunchedEffect(state.signedOut) { if (state.signedOut) nav.onSignedOut() }
    LaunchedEffect(tab) {
        if (tab == ShellTab.Notifications) notificationsController.refresh(showSpinner = false)
    }

    val pendingDeepLink by DeepLinkBus.pending.collectAsStateWithLifecycle()
    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink is AppDeepLink.OpenConversation) {
            val link = DeepLinkBus.consume() as? AppDeepLink.OpenConversation ?: return@LaunchedEffect
            controller.openConversationById(link.conversationId, fallbackTitle = link.title)
            tab = ShellTab.Chats
            if (isMobile) nav.onOpenConversation()
        }
    }

    LaunchedEffect(state.selectedConversationId, tab, isMobile) {
        DeepLinkBus.setActiveConversation(
            if (tab == ShellTab.Chats && !isMobile) state.selectedConversationId else null,
        )
    }

    val chatsList: @Composable (Modifier, (Conversation) -> Unit) -> Unit = { modifier, open ->
        ChatsListPane(
            state = state,
            onOpenConversation = open,
            onOpenSettings = { tab = ShellTab.Settings },
            onCompose = { tab = ShellTab.Contacts },
            onSearch = controller::search,
            onFilter = controller::setFilter,
            onRefresh = { controller.refresh().join() },
            onTogglePin = controller::togglePin,
            onToggleMute = controller::toggleMute,
            onToggleArchive = controller::toggleArchive,
            onDeleteConversation = controller::deleteConversation,
            modifier = modifier,
        )
    }
    // Contacts → open the chat: refresh the list, select it, and show it (full screen on mobile).
    val openFromContacts: (Conversation) -> Unit = { conversation ->
        controller.refresh()
        controller.selectConversation(conversation)
        tab = ShellTab.Chats
        if (isMobile) nav.onOpenConversation()
    }
    val openConversationByIdFromTab: (String) -> Unit = { conversationId ->
        controller.openConversationById(conversationId)
        tab = ShellTab.Chats
        if (isMobile) nav.onOpenConversation()
    }
    val otherTab: @Composable (ShellTab) -> Unit = { t ->
        when (t) {
            ShellTab.Ai -> AiScreen()
            ShellTab.Contacts -> NewConversationScreen(
                onBack = null,
                onConversationOpened = openFromContacts,
                onNewGroup = nav.onNewGroup,
            )
            ShellTab.Notifications -> NotificationsScreen(
                onOpenConversation = openConversationByIdFromTab,
                controller = notificationsController,
            )
            else -> SettingsScreen(
                onEditProfile = nav.onEditProfile,
                onBlockedPeople = nav.onBlockedPeople,
                onSecurity = nav.onSecurity,
                onSessions = nav.onSessions,
                onAppearance = nav.onAppearance,
                onSignedOut = nav.onSignedOut,
            )
        }
    }
    val totalChatUnread = state.conversations.sumOf { it.unreadCount }
    val tabBadges = buildMap {
        if (totalChatUnread > 0) put(ShellTab.Chats, totalChatUnread)
        if (notificationsState.unreadCount > 0) put(ShellTab.Notifications, notificationsState.unreadCount)
    }

    HomeContent(
        selectedTab = tab,
        isMobile = isMobile,
        onSelectTab = { tab = it },
        badges = tabBadges,
        chatsList = { modifier ->
            if (isMobile) {
                chatsList(modifier) { conversation ->
                    controller.selectConversation(conversation)
                    nav.onOpenConversation()
                }
            } else {
                chatsList(modifier, controller::selectConversation)
            }
        },
        conversationPane = {
            ConversationPane(state, controller, conversationNav, showBackButton = false, onBack = {}, onComingSoon = { snackBar.showInfo(it) })
        },
        otherTab = otherTab,
    )
}

/** Conversation header actions for the shell: details routes plus 1-on-1 call start through [CallController]. */
@Composable
fun rememberConversationNavigator(
    nav: ShellNavigator,
    chatController: ChatViewModel,
    callController: CallController = koinInject(),
): ConversationNavigator {
    val scope = rememberCoroutineScope()
    val snackBar = LocalLeoSnackBar.current
    return remember(nav, chatController, callController, snackBar) {
        ConversationNavigator(
            onGroupDetails = nav.onGroupDetails,
            onSharedMedia = nav.onSharedMedia,
            onStartCall = { c, video ->
                if (c.isGroup || c.isAi) {
                    snackBar.showInfo(CallStrings.groupCallsUnsupported)
                } else {
                    scope.launch {
                        val targetId = chatController.resolveCallTargetUserId(c)
                        if (targetId.isNullOrBlank()) {
                            snackBar.showError(CallStrings.participantNotFound)
                            return@launch
                        }
                        callController.startCall(
                            conversationId = c.id,
                            targetUserId = targetId,
                            targetUserName = c.effectiveTitle,
                            callType = if (video) CallType.VIDEO else CallType.AUDIO,
                            targetAvatarUrl = c.preferredAvatarUrl,
                        )
                        nav.onCall()
                    }
                }
            },
        )
    }
}
