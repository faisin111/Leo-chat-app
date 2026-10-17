import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/cache/cache_keys.dart';
import '../../../../core/cache/cache_store.dart';
import '../../../../core/errors/app_failure.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/conversation.dart';
import '../../domain/entities/message.dart';
import '../../domain/repositories/chat_repository.dart';
import '../datasources/chat_remote_data_source.dart';
import '../models/conversation_dto.dart';
import '../models/message_dto.dart';
import '../models/send_message_request_dto.dart';

class ChatRepositoryImpl implements ChatRepository {
  ChatRepositoryImpl({
    required ChatRemoteDataSource remoteDataSource,
    required FlutterSecureStorage secureStorage,
    CacheStore? cacheStore,
  })  : _remoteDataSource = remoteDataSource,
        _secureStorage = secureStorage,
        _cacheStore = cacheStore;

  final ChatRemoteDataSource _remoteDataSource;
  final FlutterSecureStorage _secureStorage;
  final CacheStore? _cacheStore;

  static const _keyUserId = 'auth_user_id';
  static const _keyUsername = 'auth_username';

  Future<String> _getUserId() async {
    return await _secureStorage.read(key: _keyUserId) ?? 'anonymous';
  }

  Future<String> _getUsername() async {
    return await _secureStorage.read(key: _keyUsername) ?? '';
  }

  @override
  Future<Result<List<Conversation>>> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async {
    final userId = await _getUserId();
    final cacheKey = CacheKeys.conversations(userId);

    // If searching, directly query remote data source
    if (query != null && query.trim().isNotEmpty) {
      try {
        final pageDto = await _remoteDataSource.getConversations(
          cursor: cursor,
          limit: limit,
          query: query,
        );
        final list = pageDto.items.map((dto) => dto.toDomain()).toList();
        return Right(list);
      } on DioException catch (e) {
        return Left(failureFromDio(e));
      } catch (e) {
        return Left(UnknownFailure(e.toString()));
      }
    }

    try {
      final pageDto = await _remoteDataSource.getConversations(
        cursor: cursor,
        limit: limit,
      );
      final list = pageDto.items.map((dto) => dto.toDomain()).toList();

      // Cache the first page
      if (cursor == null && _cacheStore != null) {
        final cacheMap = {
          'items': pageDto.items.map((e) => e.toJson()).toList(),
        };
        await _cacheStore.writeJson(cacheKey, cacheMap);
      }

      return Right(list);
    } on DioException catch (e) {
      // Try to read from cache when offline / error
      if (_cacheStore != null) {
        final cachedData = await _cacheStore.readJson(cacheKey);
        if (cachedData != null) {
          final cachedPage = ConversationPageDto.fromJson(cachedData);
          if (cachedPage.items.isNotEmpty) {
            return Right(
              cachedPage.items.map((dto) => dto.toDomain()).toList(),
            );
          }
        }
      }
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<List<Message>>> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
  }) async {
    final userId = await _getUserId();
    final currentUsername = await _getUsername();
    final cacheKey = CacheKeys.messages(userId, conversationId);

    try {
      final pageDto = await _remoteDataSource.getMessages(
        conversationId: conversationId,
        beforeSeq: beforeSeq,
        afterSeq: afterSeq,
        limit: limit,
      );

      final list = pageDto.items
          .map((dto) => dto.toDomain(
                currentUserId: userId,
                currentUsername: currentUsername,
              ))
          .toList();

      // Sort by sequence ascending
      list.sort((a, b) => a.seq.compareTo(b.seq));

      if (_cacheStore != null && beforeSeq == null && afterSeq == null) {
        final cacheMap = {
          'items': pageDto.items.map((e) => e.toJson()).toList(),
        };
        await _cacheStore.writeJson(cacheKey, cacheMap);
      }

      return Right(list);
    } on DioException catch (e) {
      if (_cacheStore != null) {
        final cachedData = await _cacheStore.readJson(cacheKey);
        if (cachedData != null) {
          final cachedPage = MessagePageDto.fromJson(cachedData);
          if (cachedPage.items.isNotEmpty) {
            final list = cachedPage.items
                .map((dto) => dto.toDomain(
                      currentUserId: userId,
                      currentUsername: currentUsername,
                    ))
                .toList();
            list.sort((a, b) => a.seq.compareTo(b.seq));
            return Right(list);
          }
        }
      }
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<Message>> sendMessage({
    required String conversationId,
    required String clientMessageId,
    required String content,
    String type = 'TEXT',
    String? replyToId,
  }) async {
    final userId = await _getUserId();
    final currentUsername = await _getUsername();

    try {
      final dto = await _remoteDataSource.sendMessage(
        SendMessageRequestDto(
          conversationId: conversationId,
          clientMessageId: clientMessageId,
          content: content,
          type: type,
          replyToId: replyToId,
        ),
      );
      final message = dto.toDomain(
        currentUserId: userId,
        currentUsername: currentUsername,
      );

      // Invalidate or update message cache
      if (_cacheStore != null) {
        final cacheKey = CacheKeys.messages(userId, conversationId);
        final cached = await _cacheStore.readJson(cacheKey);
        final currentItems = (cached?['items'] as List<dynamic>?) ?? [];
        currentItems.add(dto.toJson());
        await _cacheStore.writeJson(cacheKey, {'items': currentItems});
      }

      return Right(message);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  }) async {
    try {
      await _remoteDataSource.markConversationRead(
        conversationId: conversationId,
        upToSeq: upToSeq,
      );
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<int>> getUnreadCount() async {
    try {
      final count = await _remoteDataSource.getUnreadCount();
      return Right(count);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<Conversation>> getConversation({
    required String conversationId,
  }) async {
    try {
      final dto = await _remoteDataSource.getConversation(conversationId);
      return Right(dto.toDomain());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }
}
