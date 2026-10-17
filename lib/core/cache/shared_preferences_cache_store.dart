import 'package:shared_preferences/shared_preferences.dart';

import 'cache_store.dart';

class SharedPreferencesCacheStore implements CacheStore {
  SharedPreferencesCacheStore(this._preferences);

  final SharedPreferences _preferences;

  @override
  Future<void> writeJson(String key, Object value) async {
    await _preferences.setString(key, encodeCache(value));
  }

  @override
  Future<Map<String, dynamic>?> readJson(String key) async {
    return decodeCache(_preferences.getString(key));
  }

  @override
  Future<void> delete(String key) async {
    await _preferences.remove(key);
  }

  @override
  Future<void> clearUserScope(String userId) async {
    final prefix = 'cache/$userId/';
    for (final key
        in _preferences.getKeys().where((key) => key.startsWith(prefix))) {
      await _preferences.remove(key);
    }
  }
}
