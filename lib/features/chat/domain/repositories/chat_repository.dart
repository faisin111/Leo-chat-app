import '../../../../core/network/result.dart';
import '../entities/conversation.dart';
import '../entities/message.dart';

abstract interface class ChatRepository {
  Future<Result<List<Conversation>>> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  });

  Future<Result<List<Message>>> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
  });

  Future<Result<Message>> sendMessage({
    required String conversationId,
    required String clientMessageId,
    required String content,
    String type = 'TEXT',
    String? replyToId,
  });

  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  });

  Future<Result<int>> getUnreadCount();

  Future<Result<Conversation>> getConversation({
    required String conversationId,
  });

  Future<List<Conversation>> getCachedConversations();

  Future<void> saveCachedConversations(List<Conversation> conversations);

  Future<List<Message>> getCachedMessages(String conversationId);

  Future<void> saveCachedMessages(
    String conversationId,
    List<Message> messages,
  );

  Future<void> clearCache({String? userId});
}
