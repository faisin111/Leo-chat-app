import 'package:dio/dio.dart';

import 'package:leochat/features/chat/data/models/conversation_dto.dart';
import 'package:leochat/features/chat/data/models/message_dto.dart';

abstract interface class AiRemoteDataSource {
  Future<ConversationDto> getOrCreateConversation();
  Future<void> deleteConversation();
  Future<MessageDto> chat({
    required String conversationId,
    required String prompt,
    String? clientMessageId,
  });
  Future<Map<String, dynamic>> executeAction(Map<String, dynamic> request);
}

class AiRemoteDataSourceImpl implements AiRemoteDataSource {
  const AiRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<ConversationDto> getOrCreateConversation() async {
    final response = await _dio.post<Map<String, dynamic>>('/ai/conversation');
    final data = response.data ?? <String, dynamic>{};
    return ConversationDto.fromJson(data);
  }

  @override
  Future<void> deleteConversation() async {
    await _dio.delete<void>('/ai/conversation');
  }

  @override
  Future<MessageDto> chat({
    required String conversationId,
    required String prompt,
    String? clientMessageId,
  }) async {
    final payload = <String, dynamic>{
      'conversationId': conversationId,
      'prompt': prompt,
      if (clientMessageId != null) 'clientMessageId': clientMessageId,
    };

    final response = await _dio.post<Map<String, dynamic>>(
      '/ai/chat',
      data: payload,
      options: Options(
        receiveTimeout: const Duration(seconds: 90),
      ),
    );
    final data = response.data ?? <String, dynamic>{};
    return MessageDto.fromJson(data);
  }

  @override
  Future<Map<String, dynamic>> executeAction(
      Map<String, dynamic> request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/ai/actions',
      data: request,
    );
    return response.data ?? <String, dynamic>{};
  }
}
