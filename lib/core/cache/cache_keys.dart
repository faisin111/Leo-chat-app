abstract final class CacheKeys {
  static String conversations(String userId) => 'cache/$userId/conversations';
  static String messages(String userId, String conversationId) =>
      'cache/$userId/conversations/$conversationId/messages';
}
