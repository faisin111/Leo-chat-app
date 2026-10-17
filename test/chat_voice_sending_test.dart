import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/errors/app_failure.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/core/network/stomp_service.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/domain/repositories/chat_repository.dart';
import 'package:leochat/features/chat/presentation/providers/chat_notifier.dart';
import 'package:leochat/features/media/domain/repositories/media_repository.dart';

class _FakeChatRepository implements ChatRepository {
  List<Message> sentMessages = [];

  @override
  Future<Result<List<Conversation>>> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async =>
      const Right([
        Conversation(
          id: 'c-1',
          title: 'Alice',
          type: 'DIRECT',
          lastSeq: 10,
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
  }) async {
    final msg = Message(
      id: 'server-$clientMessageId',
      conversationId: conversationId,
      clientMessageId: clientMessageId,
      content: content,
      type: type,
      seq: 11,
      isOwn: true,
      sendStatus: MessageSendStatus.sent,
    );
    sentMessages.add(msg);
    return Right(msg);
  }

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
      const Right(Conversation(
        id: 'c-1',
        title: 'Alice',
        type: 'DIRECT',
        lastSeq: 10,
      ));
}

class _FakeMediaRepository implements MediaRepository {
  _FakeMediaRepository({this.shouldFail = false});

  final bool shouldFail;
  String? uploadedFilePath;

  @override
  Future<Result<String>> uploadVoice({required String filePath}) async {
    uploadedFilePath = filePath;
    if (shouldFail) {
      return const Left(NetworkFailure('Upload failed'));
    }
    return const Right('https://storage.leochat.app/voice/123.m4a');
  }
}

void main() {
  setUp(() {
    FlutterSecureStorage.setMockInitialValues({});
  });

  test('sendVoice performs optimistic insert, uploads file, and dispatches voice message',
      () async {
    final chatRepo = _FakeChatRepository();
    final mediaRepo = _FakeMediaRepository();
    final stompService = StompService(wsUrl: 'wss://test.leochat.app/ws');

    final notifier = ChatNotifier(
      chatRepository: chatRepo,
      stompService: stompService,
      secureStorage: const FlutterSecureStorage(),
      mediaRepository: mediaRepo,
    );

    await notifier.loadConversations();
    expect(notifier.state.selectedConversation, isNotNull);

    // Call sendVoice
    await notifier.sendVoice('/tmp/recorded_voice.m4a');

    // Verify upload occurred
    expect(mediaRepo.uploadedFilePath, '/tmp/recorded_voice.m4a');

    // Verify message is in state with type VOICE and updated remote URL
    expect(notifier.state.messages.length, 1);
    final voiceMsg = notifier.state.messages.first;
    expect(voiceMsg.type, 'VOICE');
    expect(voiceMsg.isOwn, isTrue);
    expect(voiceMsg.content, 'https://storage.leochat.app/voice/123.m4a');
    expect(voiceMsg.sendStatus, MessageSendStatus.sent);
  });

  test('sendVoice marks message as failed when upload fails', () async {
    final chatRepo = _FakeChatRepository();
    final mediaRepo = _FakeMediaRepository(shouldFail: true);
    final stompService = StompService(wsUrl: 'wss://test.leochat.app/ws');

    final notifier = ChatNotifier(
      chatRepository: chatRepo,
      stompService: stompService,
      secureStorage: const FlutterSecureStorage(),
      mediaRepository: mediaRepo,
    );

    await notifier.loadConversations();
    await notifier.sendVoice('/tmp/failed_voice.m4a');

    expect(notifier.state.messages.length, 1);
    final voiceMsg = notifier.state.messages.first;
    expect(voiceMsg.type, 'VOICE');
    expect(voiceMsg.sendStatus, MessageSendStatus.failed);
    expect(notifier.state.errorMessage, 'Upload failed');
  });
}
