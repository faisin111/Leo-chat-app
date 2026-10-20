import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';
import 'package:stomp_dart_client/stomp_handler.dart';

import '../../features/chat/data/models/message_dto.dart';

enum RealtimeConnectionStatus {
  disconnected,
  connecting,
  connected,
  reconnecting,
}

class StompService {
  StompService({required String wsUrl}) : _wsUrl = wsUrl;

  final String _wsUrl;
  StompClient? _client;
  String? _currentToken;
  bool _disposed = false;

  final _connectionStatusNotifier = ValueNotifier<RealtimeConnectionStatus>(
      RealtimeConnectionStatus.disconnected);
  ValueListenable<RealtimeConnectionStatus> get connectionStatus =>
      _connectionStatusNotifier;

  void _updateConnectionStatus(RealtimeConnectionStatus status) {
    if (!_disposed) {
      _connectionStatusNotifier.value = status;
    }
  }

  final _messageController = StreamController<MessageDto>.broadcast();
  Stream<MessageDto> get messageStream => _messageController.stream;

  final _typingController = StreamController<Map<String, dynamic>>.broadcast();
  Stream<Map<String, dynamic>> get typingStream => _typingController.stream;

  final _ackController = StreamController<MessageDto>.broadcast();
  Stream<MessageDto> get ackStream => _ackController.stream;

  final _errorController = StreamController<Map<String, dynamic>>.broadcast();
  Stream<Map<String, dynamic>> get errorStream => _errorController.stream;

  // Active conversations to subscribe / resubscribe on reconnect
  final Set<String> _activeConversationSubscriptions = <String>{};
  final Map<String, List<StompUnsubscribe>> _unsubscribers = {};

  void connect(String token) {
    if (_client != null && _currentToken == token && _client!.connected) {
      return;
    }
    _currentToken = token;
    _updateConnectionStatus(RealtimeConnectionStatus.connecting);

    _client?.deactivate();

    _client = StompClient(
      config: StompConfig(
        url: _wsUrl,
        useSockJS: false,
        reconnectDelay: const Duration(seconds: 4),
        heartbeatOutgoing: const Duration(seconds: 10),
        heartbeatIncoming: const Duration(seconds: 10),
        stompConnectHeaders: {
          'Authorization': 'Bearer $token',
        },
        webSocketConnectHeaders: {
          'Authorization': 'Bearer $token',
        },
        onConnect: _onStompConnect,
        onDisconnect: (frame) {
          _updateConnectionStatus(RealtimeConnectionStatus.disconnected);
        },
        onWebSocketDone: () {
          _updateConnectionStatus(RealtimeConnectionStatus.reconnecting);
        },
        onWebSocketError: (error) {
          _updateConnectionStatus(RealtimeConnectionStatus.reconnecting);
        },
        onStompError: (frame) {
          try {
            if (frame.body != null) {
              final body = jsonDecode(frame.body!) as Map<String, dynamic>;
              _errorController.add(body);
            }
          } catch (_) {}
        },
      ),
    );

    _client!.activate();
  }

  void _onStompConnect(StompFrame frame) {
    _updateConnectionStatus(RealtimeConnectionStatus.connected);

    // Clear previous subscription tokens (they are dead after socket reconnect)
    _unsubscribers.clear();

    // 1. Subscribe to private acknowledgements
    _client?.subscribe(
      destination: '/user/queue/acks',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final json = jsonDecode(frame.body!) as Map<String, dynamic>;
            _ackController.add(MessageDto.fromJson(json));
          } catch (_) {}
        }
      },
    );

    // 2. Subscribe to private errors
    _client?.subscribe(
      destination: '/user/queue/errors',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final json = jsonDecode(frame.body!) as Map<String, dynamic>;
            _errorController.add(json);
          } catch (_) {}
        }
      },
    );

    // 3. Resubscribe to all active conversations
    for (final conversationId in _activeConversationSubscriptions) {
      _applyConversationSubscriptions(conversationId);
    }
  }

  void subscribeToConversation(String conversationId) {
    _activeConversationSubscriptions.add(conversationId);
    if (_client != null && _client!.connected) {
      _applyConversationSubscriptions(conversationId);
    }
  }

  void _applyConversationSubscriptions(String conversationId) {
    if (_client == null || !_client!.connected) return;

    // Avoid duplicate subscriptions
    if (_unsubscribers.containsKey(conversationId)) return;

    final unsubs = <StompUnsubscribe>[];

    // Messages topic
    final unsubMsg = _client!.subscribe(
      destination: '/topic/conversations.$conversationId',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final json = jsonDecode(frame.body!) as Map<String, dynamic>;
            _messageController.add(MessageDto.fromJson(json));
          } catch (_) {}
        }
      },
    );
    unsubs.add(unsubMsg);

    // Typing topic
    final unsubTyping = _client!.subscribe(
      destination: '/topic/conversations.$conversationId.typing',
      callback: (frame) {
        if (frame.body != null) {
          try {
            final json = jsonDecode(frame.body!) as Map<String, dynamic>;
            _typingController.add(json);
          } catch (_) {}
        }
      },
    );
    unsubs.add(unsubTyping);

    _unsubscribers[conversationId] = unsubs;
  }

  void unsubscribeFromConversation(String conversationId) {
    _activeConversationSubscriptions.remove(conversationId);
    final unsubs = _unsubscribers.remove(conversationId);
    if (unsubs != null) {
      for (final unsub in unsubs) {
        try {
          unsub();
        } catch (_) {}
      }
    }
  }

  bool sendMessage({
    required String conversationId,
    required String clientMessageId,
    required String content,
    String type = 'TEXT',
    String? replyToId,
  }) {
    if (_client == null || !_client!.connected) return false;

    final payload = {
      'conversationId': conversationId,
      'clientMessageId': clientMessageId,
      'content': content,
      'type': type,
      if (replyToId != null) 'replyToId': replyToId,
    };

    _client!.send(
      destination: '/app/chat.send',
      body: jsonEncode(payload),
    );
    return true;
  }

  bool sendTyping({required String conversationId, required bool isTyping}) {
    if (_client == null || !_client!.connected) return false;

    final payload = {
      'conversationId': conversationId,
      'isTyping': isTyping,
    };

    _client!.send(
      destination: '/app/chat.typing',
      body: jsonEncode(payload),
    );
    return true;
  }

  bool sendRead({required String conversationId, required int upToSeq}) {
    if (_client == null || !_client!.connected) return false;

    final payload = {
      'conversationId': conversationId,
      'upToSeq': upToSeq,
    };

    _client!.send(
      destination: '/app/chat.read',
      body: jsonEncode(payload),
    );
    return true;
  }

  void disconnect() {
    _activeConversationSubscriptions.clear();
    _unsubscribers.clear();
    _client?.deactivate();
    _client = null;
    _currentToken = null;
    _updateConnectionStatus(RealtimeConnectionStatus.disconnected);
  }

  void dispose() {
    _disposed = true;
    disconnect();
    _messageController.close();
    _typingController.close();
    _ackController.close();
    _errorController.close();
    _connectionStatusNotifier.dispose();
  }
}
