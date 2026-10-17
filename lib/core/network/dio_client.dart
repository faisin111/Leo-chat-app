import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../config/app_config.dart';
import '../auth/auth_session_events.dart';
import '../auth/auth_session_manager.dart';

class DioClient {
  DioClient({required AppConfig config, required FlutterSecureStorage storage})
      : dio = Dio(BaseOptions(
          baseUrl: config.apiBaseUrl,
          connectTimeout: const Duration(seconds: 15),
          receiveTimeout: const Duration(seconds: 20),
          headers: {'Accept': 'application/json'},
        )) {
    dio.interceptors.add(_AuthInterceptor(storage));
  }

  final Dio dio;
}

class _AuthInterceptor extends Interceptor {
  _AuthInterceptor(this._storage);
  final FlutterSecureStorage _storage;

  @override
  Future<void> onRequest(
      RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _storage.read(key: 'accessToken');
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
      DioException err, ErrorInterceptorHandler handler) async {
    final path = err.requestOptions.path;
    if (err.response?.statusCode == 401 &&
        path != '/auth/login' &&
        path != '/auth/register' &&
        path != '/auth/logout') {
      await AuthSessionManager.clearLocalSession(_storage);
      AuthSessionEvents.notifyExpired();
    }
    handler.next(err);
  }
}
