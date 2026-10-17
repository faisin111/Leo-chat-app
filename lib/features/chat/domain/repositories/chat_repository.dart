import '../../../../core/network/result.dart';

abstract interface class ChatRepository {
  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  });
}
