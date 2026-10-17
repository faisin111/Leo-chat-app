import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/errors/app_failure.dart';
import 'package:leochat/features/auth/data/datasources/auth_remote_data_source.dart';
import 'package:leochat/features/auth/data/models/auth_response_dto.dart';
import 'package:leochat/features/auth/data/models/login_request_dto.dart';
import 'package:leochat/features/auth/data/models/register_request_dto.dart';
import 'package:leochat/features/auth/data/models/user_dto.dart';
import 'package:leochat/features/auth/data/repositories/auth_repository_impl.dart';

class _FakeAuthRemoteDataSource implements AuthRemoteDataSource {
  @override
  Future<void> logout() async {}
  bool shouldThrowDioError = false;

  @override
  Future<AuthResponseDto> login(LoginRequestDto request) async {
    if (shouldThrowDioError) {
      throw DioException(
        requestOptions: RequestOptions(path: '/auth/login'),
        response: Response(
          requestOptions: RequestOptions(path: '/auth/login'),
          statusCode: 401,
          data: {'message': 'Bad credentials', 'code': 'UNAUTHORIZED'},
        ),
      );
    }
    return const AuthResponseDto(
      accessToken: 'access_123',
      refreshToken: 'refresh_123',
      user: UserDto(
        id: 'u-1',
        username: 'alex',
        email: 'alex@example.com',
        displayName: 'Alex Morgan',
      ),
    );
  }

  @override
  Future<AuthResponseDto> register(RegisterRequestDto request) async {
    if (shouldThrowDioError) {
      throw DioException(
        requestOptions: RequestOptions(path: '/auth/register'),
        response: Response(
          requestOptions: RequestOptions(path: '/auth/register'),
          statusCode: 400,
          data: {'message': 'Username already taken'},
        ),
      );
    }
    return AuthResponseDto(
      accessToken: 'access_register',
      refreshToken: 'refresh_register',
      user: UserDto(
        id: 'u-2',
        username: request.username,
        email: request.email,
        displayName: request.displayName,
      ),
    );
  }

  @override
  Future<UserDto> getMe() async => const UserDto(
        id: 'u-1',
        username: 'alex',
        email: 'alex@example.com',
        displayName: 'Alex Morgan',
      );
}

class _FakeSecureStorage extends FlutterSecureStorage {
  final Map<String, String> _storage = {};

  @override
  Future<void> write({
    required String key,
    required String? value,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    if (value != null) {
      _storage[key] = value;
    }
  }

  @override
  Future<String?> read({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async =>
      _storage[key];

  @override
  Future<void> delete({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    _storage.remove(key);
  }
}

void main() {
  late _FakeAuthRemoteDataSource remoteDataSource;
  late _FakeSecureStorage secureStorage;
  late AuthRepositoryImpl repository;

  setUp(() {
    remoteDataSource = _FakeAuthRemoteDataSource();
    secureStorage = _FakeSecureStorage();
    repository = AuthRepositoryImpl(
      remoteDataSource: remoteDataSource,
      secureStorage: secureStorage,
    );
  });

  group('AuthRepositoryImpl', () {
    test('login success persists tokens and returns session', () async {
      final result = await repository.login(
        username: 'alex',
        password: 'password123',
      );

      expect(result.isRight(), isTrue);
      result.fold(
        (_) => fail('Expected Right'),
        (session) {
          expect(session.accessToken, 'access_123');
          expect(session.user.username, 'alex');
        },
      );
      expect(await secureStorage.read(key: 'accessToken'), 'access_123');
      expect(await secureStorage.read(key: 'refreshToken'), 'refresh_123');
    });

    test('login failure maps DioException to AppFailure', () async {
      remoteDataSource.shouldThrowDioError = true;

      final result = await repository.login(
        username: 'alex',
        password: 'wrongpassword',
      );

      expect(result.isLeft(), isTrue);
      result.fold(
        (failure) => expect(failure, isA<UnauthorizedFailure>()),
        (_) => fail('Expected Left'),
      );
    });

    test('register success persists tokens and returns session', () async {
      final result = await repository.register(
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
        displayName: 'New User',
      );

      expect(result.isRight(), isTrue);
      result.fold(
        (_) => fail('Expected Right'),
        (session) {
          expect(session.accessToken, 'access_register');
          expect(session.user.displayName, 'New User');
        },
      );
      expect(await secureStorage.read(key: 'accessToken'), 'access_register');
    });

    test('logout deletes stored tokens', () async {
      await secureStorage.write(key: 'accessToken', value: 'token');
      await repository.logout();
      expect(await secureStorage.read(key: 'accessToken'), isNull);
    });
  });
}
