import '../../../../core/network/result.dart';
import '../entities/conversation.dart';
import '../entities/conversation_member.dart';
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
    String? storageKey,
    String? fileName,
    String? mimeType,
    int? sizeBytes,
  });

  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  });

  Future<Result<int>> getUnreadCount();

  Future<Result<Conversation>> getConversation({
    required String conversationId,
  });

  Future<Result<Conversation>> startDirectMessage({
    required String targetUserId,
  });

  Future<Result<Conversation>> createGroup({
    required String title,
    required List<String> memberIds,
  });

  Future<Result<Conversation>> updateConversation({
    required String conversationId,
    String? title,
    String? avatarUrl,
  });

  Future<Result<void>> leaveConversation(String conversationId);

  Future<Result<List<ConversationMember>>> getMembers(String conversationId);

  Future<Result<void>> addGroupMembers({
    required String conversationId,
    required List<String> memberIds,
  });

  Future<Result<void>> removeMember({
    required String conversationId,
    required String userId,
  });

  Future<Result<void>> updateMemberRole({
    required String conversationId,
    required String userId,
    required String role,
  });

  Future<Result<void>> muteConversation({
    required String conversationId,
    required String mutedUntil,
  });

  Future<Result<void>> unmuteConversation(String conversationId);

  Future<Result<void>> pinConversation(String conversationId);

  Future<Result<void>> unpinConversation(String conversationId);

  Future<Result<void>> archiveConversation(String conversationId);

  Future<Result<void>> unarchiveConversation(String conversationId);

  Future<Result<Message>> editMessage({
    required String messageId,
    required String content,
  });

  Future<Result<void>> deleteMessage(String messageId);

  Future<Result<void>> addReaction({
    required String messageId,
    required String emoji,
  });

  Future<Result<void>> removeReaction({
    required String messageId,
    required String emoji,
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
