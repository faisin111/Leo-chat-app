import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/network/result.dart';
import '../../../../core/network/stomp_service.dart';
import '../../../media/domain/repositories/media_repository.dart';
import '../../data/models/message_dto.dart';
import '../../domain/entities/conversation.dart';
import '../../domain/entities/message.dart';
import '../../domain/repositories/chat_repository.dart';
import 'chat_state.dart';

import '../../../ai/domain/repositories/ai_repository.dart';

final chatNotifierProvider =
    StateNotifierProvider<ChatNotifier, ChatState>((ref) {
  final repository = ref.watch(chatRepositoryProvider);
  final stompService = ref.watch(stompServiceProvider);
  final storage = ref.watch(secureStorageProvider);
  final aiRepo = ref.watch(aiRepositoryProvider);

  return ChatNotifier(
    chatRepository: repository,
    stompService: stompService,
    secureStorage: storage,
    mediaRepository: ref.watch(mediaRepositoryProvider),
    aiRepository: aiRepo,
  );
});

class ChatNotifier extends StateNotifier<ChatState> {
  ChatNotifier({
    required ChatRepository chatRepository,
    required StompService stompService,
    required FlutterSecureStorage secureStorage,
    required MediaRepository mediaRepository,
    AiRepository? aiRepository,
  })  : _chatRepository = chatRepository,
        _stompService = stompService,
        _secureStorage = secureStorage,
        _mediaRepository = mediaRepository,
        _aiRepository = aiRepository,
        super(const ChatState(isLoadingConversations: true)) {
    _init();
  }

  final ChatRepository _chatRepository;
  final StompService _stompService;
  final FlutterSecureStorage _secureStorage;
  final MediaRepository _mediaRepository;
  final AiRepository? _aiRepository;

  StreamSubscription<MessageDto>? _msgSub;
  StreamSubscription<MessageDto>? _ackSub;
  StreamSubscription<Map<String, dynamic>>? _typingSub;
  Timer? _typingTimer;

  String? _currentUserId;
  String? _currentUsername;
  String? _currentDisplayName;
  bool _isLocalUserTyping = false;
  DateTime? _lastLocalTypingAt;

  Future<void> _init() async {
    try {
      _currentUserId = await _secureStorage.read(key: 'auth_user_id');
      _currentUsername = await _secureStorage.read(key: 'auth_username');
      _currentDisplayName = await _secureStorage.read(key: 'auth_display_name');
    } catch (_) {}

    // 1. Connect real-time WebSocket if token is present
    try {
      final token = await _secureStorage.read(key: 'accessToken');
      if (token != null && token.isNotEmpty) {
        _stompService.connect(token);
      }
    } catch (_) {}

    _stompService.connectionStatus.addListener(_onConnectionStatusChanged);
    state = state.copyWith(
      realtimeStatus: _stompService.connectionStatus.value,
    );

    // 2. Listen to incoming real-time messages
    _msgSub = _stompService.messageStream.listen(_onRealtimeMessageReceived);

    // 3. Listen to send acks
    _ackSub = _stompService.ackStream.listen(_onRealtimeAckReceived);

    // 4. Listen to typing indicators
    _typingSub = _stompService.typingStream.listen(_onRealtimeTypingReceived);

    // 5. Load conversations & initial data
    await loadConversations();
    await loadUnreadCount();
  }

  void _onConnectionStatusChanged() {
    state = state.copyWith(
      realtimeStatus: _stompService.connectionStatus.value,
    );
  }

