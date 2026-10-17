import '../../../../core/network/stomp_service.dart';
import '../../domain/entities/conversation.dart';
import '../../domain/entities/message.dart';

class ChatState {
  const ChatState({
    this.isLoadingConversations = true,
    this.conversations = const [],
    this.filterIndex = 0,
    this.searchQuery = '',
    this.totalUnreadCount = 0,
    this.selectedConversationId,
    this.selectedConversation,
    this.isLoadingMessages = false,
    this.messages = const [],
    this.isTyping = false,
    this.typingStatusText,
    this.realtimeStatus = RealtimeConnectionStatus.disconnected,
    this.errorMessage,
  });

  final bool isLoadingConversations;
  final List<Conversation> conversations;
  final int filterIndex; // 0: All, 1: Unread, 2: Groups
  final String searchQuery;
  final int totalUnreadCount;
  final String? selectedConversationId;
  final Conversation? selectedConversation;
  final bool isLoadingMessages;
  final List<Message> messages;
  final bool isTyping;
  final String? typingStatusText;
  final RealtimeConnectionStatus realtimeStatus;
  final String? errorMessage;

  /// Filtered list according to tab pill and search input
  List<Conversation> get displayedConversations {
    var list = conversations;

    if (filterIndex == 1) {
      list = list.where((c) => c.unreadCount > 0).toList();
    } else if (filterIndex == 2) {
      list = list.where((c) => c.isGroup).toList();
    }

    if (searchQuery.trim().isNotEmpty) {
      final q = searchQuery.toLowerCase().trim();
      list = list.where((c) {
        final title = c.effectiveTitle.toLowerCase();
        final snippet = (c.lastMessageSnippet ?? '').toLowerCase();
        return title.contains(q) || snippet.contains(q);
      }).toList();
    }

    return list;
  }

  ChatState copyWith({
    bool? isLoadingConversations,
    List<Conversation>? conversations,
    int? filterIndex,
    String? searchQuery,
    int? totalUnreadCount,
    String? selectedConversationId,
    Conversation? selectedConversation,
    bool? isLoadingMessages,
    List<Message>? messages,
    bool? isTyping,
    String? typingStatusText,
    RealtimeConnectionStatus? realtimeStatus,
    String? errorMessage,
    bool clearSelectedConversation = false,
    bool clearTypingStatusText = false,
  }) {
    return ChatState(
      isLoadingConversations:
          isLoadingConversations ?? this.isLoadingConversations,
      conversations: conversations ?? this.conversations,
      filterIndex: filterIndex ?? this.filterIndex,
      searchQuery: searchQuery ?? this.searchQuery,
      totalUnreadCount: totalUnreadCount ?? this.totalUnreadCount,
      selectedConversationId: clearSelectedConversation
          ? null
          : (selectedConversationId ?? this.selectedConversationId),
      selectedConversation: clearSelectedConversation
          ? null
          : (selectedConversation ?? this.selectedConversation),
      isLoadingMessages: isLoadingMessages ?? this.isLoadingMessages,
      messages: messages ?? this.messages,
      isTyping: isTyping ?? this.isTyping,
      typingStatusText: clearTypingStatusText
          ? null
          : (typingStatusText ?? this.typingStatusText),
      realtimeStatus: realtimeStatus ?? this.realtimeStatus,
      errorMessage: errorMessage,
    );
  }
}
