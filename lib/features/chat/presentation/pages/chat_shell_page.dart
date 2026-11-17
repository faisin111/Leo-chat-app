import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/network/stomp_service.dart';
import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../ai/presentation/pages/ai_page.dart';
import '../../../calls/domain/entities/call_session.dart';
import '../../../calls/presentation/pages/active_call_page.dart';
import '../../../calls/presentation/providers/call_notifier.dart';
import '../../../calls/presentation/widgets/incoming_call_dialog.dart';
import '../../../settings/presentation/pages/settings_page.dart';
import '../../domain/entities/conversation.dart';
import '../providers/chat_notifier.dart';
import '../providers/chat_state.dart';
import '../widgets/chat_bubble.dart';
import '../widgets/chat_composer.dart';
import '../widgets/conversation_skeleton.dart';
import '../widgets/conversation_tile.dart';
import '../widgets/filter_bubble_bar.dart';
import '../widgets/message_skeleton.dart';
import '../widgets/typing_indicator.dart';
import 'group_details_page.dart';
import 'new_conversation_page.dart';
import 'shared_media_page.dart';

class ChatShellPage extends ConsumerStatefulWidget {
  const ChatShellPage({super.key});

  @override
  ConsumerState<ChatShellPage> createState() => _ChatShellPageState();
}

class _ChatShellPageState extends ConsumerState<ChatShellPage> {
  int _selectedTab = 0;

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(chatNotifierProvider);
    final isMobile =
        layoutForWidth(MediaQuery.sizeOf(context).width) == LeoLayout.mobile;

    if (!isMobile) {
      return Scaffold(
        backgroundColor: const Color(0xFFFAF7F4),
        body: Row(
          children: [
            Container(
              width: 72,
              color: LeoColors.rail,
              child: const _DesktopRail(),
            ),
            SizedBox(
              width: 360,
              child: _ChatsContent(
                chatState: chatState,
                onOpenConversation: (conversation) {
                  ref
                      .read(chatNotifierProvider.notifier)
                      .selectConversation(conversation);
                },
              ),
            ),
            const VerticalDivider(width: 1, color: Color(0xFFE7DFD8)),
            Expanded(
              child: _ConversationDetailView(
                conversation: chatState.selectedConversation,
              ),
            ),
          ],
        ),
      );
    }

    Widget bodyContent;
    switch (_selectedTab) {
      case 1:
        bodyContent = const AiPage();
        break;
      case 2:
        bodyContent = const NewConversationPage();
        break;
      case 3:
        bodyContent = const SettingsPage();
        break;
      case 0:
      default:
        bodyContent = _ChatsContent(
          chatState: chatState,
          onOpenConversation: (conversation) {
            ref
                .read(chatNotifierProvider.notifier)
                .selectConversation(conversation);
            Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => _ConversationPage(
                  conversationId: conversation.id,
                ),
              ),
            );
          },
        );
        break;
    }

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      body: SafeArea(
        child: Stack(
          children: [
            bodyContent,
            const Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: IncomingCallBanner(),
            ),
          ],
        ),
      ),
      bottomNavigationBar: _BottomNav(
        selectedIndex: _selectedTab,
        onTap: (index) {
          setState(() => _selectedTab = index);
        },
      ),
    );
  }
}

class _ChatsContent extends ConsumerStatefulWidget {
  const _ChatsContent({
    required this.chatState,
    required this.onOpenConversation,
  });

  final ChatState chatState;
  final ValueChanged<Conversation> onOpenConversation;

  @override
  ConsumerState<_ChatsContent> createState() => _ChatsContentState();
}

class _ChatsContentState extends ConsumerState<_ChatsContent> {
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final chatState = widget.chatState;
    final conversations = chatState.displayedConversations;

