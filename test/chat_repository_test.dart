import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';

import 'package:leochat/core/cache/cache_store.dart';
import 'package:leochat/features/chat/data/datasources/chat_remote_data_source.dart';
import 'package:leochat/features/chat/data/models/conversation_dto.dart';
import 'package:leochat/features/chat/data/models/message_dto.dart';
import 'package:leochat/features/chat/data/models/send_message_request_dto.dart';
import 'package:leochat/features/chat/data/repositories/chat_repository_impl.dart';

class _FakeChatRemoteDataSource implements ChatRemoteDataSource {
  final List<ConversationDto> conversations = [
    const ConversationDto(
      id: 'c-1',
      title: 'Design Team',
      type: 'GROUP',
      unreadCount: 3,
      lastSeq: 10,
    ),
    const ConversationDto(
      id: 'c-2',
      title: 'Alice',
      type: 'DIRECT',
      unreadCount: 0,
      lastSeq: 5,
    ),
  ];

  final List<MessageDto> messages = [
    const MessageDto(
      id: 'm-2',
      conversationId: 'c-1',
      content: 'Second message',
      seq: 2,
    ),
    const MessageDto(
      id: 'm-1',
      conversationId: 'c-1',
      content: 'First message',
      seq: 1,
    ),
  ];

  @override
  Future<ConversationPageDto> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async {
    if (query != null && query.isNotEmpty) {
      final filtered = conversations
          .where((c) => c.title.toLowerCase().contains(query.toLowerCase()))
          .toList();
      return ConversationPageDto(items: filtered);
    }
    return ConversationPageDto(items: conversations);
  }

  @override
  Future<ConversationDto> getConversation(String conversationId) async {
    return conversations.firstWhere((c) => c.id == conversationId);
  }

  @override
  Future<MessagePageDto> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
    String? query,
  }) async {
    return MessagePageDto(items: messages);
  }

  @override
  Future<MessageDto> sendMessage(SendMessageRequestDto request) async {
    return MessageDto(
      id: 'm-new',
      conversationId: request.conversationId,
      clientMessageId: request.clientMessageId,
      content: request.content,
      type: request.type,
      seq: 3,
    );
  }

  @override
  Future<void> markConversationRead({
    required String conversationId,
    required int upToSeq,
  }) async {}

  @override
  Future<int> getUnreadCount() async => 3;
}

class _MemoryCacheStore implements CacheStore {
  final Map<String, dynamic> _storage = {};

  @override
  Future<void> writeJson(String key, Object value) async {
    _storage[key] = value;
  }

  @override
  Future<Map<String, dynamic>?> readJson(String key) async {
    return _storage[key] as Map<String, dynamic>?;
  }

  @override
  Future<void> delete(String key) async {
    _storage.remove(key);
  }

  @override
  Future<void> clearUserScope(String userId) async {
    _storage.clear();
  }

  @override
  Future<void> clearAll() async {
    _storage.clear();
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late _FakeChatRemoteDataSource remoteDataSource;
  late _MemoryCacheStore cacheStore;
  late ChatRepositoryImpl repository;

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({
      'auth_user_id': 'user-123',
      'auth_username': 'testuser',
    });
    remoteDataSource = _FakeChatRemoteDataSource();
    cacheStore = _MemoryCacheStore();
    repository = ChatRepositoryImpl(
      remoteDataSource: remoteDataSource,
      secureStorage: const FlutterSecureStorage(),
      cacheStore: cacheStore,
    );
  });

  test('getConversations returns domain list and caches first page', () async {
    final result = await repository.getConversations();

    expect(result.isRight(), isTrue);
    final list = result.getOrElse((_) => []);
    expect(list.length, 2);
    expect(list[0].id, 'c-1');
    expect(list[0].title, 'Design Team');
    expect(list[0].isGroup, isTrue);
    expect(list[1].title, 'Alice');

    // Verify cache was populated
    final cached = await cacheStore.readJson('cache/user-123/conversations');
    expect(cached, isNotNull);
  });

  test('getConversations with query filters by search keyword', () async {
    final result = await repository.getConversations(query: 'Alice');

    expect(result.isRight(), isTrue);
    final list = result.getOrElse((_) => []);
    expect(list.length, 1);
    expect(list[0].title, 'Alice');
  });

  test('getMessages sorts by sequence ascending', () async {
    final result = await repository.getMessages(conversationId: 'c-1');

    expect(result.isRight(), isTrue);
    final messages = result.getOrElse((_) => []);
    expect(messages.length, 2);
    expect(messages[0].seq, 1);
    expect(messages[0].content, 'First message');
    expect(messages[1].seq, 2);
    expect(messages[1].content, 'Second message');
  });

  test('sendMessage returns persisted message model', () async {
    final result = await repository.sendMessage(
      conversationId: 'c-1',
      clientMessageId: 'client-1',
      content: 'Hello world',
    );

    expect(result.isRight(), isTrue);
    final msg = result.getOrElse((_) => throw Exception());
    expect(msg.id, 'm-new');
    expect(msg.content, 'Hello world');
    expect(msg.seq, 3);
  });

  test('getUnreadCount returns total count', () async {
    final result = await repository.getUnreadCount();
    expect(result, const Right(3));
  });
}
