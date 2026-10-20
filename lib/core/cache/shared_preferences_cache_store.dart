import 'package:shared_preferences/shared_preferences.dart';

import 'cache_store.dart';

class SharedPreferencesCacheStore implements CacheStore {
  SharedPreferencesCacheStore([this._preferences]);

  SharedPreferences? _preferences;

  Future<SharedPreferences> _getPrefs() async {
    return _preferences ??= await SharedPreferences.getInstance();
  }

  @override
  Future<void> writeJson(String key, Object value) async {
    final prefs = await _getPrefs();
    await prefs.setString(key, encodeCache(value));
  }

  @override
  Future<Map<String, dynamic>?> readJson(String key) async {
    final prefs = await _getPrefs();
    return decodeCache(prefs.getString(key));
  }

  @override
  Future<void> delete(String key) async {
    final prefs = await _getPrefs();
    await prefs.remove(key);
  }

  @override
  Future<void> clearUserScope(String userId) async {
    final prefs = await _getPrefs();
    final prefix = 'cache/$userId/';
    for (final key in prefs.getKeys().where((key) => key.startsWith(prefix))) {
      await prefs.remove(key);
    }
  }

  @override
  Future<void> clearAll() async {
    final prefs = await _getPrefs();
    for (final key
        in prefs.getKeys().where((key) => key.startsWith('cache/'))) {
      await prefs.remove(key);
    }
  }
}
