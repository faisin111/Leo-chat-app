import 'package:dio/dio.dart';

import '../models/conversation_dto.dart';
import '../models/conversation_member_dto.dart';
import '../models/message_dto.dart';
import '../models/send_message_request_dto.dart';

abstract interface class ChatRemoteDataSource {
  Future<ConversationPageDto> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  });

  Future<ConversationDto> getConversation(String conversationId);

  Future<ConversationDto> startDirectMessage(String targetUserId);

  Future<ConversationDto> createGroup({
    required String title,
    required List<String> memberIds,
  });

  Future<ConversationDto> updateConversation(
    String conversationId, {
    String? title,
    String? avatarUrl,
  });

  Future<List<ConversationMemberDto>> getMembers(String conversationId);

  Future<void> addGroupMembers(String conversationId, List<String> memberIds);

  Future<void> removeMember(String conversationId, String userId);

  Future<void> updateMemberRole(
    String conversationId,
    String userId,
    String role,
  );

  Future<void> muteConversation(String conversationId, String mutedUntil);

  Future<void> unmuteConversation(String conversationId);

  Future<void> pinConversation(String conversationId);

  Future<void> unpinConversation(String conversationId);

  Future<void> archiveConversation(String conversationId);

  Future<void> unarchiveConversation(String conversationId);

  Future<MessagePageDto> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
    String? query,
  });

  Future<MessageDto> sendMessage(SendMessageRequestDto request);

  Future<MessageDto> editMessage(String messageId, String content);

  Future<void> deleteMessage(String messageId);

  Future<void> addReaction(String messageId, String emoji);

  Future<void> removeReaction(String messageId, String emoji);

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
  Future<ConversationDto> startDirectMessage(String targetUserId) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/conversations/direct',
      data: {'targetUserId': targetUserId},
    );
    return ConversationDto.fromJson(response.data ?? {});
  }

  @override
  Future<ConversationDto> createGroup({
    required String title,
    required List<String> memberIds,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/conversations/group',
      data: {'title': title, 'memberIds': memberIds},
    );
    return ConversationDto.fromJson(response.data ?? {});
  }

  @override
  Future<ConversationDto> updateConversation(
    String conversationId, {
    String? title,
    String? avatarUrl,
  }) async {
    final body = <String, dynamic>{
      if (title != null) 'title': title,
      if (avatarUrl != null) 'avatarUrl': avatarUrl,
    };
    final response = await _dio.patch<Map<String, dynamic>>(
      '/conversations/$conversationId',
      data: body,
    );
    return ConversationDto.fromJson(response.data ?? {});
  }

  @override
  Future<List<ConversationMemberDto>> getMembers(String conversationId) async {
    final response =
        await _dio.get<dynamic>('/conversations/$conversationId/members');
    final rawList = response.data;
    if (rawList is List) {
      return rawList
          .map((item) =>
              ConversationMemberDto.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    return [];
  }

  @override
  Future<void> addGroupMembers(
      String conversationId, List<String> memberIds) async {
    await _dio.post<void>(
      '/conversations/$conversationId/members',
      data: {'memberIds': memberIds},
    );
  }

  @override
  Future<void> removeMember(String conversationId, String userId) async {
    await _dio.delete<void>('/conversations/$conversationId/members/$userId');
  }

  @override
  Future<void> updateMemberRole(
    String conversationId,
    String userId,
    String role,
  ) async {
    await _dio.patch<void>(
      '/conversations/$conversationId/members/$userId/role',
      data: {'role': role},
    );
  }

  @override
  Future<void> muteConversation(
      String conversationId, String mutedUntil) async {
    await _dio.put<void>(
      '/conversations/$conversationId/mute',
      data: {'mutedUntil': mutedUntil},
    );
  }

  @override
  Future<void> unmuteConversation(String conversationId) async {
    await _dio.delete<void>('/conversations/$conversationId/mute');
  }

  @override
  Future<void> pinConversation(String conversationId) async {
    await _dio.put<void>('/conversations/$conversationId/pin');
  }

  @override
  Future<void> unpinConversation(String conversationId) async {
    await _dio.delete<void>('/conversations/$conversationId/pin');
  }

  @override
  Future<void> archiveConversation(String conversationId) async {
    await _dio.put<void>('/conversations/$conversationId/archive');
  }

  @override
  Future<void> unarchiveConversation(String conversationId) async {
    await _dio.delete<void>('/conversations/$conversationId/archive');
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
  Future<MessageDto> editMessage(String messageId, String content) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '/messages/$messageId',
      data: {'content': content},
    );
    return MessageDto.fromJson(response.data ?? {});
  }

  @override
  Future<void> deleteMessage(String messageId) async {
    await _dio.delete<void>('/messages/$messageId');
  }

  @override
  Future<void> addReaction(String messageId, String emoji) async {
    await _dio.post<void>(
      '/messages/$messageId/reactions',
      data: {'emoji': emoji},
    );
  }

  @override
  Future<void> removeReaction(String messageId, String emoji) async {
    final encoded = Uri.encodeComponent(emoji);
    await _dio.delete<void>('/messages/$messageId/reactions/$encoded');
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