  void _onRealtimeMessageReceived(MessageDto dto) async {
    final currentUserId = await _secureStorage.read(key: 'auth_user_id') ?? '';
    final currentUsername =
        await _secureStorage.read(key: 'auth_username') ?? '';

    final incomingMsg = dto.toDomain(
      currentUserId: currentUserId,
      currentUsername: currentUsername,
    );

    // If message belongs to currently active conversation
    if (state.selectedConversationId == incomingMsg.conversationId) {
      final updatedList = List<Message>.from(state.messages);

      // Check if optimistic message or existing message needs replacement
      final existingIndex = updatedList.indexWhere((m) =>
          (m.clientMessageId.isNotEmpty &&
              m.clientMessageId == incomingMsg.clientMessageId) ||
          m.id == incomingMsg.id);

      if (existingIndex >= 0) {
        updatedList[existingIndex] = incomingMsg;
      } else {
        updatedList.add(incomingMsg);
      }

      // Keep sorted by seq ascending
      updatedList.sort((a, b) => a.seq.compareTo(b.seq));
      state = state.copyWith(messages: updatedList);

      // Persist to local message cache
      _chatRepository.saveCachedMessages(
        incomingMsg.conversationId,
        updatedList,
      );

      // Mark message read
      markRead(incomingMsg.conversationId, incomingMsg.seq);
    }

    // Update conversation list preview
    _updateConversationLastMessage(
      conversationId: incomingMsg.conversationId,
      snippet: incomingMsg.content ?? 'Sent an attachment',
      lastSeq: incomingMsg.seq,
      time: incomingMsg.createdAt ?? DateTime.now(),
    );
  }

  void _onRealtimeAckReceived(MessageDto dto) {
    if (state.selectedConversationId == dto.conversationId) {
      final updatedList = state.messages.map((m) {
        if (m.clientMessageId == dto.clientMessageId) {
          return m.copyWith(
            id: dto.id,
            seq: dto.seq,
            sendStatus: MessageSendStatus.sent,
          );
        }
        return m;
      }).toList();
      state = state.copyWith(messages: updatedList);
    }
  }

  void _onRealtimeTypingReceived(Map<String, dynamic> data) async {
    final conversationId = data['conversationId']?.toString();
    final typingUserId = data['userId']?.toString() ??
        data['senderId']?.toString() ??
        data['fromId']?.toString();
    final typingUsername = data['username']?.toString() ??
        data['senderUsername']?.toString() ??
        data['from']?.toString() ??
        data['user']?.toString();
    final rawDisplayName = data['displayName']?.toString();

    final myId =
        _currentUserId ?? await _secureStorage.read(key: 'auth_user_id');
    final myUsername =
        _currentUsername ?? await _secureStorage.read(key: 'auth_username');
    final myDisplayName = _currentDisplayName ??
        await _secureStorage.read(key: 'auth_display_name');

    // Never show typing indicator for "me" — only show friend or others
    final isMe = (myId != null && myId.isNotEmpty && typingUserId == myId) ||
        (myUsername != null &&
            myUsername.isNotEmpty &&
            typingUsername != null &&
            typingUsername.toLowerCase() == myUsername.toLowerCase()) ||
        (myDisplayName != null &&
            myDisplayName.isNotEmpty &&
            rawDisplayName != null &&
            rawDisplayName.toLowerCase() == myDisplayName.toLowerCase()) ||
        (rawDisplayName != null && rawDisplayName.toLowerCase() == 'you');

    // If payload has no sender info, but the local user is currently typing, ignore the echo
    final hasSenderIdentity =
        (typingUserId != null && typingUserId.isNotEmpty) ||
            (typingUsername != null && typingUsername.isNotEmpty) ||
            (rawDisplayName != null && rawDisplayName.isNotEmpty);

    final isEchoOfLocalTyping = !hasSenderIdentity &&
        _isLocalUserTyping &&
        _lastLocalTypingAt != null &&
        DateTime.now().difference(_lastLocalTypingAt!).inSeconds < 3;

    if (isMe || isEchoOfLocalTyping) {
      return;
    }

    final displayName = rawDisplayName ??
        typingUsername ??
        state.selectedConversation?.effectiveTitle ??
        'Someone';
    final isTyping = data['isTyping'] == true;

    if (state.selectedConversationId == conversationId) {
      _typingTimer?.cancel();
      if (isTyping) {
        state = state.copyWith(
          typingStatusText: '$displayName is typing...',
        );
        _typingTimer = Timer(const Duration(seconds: 4), () {
          if (mounted) {
            state = state.copyWith(clearTypingStatusText: true);
          }
        });
      } else {
        state = state.copyWith(clearTypingStatusText: true);
      }
    }
  }