    return RefreshIndicator(
      color: LeoColors.primary,
      backgroundColor: Colors.white,
      displacement: 28,
      onRefresh: () =>
          ref.read(chatNotifierProvider.notifier).loadConversations(
                query: chatState.searchQuery.trim().isEmpty
                    ? null
                    : chatState.searchQuery.trim(),
              ),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Top Bar: Logo & User Avatar
            const Row(
              children: [
                LeoLogo(),
                Spacer(),
                LeoAvatar(
                  radius: 20,
                  assetPath: 'assets/avatars/Portrait.png',
                ),
              ],
            ),
            const SizedBox(height: 20),
            // Header Section: "Your chats" & Compose FAB
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        AppStrings.yourChats,
                        style: TextStyle(
                          fontSize: 34,
                          height: 1.1,
                          letterSpacing: -1.2,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF1C1917),
                        ),
                      ),
                      SizedBox(height: 4),
                      Text(
                        AppStrings.yourChatsSubtitle,
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF78716C),
                        ),
                      ),
                    ],
                  ),
                ),
                LeoPressable(
                  onPressed: () {
                    Navigator.of(context).push(
                      MaterialPageRoute<void>(
                        builder: (_) => const NewConversationPage(),
                      ),
                    );
                  },
                  child: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: LeoColors.primary,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: const [
                        BoxShadow(
                          color: Color(0x33E64A19),
                          blurRadius: 14,
                          offset: Offset(0, 6),
                        ),
                      ],
                    ),
                    child: const Icon(
                      Icons.edit_square,
                      color: Colors.white,
                      size: 22,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            // Search Bar
            Container(
              height: 50,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                color: const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  const Icon(
                    Icons.search_rounded,
                    color: Color(0xFF78716C),
                    size: 22,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      controller: _searchController,
                      onChanged: (val) {
                        ref.read(chatNotifierProvider.notifier).search(val);
                      },
                      decoration: const InputDecoration(
                        hintText: AppStrings.searchPeopleOrMessages,
                        hintStyle: TextStyle(
                          color: Color(0xFF78716C),
                          fontSize: 15,
                          fontWeight: FontWeight.w500,
                        ),
                        border: InputBorder.none,
                        isDense: true,
                        contentPadding: EdgeInsets.zero,
                      ),
                    ),
                  ),
                  if (_searchController.text.isNotEmpty)
                    GestureDetector(
                      onTap: () {
                        _searchController.clear();
                        ref.read(chatNotifierProvider.notifier).search('');
                      },
                      child: const Icon(
                        Icons.close_rounded,
                        color: Color(0xFF78716C),
                        size: 20,
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            // Filter Bubble Bar
            LeoFilterBubbleBar(
              selectedIndex: chatState.filterIndex,
              unreadCount: chatState.totalUnreadCount,
              onSelect: (index) =>
                  ref.read(chatNotifierProvider.notifier).setFilter(index),
            ),
            const SizedBox(height: 20),
            // Loading Skeleton View vs Populated Conversations List
            AnimatedSwitcher(
              duration: const Duration(milliseconds: 250),
              child: chatState.isLoadingConversations
                  ? const LeoConversationSkeletonList(
                      key: ValueKey('conversations_loading'),
                      itemCount: 6,
                    )
                  : conversations.isEmpty
                      ? Padding(
                          key: const ValueKey('conversations_empty'),
                          padding: const EdgeInsets.symmetric(vertical: 40),
                          child: Center(
                            child: Column(
                              children: [
                                const Icon(
                                  Icons.chat_bubble_outline_rounded,
                                  size: 48,
                                  color: Color(0xFFA8A19B),
                                ),
                                const SizedBox(height: 12),
                                Text(
                                  chatState.searchQuery.isNotEmpty
                                      ? 'No chats matching "${chatState.searchQuery}"'
                                      : 'No conversations yet',
                                  style: const TextStyle(
                                    fontSize: 15,
                                    fontWeight: FontWeight.w600,
                                    color: Color(0xFF78716C),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        )
                      : AnimatedSwitcher(
                          key: ValueKey(
                              'conversations_switcher_${chatState.filterIndex}'),
                          duration: const Duration(milliseconds: 280),
                          switchInCurve: Curves.easeOutCubic,
                          switchOutCurve: Curves.easeInCubic,
                          transitionBuilder: (child, animation) {
                            return FadeTransition(
                              opacity: animation,
                              child: SlideTransition(
                                position: Tween<Offset>(
                                  begin: const Offset(0.02, 0),
                                  end: Offset.zero,
                                ).animate(animation),
                                child: child,
                              ),
                            );
                          },
                          child: Column(
                            key: ValueKey(
                                'filter_${chatState.filterIndex}_query_${chatState.searchQuery}'),
                            children: conversations
                                .map(
                                  (conv) => LeoConversationTile(
                                    conversation: conv,
                                    isSelected: conv.id ==
                                        chatState.selectedConversationId,
                                    onTap: () =>
                                        widget.onOpenConversation(conv),
                                  ),
                                )
                                .toList(),
                          ),
                        ),
            ),
            const SizedBox(height: 24),
            // Footer branding
            const Center(
              child: Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                alignment: WrapAlignment.center,
                spacing: 8,
                children: [
                  Icon(
                    Icons.favorite_border_rounded,
                    size: 15,
                    color: Color(0xFFA8A19B),
                  ),
                  Text(
                    AppStrings.madeForConnection,
                    style: TextStyle(
                      fontSize: 12,
                      color: Color(0xFF78716C),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ConversationPage extends ConsumerWidget {
  const _ConversationPage({required this.conversationId});

  final String conversationId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final chatState = ref.watch(chatNotifierProvider);
    return Scaffold(
      backgroundColor: LeoColors.background,
      body: SafeArea(
        child: _ConversationDetailView(
          conversation: chatState.selectedConversation,
          showBackButton: true,
        ),
      ),
    );
  }
}

class _ConversationDetailView extends ConsumerStatefulWidget {
  const _ConversationDetailView({
    required this.conversation,
    this.showBackButton = false,
  });

  final Conversation? conversation;
  final bool showBackButton;

  @override
  ConsumerState<_ConversationDetailView> createState() =>
      _ConversationDetailViewState();
}

class _ConversationDetailViewState
    extends ConsumerState<_ConversationDetailView> {
  final ScrollController _scrollController = ScrollController();
  bool _showScrollToBottomButton = false;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _scrollToBottom(animated: false);
    });
  }

  @override
  void didUpdateWidget(covariant _ConversationDetailView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.conversation?.id != oldWidget.conversation?.id) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _scrollToBottom(animated: false);
      });
    }
  }

  void _onScroll() {
    if (!_scrollController.hasClients) return;
    final maxScroll = _scrollController.position.maxScrollExtent;
    final currentScroll = _scrollController.position.pixels;
    final isFarFromBottom = (maxScroll - currentScroll) > 180;
    if (isFarFromBottom != _showScrollToBottomButton) {
      setState(() => _showScrollToBottomButton = isFarFromBottom);
    }
  }

  void _scrollToBottom({bool animated = true}) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted || !_scrollController.hasClients) return;
      final maxScroll = _scrollController.position.maxScrollExtent;
      if (animated) {
        _scrollController.animateTo(
          maxScroll,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOutCubic,
        );
      } else {
        _scrollController.jumpTo(maxScroll);
      }
    });
  }

  @override
  void dispose() {
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(chatNotifierProvider);
    final notifier = ref.read(chatNotifierProvider.notifier);
    final conv = widget.conversation;

    // Auto-scroll on every new message received or sent, or when typing indicator appears
    ref.listen<ChatState>(chatNotifierProvider, (previous, next) {
      final prevCount = previous?.messages.length ?? 0;
      final nextCount = next.messages.length;
      final prevTyping = previous?.typingStatusText;
      final nextTyping = next.typingStatusText;
      final wasLoading = previous?.isLoadingMessages ?? false;
      final isNowLoaded = !next.isLoadingMessages;

      if (wasLoading && isNowLoaded) {
        _scrollToBottom(animated: false);
      } else if (nextCount > prevCount) {
        _scrollToBottom(animated: true);
      } else if (prevTyping == null && nextTyping != null) {
        _scrollToBottom(animated: true);
      } else if (nextCount == prevCount && nextCount > 0) {
        final prevLast = previous?.messages.isNotEmpty == true
            ? previous!.messages.last
            : null;
        final nextLast = next.messages.last;
        if (prevLast?.id != nextLast.id ||
            prevLast?.sendStatus != nextLast.sendStatus) {
          _scrollToBottom(animated: true);
        }
      }
    });

    if (conv == null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                width: 72,
                height: 72,
                decoration: const BoxDecoration(
                  color: Color(0xFFF3ECE5),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.forum_outlined,
                  size: 32,
                  color: Color(0xFFA8A19B),
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'No conversation selected',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF1C1917),
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Choose a chat from the left to start messaging.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF78716C),
                ),
              ),
            ],
          ),
        ),
      );
    }

    final isConnecting =
        chatState.realtimeStatus == RealtimeConnectionStatus.connecting ||
            chatState.realtimeStatus == RealtimeConnectionStatus.reconnecting;

    return Column(
      children: [
        _ConversationHeader(
          conversation: conv,
          showBackButton: widget.showBackButton,
        ),
        if (isConnecting)
          Container(
            width: double.infinity,
            color: const Color(0xFFFFEEE3),
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            child: const Row(
              children: [
                Icon(
                  Icons.wifi_off_rounded,
                  color: LeoColors.primary,
                  size: 20,
                ),
                SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Reconnecting... Your messages are safe.',
                    style: TextStyle(
                      color: LeoColors.primary,
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
          ),
        Expanded(
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 250),
            child: chatState.isLoadingMessages
                ? const LeoMessageSkeletonList(
                    key: ValueKey('messages_loading'),
                  )
                : chatState.messages.isEmpty
                    ? Center(
                        key: const ValueKey('messages_empty'),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 40),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Container(
                                width: 56,
                                height: 56,
                                decoration: const BoxDecoration(
                                  color: Color(0xFFF3ECE5),
                                  shape: BoxShape.circle,
                                ),
                                child: const Icon(
                                  Icons.chat_bubble_outline_rounded,
                                  size: 26,
                                  color: Color(0xFFA8A19B),
                                ),
                              ),
                              const SizedBox(height: 12),
                              const Text(
                                'No messages yet',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w700,
                                  color: Color(0xFF1C1917),
                                ),
                              ),
                              const SizedBox(height: 4),
                              const Text(
                                'Send a message to say hello!',
                                style: TextStyle(
                                  fontSize: 13,
                                  color: Color(0xFF78716C),
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                    : Stack(
                        key: ValueKey('messages_content_${conv.id}'),
                        children: [
                          ListView(
                            controller: _scrollController,
                            padding: const EdgeInsets.fromLTRB(28, 20, 28, 16),
                            children: [
                              const Center(
                                child: Text(
                                  'TODAY',
                                  style: TextStyle(
                                    color: Color(0xFF8D827B),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w700,
                                    letterSpacing: .4,
                                  ),
                                ),
                              ),
                              const SizedBox(height: 20),
                              ...chatState.messages.map(
                                (msg) => LeoChatBubble(
                                  key: ValueKey(msg.id.isNotEmpty
                                      ? msg.id
                                      : msg.clientMessageId),
                                  message: msg,
                                  onRetry: () {
                                    if (msg.type == 'VOICE' ||
                                        msg.type == 'AUDIO') {
                                      notifier.sendVoice(msg.content ?? '');
                                    } else {
                                      notifier.sendMessage(msg.content ?? '',
                                          type: msg.type);
                                    }
                                  },
                                ),
                              ),
                              if (chatState.typingStatusText != null) ...[
                                const SizedBox(height: 10),
                                LeoTypingIndicator(
                                  text: chatState.typingStatusText,
                                  avatarUrl: conv.preferredAvatarUrl,
                                ),
                              ],
                            ],
                          ),
                          if (_showScrollToBottomButton)
                            Positioned(
                              bottom: 12,
                              right: 24,
                              child: AnimatedOpacity(
                                opacity: _showScrollToBottomButton ? 1.0 : 0.0,
                                duration: const Duration(milliseconds: 200),
                                child: LeoPressable(
                                  child: InkWell(
                                    onTap: () =>
                                        _scrollToBottom(animated: true),
                                    borderRadius: BorderRadius.circular(24),
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 14, vertical: 8),
                                      decoration: BoxDecoration(
                                        color: Colors.white,
                                        borderRadius: BorderRadius.circular(24),
                                        border: Border.all(
                                            color: const Color(0xFFE7DFD8)),
                                        boxShadow: const [
                                          BoxShadow(
                                            color: Color(0x1F000000),
                                            blurRadius: 10,
                                            offset: Offset(0, 4),
                                          ),
                                        ],
                                      ),
                                      child: const Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Icon(
                                            Icons.arrow_downward_rounded,
                                            size: 16,
                                            color: LeoColors.primary,
                                          ),
                                          SizedBox(width: 5),
                                          Text(
                                            'Latest',
                                            style: TextStyle(
                                              fontSize: 12,
                                              fontWeight: FontWeight.w800,
                                              color: LeoColors.primary,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            ),
                        ],
                      ),
          ),
        ),
        LeoChatComposer(
          onSend: (text) => notifier.sendMessage(text),
          onVoiceRecorded: notifier.sendVoice,
          onTyping: (isTyping) => notifier.sendTyping(isTyping),
        ),
      ],
    );
  }
}

class _ConversationHeader extends ConsumerWidget {
  const _ConversationHeader({
    required this.conversation,
    this.showBackButton = false,
  });

  final Conversation conversation;
  final bool showBackButton;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final isAi = conversation.isAi;
    final title = isAi ? 'Leo AI Assistant' : conversation.effectiveTitle;
    final subtitle = isAi
        ? 'Always active • Gemini 2.5 Flash'
        : (conversation.isGroup
            ? 'Group conversation'
            : (conversation.isOnline ? 'Online' : 'Offline'));

    return Container(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 16),
      decoration: BoxDecoration(
        color: isDark ? LeoColors.darkSurface : Colors.white,
        border: Border(
          bottom: BorderSide(
            color: isDark ? LeoColors.darkBorder : const Color(0xFFE7DFD8),
          ),
        ),
      ),
      child: Row(
        children: [
          if (showBackButton)
            LeoPressable(
              child: IconButton(
                onPressed: () => Navigator.of(context).pop(),
                icon: Icon(
                  Icons.arrow_back_rounded,
                  color: isDark ? LeoColors.darkTextPrimary : LeoColors.textPrimary,
                ),
              ),
            ),
          Expanded(
            child: InkWell(
              onTap: () {
                if (isAi) return;
                if (conversation.isGroup) {
                  Navigator.of(context).push(
                    MaterialPageRoute<void>(
                      builder: (_) => GroupDetailsPage(
                        groupName: title,
                      ),
                    ),
                  );
                } else {
                  Navigator.of(context).push(
                    MaterialPageRoute<void>(
                      builder: (_) => SharedMediaPage(
                        conversationTitle: 'Shared with $title',
                      ),
                    ),
                  );
                }
              },
              borderRadius: BorderRadius.circular(16),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    if (isAi)
                      Container(
                        width: 52,
                        height: 52,
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(
                            colors: [Color(0xFFFF6D00), Color(0xFFE64A19)],
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                          ),
                          borderRadius: BorderRadius.circular(16),
                          boxShadow: [
                            BoxShadow(
                              color: LeoColors.primary.withOpacity(0.3),
                              blurRadius: 10,
                              offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: const Icon(
                          Icons.auto_awesome,
                          color: Colors.white,
                          size: 26,
                        ),
                      )
                    else
                      LeoAvatar(
                        radius: 26,
                        imageUrl: conversation.preferredAvatarUrl,
                        assetPath: 'assets/avatars/Portrait.png',
                        hasOnlineBadge: conversation.isOnline,
                      ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Flexible(
                                child: Text(
                                  title,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w900,
                                    color: isDark
                                        ? LeoColors.darkTextPrimary
                                        : LeoColors.textPrimary,
                                  ),
                                ),
                              ),
                              if (isAi) ...[
                                const SizedBox(width: 6),
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 6, vertical: 1.5),
                                  decoration: BoxDecoration(
                                    color: LeoColors.primarySoft,
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: const Text(
                                    'AI',
                                    style: TextStyle(
                                      color: LeoColors.primary,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                          const SizedBox(height: 3),
                          Text(
                            subtitle,
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: isDark
                                  ? LeoColors.darkTextSecondary
                                  : LeoColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          if (isAi)
            LeoPressable(
              child: IconButton(
                icon: Icon(
                  Icons.delete_outline_rounded,
                  color: isDark
                      ? LeoColors.darkTextSecondary
                      : LeoColors.textSecondary,
                  size: 22,
                ),
                tooltip: 'Clear conversation',
                onPressed: () async {
                  final confirm = await showDialog<bool>(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      title: const Text('Clear AI Conversation?'),
                      content: const Text(
                          'This will permanently delete your AI chat history.'),
                      actions: [
                        TextButton(
                          onPressed: () => Navigator.of(ctx).pop(false),
                          child: const Text('Cancel'),
                        ),
                        TextButton(
                          onPressed: () => Navigator.of(ctx).pop(true),
                          child: const Text(
                            'Delete',
                            style: TextStyle(color: LeoColors.destructive),
                          ),
                        ),
                      ],
                    ),
                  );
                  if (confirm == true) {
                    await ref
                        .read(chatNotifierProvider.notifier)
                        .leaveConversation(conversation.id);
                  }
                },
              ),
            )
          else ...[
            LeoPressable(
              onPressed: () {
                ref.read(callNotifierProvider.notifier).startCall(
                      conversationId: conversation.id,
                      targetUserId:
                          conversation.targetUserId ?? conversation.id,
                      targetUserName: conversation.effectiveTitle,
                      targetAvatarUrl: conversation.preferredAvatarUrl,
                      callType: CallType.audio,
                    );
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => const ActiveCallPage(),
                  ),
                );
              },
              child: Icon(
                Icons.phone_outlined,
                color: isDark
                    ? LeoColors.darkTextPrimary
                    : LeoColors.textPrimary,
                size: 22,
              ),
            ),
            const SizedBox(width: 16),
            LeoPressable(
              onPressed: () {
                ref.read(callNotifierProvider.notifier).startCall(
                      conversationId: conversation.id,
                      targetUserId:
                          conversation.targetUserId ?? conversation.id,
                      targetUserName: conversation.effectiveTitle,
                      targetAvatarUrl: conversation.preferredAvatarUrl,
                      callType: CallType.video,
                    );
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => const ActiveCallPage(),
                  ),
                );
              },
              child: Icon(
                Icons.videocam_outlined,
                color: isDark
                    ? LeoColors.darkTextPrimary
                    : LeoColors.textPrimary,
                size: 24,
              ),
            ),
            const SizedBox(width: 6),
            PopupMenuButton<String>(
              icon: Icon(
                Icons.more_vert_rounded,
                color: isDark
                    ? LeoColors.darkTextSecondary
                    : LeoColors.textSecondary,
              ),
              onSelected: (val) async {
                if (val == 'leave') {
                  final isGroup = conversation.isGroup;
                  final confirm = await showDialog<bool>(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      title: Text(isGroup ? 'Leave Group?' : 'Delete Conversation?'),
                      content: Text(isGroup
                          ? 'Are you sure you want to leave this group?'
                          : 'Are you sure you want to delete this conversation?'),
                      actions: [
                        TextButton(
                          onPressed: () => Navigator.of(ctx).pop(false),
                          child: const Text('Cancel'),
                        ),
                        TextButton(
                          onPressed: () => Navigator.of(ctx).pop(true),
                          child: Text(
                            isGroup ? 'Leave' : 'Delete',
                            style: const TextStyle(color: LeoColors.destructive),
                          ),
                        ),
                      ],
                    ),
                  );
                  if (confirm == true) {
                    await ref
                        .read(chatNotifierProvider.notifier)
                        .leaveConversation(conversation.id);
                  }
                }
              },
              itemBuilder: (ctx) => [
                PopupMenuItem(
                  value: 'leave',
                  child: Row(
                    children: [
                      const Icon(Icons.delete_outline_rounded,
                          color: LeoColors.destructive, size: 20),
                      const SizedBox(width: 8),
                      Text(
                        conversation.isGroup ? 'Leave Group' : 'Delete Chat',
                        style: const TextStyle(
                          color: LeoColors.destructive,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class _BottomNav extends StatelessWidget {
  const _BottomNav({required this.selectedIndex, required this.onTap});

  final int selectedIndex;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      decoration: BoxDecoration(
        color: isDark ? LeoColors.darkSurface : const Color(0xFFFAF7F4),
        border: Border(
          top: BorderSide(
            color: isDark ? LeoColors.darkBorder : const Color(0xFFE7DFD8),
            width: 1,
          ),
        ),
      ),
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _NavItem(
            icon: Icons.chat_bubble_outline_rounded,
            activeIcon: Icons.chat_bubble_rounded,
            label: AppStrings.chats,
            active: selectedIndex == 0,
            onTap: () => onTap(0),
          ),
          _NavItem(
            icon: Icons.auto_awesome_outlined,
            activeIcon: Icons.auto_awesome,
            label: AppStrings.aiAssistant,
            active: selectedIndex == 1,
            onTap: () => onTap(1),
          ),
          _NavItem(
            icon: Icons.people_outline_rounded,
            activeIcon: Icons.people_rounded,
            label: AppStrings.contacts,
            active: selectedIndex == 2,
            onTap: () => onTap(2),
          ),
          _NavItem(
            icon: Icons.tune_rounded,
            activeIcon: Icons.tune_rounded,
            label: AppStrings.settings,
            active: selectedIndex == 3,
            onTap: () => onTap(3),
          ),
        ],
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.activeIcon,
    required this.label,
    required this.active,
    required this.onTap,
  });

  final IconData icon;
  final IconData activeIcon;
  final String label;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => LeoPressable(
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  active ? activeIcon : icon,
                  color: active ? LeoColors.primary : const Color(0xFF78716C),
                  size: 24,
                ),
                const SizedBox(height: 3),
                Text(
                  label,
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: active ? FontWeight.w800 : FontWeight.w600,
                    color: active ? LeoColors.primary : const Color(0xFF78716C),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
}

class _DesktopRail extends StatelessWidget {
  const _DesktopRail();

  @override
  Widget build(BuildContext context) => const Column(
        children: [
          SizedBox(height: 24),
          LeoLogo(compact: true, onDark: true),
          Spacer(),
          Icon(Icons.chat_bubble_rounded, color: Colors.white, size: 24),
          SizedBox(height: 28),
          Icon(Icons.phone_outlined, color: Color(0xFF78716C), size: 24),
          SizedBox(height: 28),
          Icon(Icons.people_outline_rounded,
              color: Color(0xFF78716C), size: 24),
          SizedBox(height: 28),
          Icon(Icons.tune_rounded, color: Color(0xFF78716C), size: 24),
          SizedBox(height: 24),
        ],
      );
}
