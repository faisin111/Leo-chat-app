import '../../../../core/network/result.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';

abstract interface class AiRepository {
  Future<Result<Conversation>> getOrCreateConversation();
  Future<Result<void>> deleteConversation();
  Future<Result<Message>> chat({
    required String conversationId,
    required String prompt,
    String? clientMessageId,
  });
  Future<Result<Map<String, dynamic>>> executeAction(
      Map<String, dynamic> request);
}