  void _updateConversationLastMessage({
    required String conversationId,
    required String snippet,
    required int lastSeq,
    required DateTime time,
  }) {
    final updatedConversations = state.conversations.map((c) {
      if (c.id == conversationId) {
        return c.copyWith(
          lastMessageSnippet: snippet,
          lastSeq: lastSeq,
          lastMessageAt: time,
        );
      }
      return c;
    }).toList();
    state = state.copyWith(conversations: updatedConversations);
    _chatRepository.saveCachedConversations(updatedConversations);
  }

  Future<void>? _loadConversationsFuture;

  Future<void> loadConversations({String? query}) {
    if (query == null && _loadConversationsFuture != null) {
      return _loadConversationsFuture!;
    }
    final future = _loadConversationsInternal(query: query);
    if (query == null) {
      _loadConversationsFuture = future;
      future.whenComplete(() {
        _loadConversationsFuture = null;
      });
    }
    return future;
  }

  Future<void> _loadConversationsInternal({String? query}) async {
    final isSearch = query != null && query.trim().isNotEmpty;

    // 1. Cache-first rendering: show cached conversations instantly
    if (!isSearch) {
      final cached = await _chatRepository.getCachedConversations();
      if (cached.isNotEmpty && mounted) {
        state = state.copyWith(
          conversations: cached,
          isLoadingConversations: false,
        );

        // Auto-select first conversation if none selected yet
        if (state.selectedConversation == null) {
          await selectConversation(cached.first);
        }
      } else if (state.conversations.isEmpty) {
        state = state.copyWith(isLoadingConversations: true);
      }
    } else if (state.conversations.isEmpty) {
      state = state.copyWith(isLoadingConversations: true);
    }

    // 2. Fetch fresh conversations from API in background/continuation
    final result = await _chatRepository.getConversations(query: query);

    if (!mounted) return;

    await result.fold(
      (failure) async {
        state = state.copyWith(
          isLoadingConversations: false,
          errorMessage: state.conversations.isEmpty ? failure.message : null,
        );
      },
      (items) async {
        state = state.copyWith(
          conversations: items,
          isLoadingConversations: false,
        );

        // Update conversation cache when not searching
        if (!isSearch) {
          await _chatRepository.saveCachedConversations(items);
        }

        // Auto-select first conversation if none selected yet, or update selected conversation reference
        if (state.selectedConversation == null && items.isNotEmpty) {
          await selectConversation(items.first);
        } else if (state.selectedConversation != null) {
          final updated = items
              .where((c) => c.id == state.selectedConversation!.id)
              .firstOrNull;
          if (updated != null) {
            state = state.copyWith(selectedConversation: updated);
          }
        }
      },
    );
  }

  Future<void> search(String query) async {
    state = state.copyWith(searchQuery: query);
    if (query.trim().isEmpty) {
      await loadConversations();
    } else {
      await loadConversations(query: query.trim());
    }
  }

  void setFilter(int index) {
    state = state.copyWith(filterIndex: index);
  }

  Future<void> selectConversation(Conversation conversation) async {
    // If this conversation is already active and contains messages, avoid resetting state
    if (state.selectedConversationId == conversation.id &&
        state.messages.isNotEmpty) {
      return;
    }

    // Unsubscribe from old conversation if different
    if (state.selectedConversationId != null &&
        state.selectedConversationId != conversation.id) {
      _stompService.unsubscribeFromConversation(state.selectedConversationId!);
    }

    // Check cached messages for instant rendering
    final cached = await _chatRepository.getCachedMessages(conversation.id);
    final hasCache = cached.isNotEmpty;

    state = state.copyWith(
      selectedConversationId: conversation.id,
      selectedConversation: conversation,
      isLoadingMessages: !hasCache,
      messages: cached,
      clearTypingStatusText: true,
    );

    // Subscribe to new conversation
    _stompService.subscribeToConversation(conversation.id);

    // Load messages from remote API
    await loadMessages(conversation.id, preserveCurrentOnError: hasCache);
  }

