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
    dio.interceptors.add(_AuthInterceptor(dio, config, storage));
  }

  final Dio dio;
}

class _AuthInterceptor extends QueuedInterceptor {
  _AuthInterceptor(this._dio, this._config, this._storage);

  final Dio _dio;
  final AppConfig _config;
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
    final isAuthPath = path.startsWith('/auth/login') ||
        path.startsWith('/auth/register') ||
        path.startsWith('/auth/logout') ||
        path.startsWith('/auth/refresh') ||
        path.startsWith('/auth/forgot-password') ||
        path.startsWith('/auth/reset-password');

    if (err.response?.statusCode == 401 && !isAuthPath) {
      final refreshToken = await _storage.read(key: 'refreshToken');
      if (refreshToken != null && refreshToken.isNotEmpty) {
        try {
          final refreshDio = Dio(BaseOptions(
            baseUrl: _config.apiBaseUrl,
            connectTimeout: const Duration(seconds: 10),
            receiveTimeout: const Duration(seconds: 10),
          ));

          final refreshResponse = await refreshDio.post<Map<String, dynamic>>(
            '/auth/refresh',
            data: {'refreshToken': refreshToken},
          );

          if (refreshResponse.statusCode == 200 &&
              refreshResponse.data != null) {
            final newAccessToken =
                refreshResponse.data!['accessToken']?.toString();
            final newRefreshToken =
                refreshResponse.data!['refreshToken']?.toString();

            if (newAccessToken != null && newAccessToken.isNotEmpty) {
              await _storage.write(key: 'accessToken', value: newAccessToken);
              if (newRefreshToken != null && newRefreshToken.isNotEmpty) {
                await _storage.write(
                    key: 'refreshToken', value: newRefreshToken);
              }

              // Retry original request with fresh token
              final retryOptions = err.requestOptions;
              retryOptions.headers['Authorization'] = 'Bearer $newAccessToken';
              final cloneReq = await _dio.fetch(retryOptions);
              return handler.resolve(cloneReq);
            }
          }
        } catch (_) {
          // Refresh failed - fall through to session clearance
        }
      }

      await AuthSessionManager.clearLocalSession(_storage);
      AuthSessionEvents.notifyExpired();
    }
    handler.next(err);
  }
}
