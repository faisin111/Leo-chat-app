import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:leochat/core/cache/cache_store.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/core/network/stomp_service.dart';
import 'package:leochat/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:leochat/features/auth/data/models/auth_response_dto.dart';
import 'package:leochat/features/auth/data/models/login_request_dto.dart';
import 'package:leochat/features/auth/data/models/register_request_dto.dart';
import 'package:leochat/features/auth/data/models/user_dto.dart';
import 'package:leochat/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:leochat/features/chat/data/datasources/chat_remote_data_source.dart';
import 'package:leochat/features/chat/data/models/conversation_dto.dart';
import 'package:leochat/features/chat/data/models/message_dto.dart';
import 'package:leochat/features/chat/data/models/send_message_request_dto.dart';
import 'package:leochat/features/chat/data/repositories/chat_repository_impl.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/presentation/providers/chat_notifier.dart';
import 'package:leochat/features/media/domain/repositories/media_repository.dart';

class _InMemoryCacheStore implements CacheStore {
  final Map<String, dynamic> data = {};
  bool clearAllCalled = false;
  String? clearedUserId;

  @override
  Future<void> writeJson(String key, Object value) async {
    data[key] = value;
  }

  @override
  Future<Map<String, dynamic>?> readJson(String key) async {
    return data[key] as Map<String, dynamic>?;
  }

  @override
  Future<void> delete(String key) async {
    data.remove(key);
  }

  @override
  Future<void> clearUserScope(String userId) async {
    clearedUserId = userId;
    final prefix = 'cache/$userId/';
    data.removeWhere((k, _) => k.startsWith(prefix));
  }

  @override
  Future<void> clearAll() async {
    clearAllCalled = true;
    data.clear();
  }
}

class _FakeChatRemoteDataSource implements ChatRemoteDataSource {
  List<ConversationDto> conversationsToReturn = [];
  List<MessageDto> messagesToReturn = [];

  @override
  Future<ConversationPageDto> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async {
    return ConversationPageDto(items: conversationsToReturn);
  }

  @override
  Future<MessagePageDto> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
    String? query,
  }) async {
    return MessagePageDto(items: messagesToReturn);
  }

  @override
  Future<MessageDto> sendMessage(SendMessageRequestDto request) async {
    throw UnimplementedError();
  }

  @override
  Future<void> markConversationRead(
      {required String conversationId, required int upToSeq}) async {}

  @override
  Future<int> getUnreadCount() async => 0;

  @override
  Future<ConversationDto> getConversation(String conversationId) async {
    throw UnimplementedError();
  }
}

class _FakeAuthRemoteDataSource implements AuthRemoteDataSource {
  @override
  Future<AuthResponseDto> login(LoginRequestDto request) async {
    throw UnimplementedError();
  }

  @override
  Future<AuthResponseDto> register(RegisterRequestDto request) async {
    throw UnimplementedError();
  }

  @override
  Future<void> logout() async {}

  @override
  Future<UserDto> getMe() async {
    throw UnimplementedError();
  }
}

class _FakeMediaRepo implements MediaRepository {
  @override
  Future<Result<String>> uploadVoice({required String filePath}) async {
    return const Right('https://storage.leochat.app/voice/test.m4a');
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Chat cache-first and logout clearing', () {
    late _InMemoryCacheStore cacheStore;
    late _FakeChatRemoteDataSource remoteDataSource;
    late ChatRepositoryImpl repository;

    setUp(() {
      SharedPreferences.setMockInitialValues({});
      FlutterSecureStorage.setMockInitialValues({
        'auth_user_id': 'user_99',
        'auth_username': 'leotester',
        'accessToken': 'valid_token',
      });
      cacheStore = _InMemoryCacheStore();
      remoteDataSource = _FakeChatRemoteDataSource();
      repository = ChatRepositoryImpl(
        remoteDataSource: remoteDataSource,
        secureStorage: const FlutterSecureStorage(),
        cacheStore: cacheStore,
      );
    });

    test('getCachedConversations returns empty list when no cache exists',
        () async {
      final list = await repository.getCachedConversations();
      expect(list, isEmpty);
    });

    test('saveCachedConversations and getCachedConversations work seamlessly',
        () async {
      const conv = Conversation(
        id: 'c-cached-1',
        title: 'Cached Team Chat',
        type: 'GROUP',
        lastMessageSnippet: 'See you tomorrow!',
      );

      await repository.saveCachedConversations([conv]);

      final cached = await repository.getCachedConversations();
      expect(cached.length, 1);
      expect(cached.first.id, 'c-cached-1');
      expect(cached.first.title, 'Cached Team Chat');
      expect(cached.first.lastMessageSnippet, 'See you tomorrow!');
    });

    test('saveCachedMessages and getCachedMessages persist message list',
        () async {
      const msg = Message(
        id: 'm-cached-1',
        conversationId: 'c-cached-1',
        content: 'Cached message text',
        seq: 5,
      );

      await repository.saveCachedMessages('c-cached-1', [msg]);

      final cached = await repository.getCachedMessages('c-cached-1');
      expect(cached.length, 1);
      expect(cached.first.id, 'm-cached-1');
      expect(cached.first.content, 'Cached message text');
      expect(cached.first.seq, 5);
    });

    test('ChatNotifier loads cache data first, then loads loaded API data',
        () async {
      // 1. Seed cache
      const cachedConv = Conversation(
        id: 'conv-cache-1',
        title: 'Cached Conversation',
      );
      await repository.saveCachedConversations([cachedConv]);

      // Seed remote data source with different updated data
      remoteDataSource.conversationsToReturn = [
        const ConversationDto(
          id: 'conv-remote-1',
          title: 'Remote Loaded Conversation',
        ),
      ];

      final stomp = StompService(wsUrl: 'wss://test.leochat.app/ws');
      final notifier = ChatNotifier(
        chatRepository: repository,
        stompService: stomp,
        secureStorage: const FlutterSecureStorage(),
        mediaRepository: _FakeMediaRepo(),
      );

      // Wait for loadConversations
      await notifier.loadConversations();

      // State should now hold the remote loaded data
      expect(notifier.state.conversations.isNotEmpty, isTrue);
      expect(notifier.state.conversations.first.id, 'conv-remote-1');
      expect(notifier.state.conversations.first.title,
          'Remote Loaded Conversation');

      // And the repository cache should have been updated with the new remote data
      final updatedCache = await repository.getCachedConversations();
      expect(updatedCache.first.id, 'conv-remote-1');
    });

    test('AuthRepositoryImpl logout clears all cache and user scope', () async {
      // Seed cache data for user
      await cacheStore.writeJson('cache/user_99/conversations', {'items': []});
      await cacheStore
          .writeJson('cache/user_99/conversations/c-1/messages', {'items': []});

      final authRepo = AuthRepositoryImpl(
        remoteDataSource: _FakeAuthRemoteDataSource(),
        secureStorage: const FlutterSecureStorage(),
        cacheStore: cacheStore,
      );

      final result = await authRepo.logout();
      expect(result.isRight(), isTrue);

      // Verify cache was cleared
      expect(cacheStore.clearAllCalled, isTrue);
      expect(cacheStore.data, isEmpty);
    });
  });
}