  Future<void> loadMessages(
    String conversationId, {
    bool preserveCurrentOnError = false,
  }) async {
    final result =
        await _chatRepository.getMessages(conversationId: conversationId);

    if (!mounted) return;

    result.fold(
      (failure) {
        state = state.copyWith(
          messages: preserveCurrentOnError ? state.messages : [],
          isLoadingMessages: false,
          errorMessage: failure.message,
        );
      },
      (messages) {
        // Retain any pending or active optimistic messages in flight
        final pending = state.messages.where((m) =>
            m.conversationId == conversationId &&
            (m.sendStatus == MessageSendStatus.sending ||
                m.sendStatus == MessageSendStatus.failed ||
                (m.clientMessageId.isNotEmpty &&
                    !messages.any(
                        (rm) => rm.clientMessageId == m.clientMessageId))));
        final merged = List<Message>.from(messages)..addAll(pending);
        merged.sort((a, b) => a.seq.compareTo(b.seq));

        state = state.copyWith(
          messages: merged,
          isLoadingMessages: false,
        );
        _chatRepository.saveCachedMessages(conversationId, merged);
      },
    );
  }

  Future<void> sendAiPrompt(String conversationId, String prompt) async {
    final cleanPrompt = prompt.trim();
    if (cleanPrompt.isEmpty) return;

    final clientMessageId = 'ai-client-${DateTime.now().millisecondsSinceEpoch}';

    final optimisticMsg = Message(
      id: clientMessageId,
      conversationId: conversationId,
      clientMessageId: clientMessageId,
      content: cleanPrompt,
      type: 'TEXT',
      createdAt: DateTime.now(),
      isOwn: true,
      isRead: false,
      seq: (state.messages.isNotEmpty ? state.messages.last.seq : 0) + 1,
      sendStatus: MessageSendStatus.sending,
    );

    final updated = List<Message>.from(state.messages)..add(optimisticMsg);
    state = state.copyWith(messages: updated);

    final sentRealtime = _stompService.sendAiChat(
      conversationId: conversationId,
      prompt: cleanPrompt,
      clientMessageId: clientMessageId,
    );

    final aiRepo = _aiRepository;
    if (!sentRealtime && aiRepo != null) {
      final res = await aiRepo.chat(
        conversationId: conversationId,
        prompt: cleanPrompt,
        clientMessageId: clientMessageId,
      );
      res.fold(
        (failure) {
          final list = state.messages.map((m) {
            if (m.clientMessageId == clientMessageId) {
              return m.copyWith(sendStatus: MessageSendStatus.failed);
            }
            return m;
          }).toList();
          state = state.copyWith(messages: list, errorMessage: failure.message);
        },
        (aiMsg) {
          if (!state.messages.any((m) => m.id == aiMsg.id)) {
            final list = List<Message>.from(state.messages)..add(aiMsg);
            state = state.copyWith(messages: list);
          }
        },
      );
    }
  }

  Future<Result<void>> leaveConversation(String conversationId) async {
    final result = await _chatRepository.leaveConversation(conversationId);
    return result.fold(
      (failure) => Left(failure),
      (_) {
        final updated =
            state.conversations.where((c) => c.id != conversationId).toList();
        _stompService.unsubscribeFromConversation(conversationId);
        final isSelected = state.selectedConversationId == conversationId;
        state = state.copyWith(
          conversations: updated,
          selectedConversationId:
              isSelected ? null : state.selectedConversationId,
        );
        _chatRepository.saveCachedConversations(updated);
        return const Right(null);
      },
    );
  }

