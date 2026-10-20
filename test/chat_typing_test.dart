import 'dart:async';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/core/network/stomp_service.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/domain/repositories/chat_repository.dart';
import 'package:leochat/features/chat/presentation/providers/chat_notifier.dart';
import 'package:leochat/features/media/domain/repositories/media_repository.dart';

class _FakeChatRepo implements ChatRepository {
  @override
  Future<Result<List<Conversation>>> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async =>
      const Right([
        Conversation(
          id: 'conv-1',
          title: 'Alice',
          type: 'DIRECT',
          targetUsername: 'alice',
          targetDisplayName: 'Alice Morgan',
        ),
      ]);

  @override
  Future<Result<List<Message>>> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
  }) async =>
      const Right([]);

  @override
  Future<Result<Message>> sendMessage({
    required String conversationId,
    required String clientMessageId,
    required String content,
    String type = 'TEXT',
    String? replyToId,
  }) async =>
      Right(Message(
        id: 'm-1',
        conversationId: conversationId,
        content: content,
      ));

  @override
  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  }) async =>
      const Right(null);

  @override
  Future<Result<int>> getUnreadCount() async => const Right(0);

  @override
  Future<Result<Conversation>> getConversation({
    required String conversationId,
  }) async =>
      const Right(Conversation(id: 'conv-1', title: 'Alice'));

  @override
  Future<List<Conversation>> getCachedConversations() async => [];

  @override
  Future<void> saveCachedConversations(
      List<Conversation> conversations) async {}

  @override
  Future<List<Message>> getCachedMessages(String conversationId) async => [];

  @override
  Future<void> saveCachedMessages(
    String conversationId,
    List<Message> messages,
  ) async {}

  @override
  Future<void> clearCache({String? userId}) async {}
}

class _FakeMediaRepo implements MediaRepository {
  @override
  Future<Result<String>> uploadVoice({required String filePath}) async =>
      const Right('url');
}

class _TestStompService extends StompService {
  _TestStompService() : super(wsUrl: 'wss://test.leochat.app/ws');

  final _testTypingController =
      StreamController<Map<String, dynamic>>.broadcast();

  @override
  Stream<Map<String, dynamic>> get typingStream => _testTypingController.stream;

  void emitTyping(Map<String, dynamic> data) {
    _testTypingController.add(data);
  }

  @override
  void dispose() {
    _testTypingController.close();
    super.dispose();
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late _TestStompService stompService;
  late ChatNotifier notifier;

  setUp(() async {
    FlutterSecureStorage.setMockInitialValues({
      'auth_user_id': 'my-user-id',
      'auth_username': 'my_username',
      'auth_display_name': 'My Name',
      'accessToken': 'valid_token',
    });

    stompService = _TestStompService();
    notifier = ChatNotifier(
      chatRepository: _FakeChatRepo(),
      stompService: stompService,
      secureStorage: const FlutterSecureStorage(),
      mediaRepository: _FakeMediaRepo(),
    );

    await notifier.loadConversations();
  });

  tearDown(() {
    notifier.dispose();
    stompService.dispose();
  });

  group('Typing indicator filtering', () {
    test('Shows typing indicator when friend or other user types', () async {
      expect(notifier.state.selectedConversationId, 'conv-1');

      // Emit typing event from friend "Alice"
      stompService.emitTyping({
        'conversationId': 'conv-1',
        'userId': 'user-alice',
        'username': 'alice',
        'displayName': 'Alice',
        'isTyping': true,
      });

      // Allow microtask to process
      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(notifier.state.typingStatusText, 'Alice is typing...');
    });

    test(
        'DOES NOT show typing indicator when current user ("me") types by userId',
        () async {
      expect(notifier.state.selectedConversationId, 'conv-1');

      // Emit typing event containing my own userId
      stompService.emitTyping({
        'conversationId': 'conv-1',
        'userId': 'my-user-id',
        'username': 'my_username',
        'displayName': 'My Name',
        'isTyping': true,
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));

      // Must NOT show my own typing indicator
      expect(notifier.state.typingStatusText, isNull);
    });

    test(
        'DOES NOT show typing indicator when event matches current username or display name',
        () async {
      // Event matching username
      stompService.emitTyping({
        'conversationId': 'conv-1',
        'username': 'my_username',
        'isTyping': true,
      });
      await Future<void>.delayed(const Duration(milliseconds: 50));
      expect(notifier.state.typingStatusText, isNull);

      // Event matching displayName
      stompService.emitTyping({
        'conversationId': 'conv-1',
        'displayName': 'My Name',
        'isTyping': true,
      });
      await Future<void>.delayed(const Duration(milliseconds: 50));
      expect(notifier.state.typingStatusText, isNull);
    });

    test(
        'DOES NOT show typing indicator for local user echo without sender info',
        () async {
      // Current user types in composer
      notifier.sendTyping(true);

      // Echo event received with no sender identity
      stompService.emitTyping({
        'conversationId': 'conv-1',
        'isTyping': true,
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));
      expect(notifier.state.typingStatusText, isNull);
    });
  });
}
