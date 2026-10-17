import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/errors/app_failure.dart';
import '../../../../core/auth/auth_session_manager.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/auth_session.dart';
import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_data_source.dart';
import '../models/login_request_dto.dart';
import '../models/register_request_dto.dart';

class AuthRepositoryImpl implements AuthRepository {
  const AuthRepositoryImpl({
    required AuthRemoteDataSource remoteDataSource,
    required FlutterSecureStorage secureStorage,
  })  : _remoteDataSource = remoteDataSource,
        _secureStorage = secureStorage;

  final AuthRemoteDataSource _remoteDataSource;
  final FlutterSecureStorage _secureStorage;

  static const _keyAccessToken = 'accessToken';
  static const _keyRefreshToken = 'refreshToken';
  static const _keyUserId = 'auth_user_id';
  static const _keyUsername = 'auth_username';
  static const _keyEmail = 'auth_email';
  static const _keyDisplayName = 'auth_display_name';
  static const _keyRole = 'auth_role';

  @override
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async {
    try {
      final dto = await _remoteDataSource.login(
        LoginRequestDto(username: username.trim(), password: password),
      );
      final session = dto.toDomain();
      await _persistSession(session);
      return Right(session);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async {
    try {
      final dto = await _remoteDataSource.register(
        RegisterRequestDto(
          username: username.trim(),
          email: email.trim(),
          password: password,
          displayName: displayName.trim(),
        ),
      );
      final session = dto.toDomain();
      await _persistSession(session);
      return Right(session);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> logout() async {
    try {
      try {
        await _remoteDataSource.logout();
      } on DioException {
        // JWT access tokens are stateless; still clear local credentials.
      }
      await AuthSessionManager.clearLocalSession(_secureStorage);
      return const Right(null);
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<User?>> getCurrentUser() async {
    try {
      final token = await _secureStorage.read(key: _keyAccessToken);
      if (token == null || token.isEmpty) {
        return const Right(null);
      }
      try {
        final dto = await _remoteDataSource.getMe();
        final user = dto.toDomain();
        await _persistUser(user);
        return Right(user);
      } catch (_) {
        // Fallback to locally stored user profile when offline
        final userId = await _secureStorage.read(key: _keyUserId);
        final username = await _secureStorage.read(key: _keyUsername);
        if (userId != null && username != null) {
          final email = await _secureStorage.read(key: _keyEmail) ?? '';
          final displayName =
              await _secureStorage.read(key: _keyDisplayName) ?? username;
          final role = await _secureStorage.read(key: _keyRole) ?? 'USER';
          return Right(User(
            id: userId,
            username: username,
            email: email,
            displayName: displayName,
            role: role,
          ));
        }
        return const Right(null);
      }
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  Future<void> _persistSession(AuthSession session) async {
    if (session.accessToken.isNotEmpty) {
      await _secureStorage.write(
        key: _keyAccessToken,
        value: session.accessToken,
      );
    }
    if (session.refreshToken.isNotEmpty) {
      await _secureStorage.write(
        key: _keyRefreshToken,
        value: session.refreshToken,
      );
    }
    await _persistUser(session.user);
  }

  Future<void> _persistUser(User user) async {
    if (user.id.isNotEmpty) {
      await _secureStorage.write(key: _keyUserId, value: user.id);
    }
    if (user.username.isNotEmpty) {
      await _secureStorage.write(key: _keyUsername, value: user.username);
    }
    if (user.email.isNotEmpty) {
      await _secureStorage.write(key: _keyEmail, value: user.email);
    }
    if (user.displayName.isNotEmpty) {
      await _secureStorage.write(key: _keyDisplayName, value: user.displayName);
    }
    if (user.role.isNotEmpty) {
      await _secureStorage.write(key: _keyRole, value: user.role);
    }
  }
}