  Future<void> sendMessage(String text,
      {String type = 'TEXT', String? replyToId}) async {
    final conv = state.selectedConversation;
    if (conv == null || text.trim().isEmpty) return;

    final trimmed = text.trim();
    if (conv.isAi || trimmed.toLowerCase().startsWith('/ai')) {
      final prompt = conv.isAi
          ? trimmed
          : trimmed
              .replaceFirst(RegExp(r'^/ai\s*', caseSensitive: false), '')
              .trim();
      if (prompt.isNotEmpty) {
        await sendAiPrompt(conv.id, prompt);
        return;
      }
    }

    final clientMessageId = 'client-${DateTime.now().millisecondsSinceEpoch}';

    // 1. Optimistic insert
    final optimisticMsg = Message(
      id: clientMessageId,
      conversationId: conv.id,
      clientMessageId: clientMessageId,
      content: text.trim(),
      type: type,
      replyToId: replyToId,
      createdAt: DateTime.now(),
      isOwn: true,
      isRead: false,
      seq:
          (state.messages.isNotEmpty ? state.messages.last.seq : conv.lastSeq) +
              1,
      sendStatus: MessageSendStatus.sending,
    );

    final updated = List<Message>.from(state.messages)..add(optimisticMsg);
    state = state.copyWith(messages: updated);

    // 2. Try sending via WebSocket first
    _stompService.sendMessage(
      conversationId: conv.id,
      clientMessageId: clientMessageId,
      content: text.trim(),
      type: type,
      replyToId: replyToId,
    );

    // REST is the reliable persistence path. The same clientMessageId makes
    // this safe when the WebSocket send was accepted before reconnecting.
    final result = await _chatRepository.sendMessage(
      conversationId: conv.id,
      clientMessageId: clientMessageId,
      content: text.trim(),
      type: type,
      replyToId: replyToId,
    );

    result.fold(
      (failure) {
        final list = state.messages.map((m) {
          if (m.clientMessageId == clientMessageId) {
            return m.copyWith(sendStatus: MessageSendStatus.failed);
          }
          return m;
        }).toList();
        state = state.copyWith(messages: list, errorMessage: failure.message);
      },
      (persistedMsg) {
        final list = state.messages.map((m) {
          if (m.clientMessageId == clientMessageId) {
            return persistedMsg.copyWith(
              sendStatus: MessageSendStatus.sent,
              isOwn: true,
            );
          }
          return m;
        }).toList();
        state = state.copyWith(messages: list, errorMessage: null);
        _chatRepository.saveCachedMessages(conv.id, list);
      },
    );
  }

  Future<void> sendVoice(String filePath) async {
    final conv = state.selectedConversation;
    if (conv == null || filePath.trim().isEmpty) return;

    final clientMessageId = 'client-${DateTime.now().millisecondsSinceEpoch}';

    // 1. Optimistic insert
    final optimisticMsg = Message(
      id: clientMessageId,
      conversationId: conv.id,
      clientMessageId: clientMessageId,
      content: filePath,
      type: 'VOICE',
      createdAt: DateTime.now(),
      isOwn: true,
      isRead: false,
      seq:
          (state.messages.isNotEmpty ? state.messages.last.seq : conv.lastSeq) +
              1,
      sendStatus: MessageSendStatus.sending,
    );

    final updated = List<Message>.from(state.messages)..add(optimisticMsg);
    state = state.copyWith(messages: updated, clearErrorMessage: true);

    // 2. Upload file
    final upload = await _mediaRepository.uploadVoice(filePath: filePath);
    await upload.fold(
      (failure) async {
        final list = state.messages.map((m) {
          if (m.clientMessageId == clientMessageId) {
            return m.copyWith(sendStatus: MessageSendStatus.failed);
          }
          return m;
        }).toList();
        state = state.copyWith(messages: list, errorMessage: failure.message);
      },
      (url) async {
        // 3. Send over WS or REST
        _stompService.sendMessage(
          conversationId: conv.id,
          clientMessageId: clientMessageId,
          content: url,
          type: 'VOICE',
        );

        final result = await _chatRepository.sendMessage(
          conversationId: conv.id,
          clientMessageId: clientMessageId,
          content: url,
          type: 'VOICE',
        );

        result.fold(
          (failure) {
            final list = state.messages.map((m) {
              if (m.clientMessageId == clientMessageId) {
                return m.copyWith(sendStatus: MessageSendStatus.failed);
              }
              return m;
            }).toList();
            state = state.copyWith(
              messages: list,
              errorMessage: failure.message,
            );
          },
          (persisted) {
            final list = state.messages.map((m) {
              if (m.clientMessageId == clientMessageId) {
                return persisted.copyWith(
                  isOwn: true,
                  sendStatus: MessageSendStatus.sent,
                );
              }
              return m;
            }).toList();
            state = state.copyWith(messages: list, errorMessage: null);
            _chatRepository.saveCachedMessages(conv.id, list);
          },
        );
      },
    );
  }

  Future<Result<Conversation>> startDirectMessage(String targetUserId) async {
    final result = await _chatRepository.startDirectMessage(
      targetUserId: targetUserId,
    );
    await result.fold(
      (failure) async {},
      (conversation) async {
        await loadConversations();
        await selectConversation(conversation);
      },
    );
    return result;
  }

