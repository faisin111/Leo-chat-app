import 'package:dio/dio.dart';

import '../models/conversation_dto.dart';
import '../models/message_dto.dart';
import '../models/send_message_request_dto.dart';

abstract interface class ChatRemoteDataSource {
  Future<ConversationPageDto> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  });

  Future<ConversationDto> getConversation(String conversationId);

  Future<MessagePageDto> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
    String? query,
  });

  Future<MessageDto> sendMessage(SendMessageRequestDto request);

  Future<void> markConversationRead({
    required String conversationId,
    required int upToSeq,
  });

  Future<int> getUnreadCount();
}

class ChatRemoteDataSourceImpl implements ChatRemoteDataSource {
  ChatRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<ConversationPageDto> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async {
    final queryParams = <String, dynamic>{
      'limit': limit,
      if (cursor != null) 'cursor': cursor,
      if (query != null && query.trim().isNotEmpty) 'q': query.trim(),
    };

    final response = await _dio.get<Map<String, dynamic>>(
      '/conversations',
      queryParameters: queryParams,
    );
    return ConversationPageDto.fromJson(response.data ?? {});
  }

  @override
  Future<ConversationDto> getConversation(String conversationId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/conversations/$conversationId',
    );
    return ConversationDto.fromJson(response.data ?? {});
  }

  @override
  Future<MessagePageDto> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
    String? query,
  }) async {
    final queryParams = <String, dynamic>{
      'limit': limit,
      if (beforeSeq != null) 'beforeSeq': beforeSeq,
      if (afterSeq != null) 'afterSeq': afterSeq,
      if (query != null && query.trim().isNotEmpty) 'q': query.trim(),
    };

    final response = await _dio.get<Map<String, dynamic>>(
      '/conversations/$conversationId/messages',
      queryParameters: queryParams,
    );
    return MessagePageDto.fromJson(response.data ?? {});
  }

  @override
  Future<MessageDto> sendMessage(SendMessageRequestDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/conversations/${request.conversationId}/messages',
      data: request.toJson(),
    );
    return MessageDto.fromJson(response.data ?? {});
  }

  @override
  Future<void> markConversationRead({
    required String conversationId,
    required int upToSeq,
  }) async {
    await _dio.post<void>(
      '/conversations/$conversationId/read',
      data: {'upToSeq': upToSeq},
    );
  }

  @override
  Future<int> getUnreadCount() async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/conversations/unread-count',
    );
    final count = response.data?['unreadCount'];
    return (count as num?)?.toInt() ?? 0;
  }
}
