import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthSessionManager {
  AuthSessionManager._();

  static const _keys = [
    'accessToken',
    'refreshToken',
    'auth_user_id',
    'auth_username',
    'auth_email',
    'auth_display_name',
    'auth_role',
  ];

  static Future<void> clearLocalSession(
      FlutterSecureStorage secureStorage) async {
    await Future.wait(_keys.map((key) => secureStorage.delete(key: key)));
    final preferences = await SharedPreferences.getInstance();
    await preferences.clear();
  }
}