  Future<Result<Conversation>> createGroup({
    required String title,
    required List<String> memberIds,
  }) async {
    final result = await _chatRepository.createGroup(
      title: title,
      memberIds: memberIds,
    );
    await result.fold(
      (failure) async {},
      (conversation) async {
        await loadConversations();
        await selectConversation(conversation);
      },
    );
    return result;
  }

  Future<void> editMessage(String messageId, String content) async {
    final result = await _chatRepository.editMessage(
      messageId: messageId,
      content: content,
    );
    result.fold(
      (_) {},
      (updated) {
        final messages = state.messages.map((m) {
          if (m.id == messageId) {
            return updated.copyWith(isOwn: m.isOwn);
          }
          return m;
        }).toList();
        state = state.copyWith(messages: messages);
        if (state.selectedConversationId != null) {
          _chatRepository.saveCachedMessages(
            state.selectedConversationId!,
            messages,
          );
        }
      },
    );
  }

  Future<void> deleteMessage(String messageId) async {
    final result = await _chatRepository.deleteMessage(messageId);
    result.fold(
      (_) {},
      (_) {
        final messages = state.messages.map((m) {
          if (m.id == messageId) {
            return m.copyWith(deleted: true, content: 'Message deleted');
          }
          return m;
        }).toList();
        state = state.copyWith(messages: messages);
        if (state.selectedConversationId != null) {
          _chatRepository.saveCachedMessages(
            state.selectedConversationId!,
            messages,
          );
        }
      },
    );
  }

  Future<void> toggleReaction(String messageId, String emoji) async {
    final msg = state.messages.firstWhere(
      (m) => m.id == messageId,
      orElse: () => Message(
        id: messageId,
        conversationId: '',
        senderId: '',
        senderUsername: '',
        seq: 0,
        type: 'TEXT',
        content: '',
        createdAt: DateTime.now(),
      ),
    );

    final hasReacted = msg.hasUserReacted(emoji);
    if (hasReacted) {
      await _chatRepository.removeReaction(messageId: messageId, emoji: emoji);
    } else {
      await _chatRepository.addReaction(messageId: messageId, emoji: emoji);
    }
  }

  Future<void> pinConversation(String conversationId) async {
    await _chatRepository.pinConversation(conversationId);
    await loadConversations();
  }

  Future<void> unpinConversation(String conversationId) async {
    await _chatRepository.unpinConversation(conversationId);
    await loadConversations();
  }

  Future<void> archiveConversation(String conversationId) async {
    await _chatRepository.archiveConversation(conversationId);
    await loadConversations();
  }

  Future<void> unarchiveConversation(String conversationId) async {
    await _chatRepository.unarchiveConversation(conversationId);
    await loadConversations();
  }

  void reset() {
    _typingTimer?.cancel();
    if (state.selectedConversationId != null) {
      _stompService.unsubscribeFromConversation(state.selectedConversationId!);
    }
    _stompService.disconnect();
    state = const ChatState();
  }

  Future<void> markRead(String conversationId, int upToSeq) async {
    _stompService.sendRead(
      conversationId: conversationId,
      upToSeq: upToSeq,
    );
    await _chatRepository.markConversationRead(
      conversationId: conversationId,
      upToSeq: upToSeq,
    );
  }

  void sendTyping(bool isTyping) {
    _isLocalUserTyping = isTyping;
    if (isTyping) {
      _lastLocalTypingAt = DateTime.now();
    }
    if (state.selectedConversationId != null) {
      _stompService.sendTyping(
        conversationId: state.selectedConversationId!,
        isTyping: isTyping,
      );
    }
  }

  Future<void> loadUnreadCount() async {
    final result = await _chatRepository.getUnreadCount();
    result.fold(
      (_) {},
      (count) => state = state.copyWith(totalUnreadCount: count),
    );
  }

  @override
  void dispose() {
    _typingTimer?.cancel();
    _msgSub?.cancel();
    _ackSub?.cancel();
    _typingSub?.cancel();
    _stompService.connectionStatus.removeListener(_onConnectionStatusChanged);
    super.dispose();
  }
}
