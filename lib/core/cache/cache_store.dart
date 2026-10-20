import 'dart:convert';

abstract interface class CacheStore {
  Future<void> writeJson(String key, Object value);
  Future<Map<String, dynamic>?> readJson(String key);
  Future<void> delete(String key);
  Future<void> clearUserScope(String userId);
  Future<void> clearAll();
}

String encodeCache(Object value) => jsonEncode(value);

Map<String, dynamic>? decodeCache(String? value) {
  if (value == null) return null;
  final decoded = jsonDecode(value);
  return decoded is Map<String, dynamic> ? decoded : null;
}
