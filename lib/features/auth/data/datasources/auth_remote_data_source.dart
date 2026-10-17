import 'package:dio/dio.dart';

import '../models/auth_response_dto.dart';
import '../models/login_request_dto.dart';
import '../models/register_request_dto.dart';
import '../models/user_dto.dart';

abstract interface class AuthRemoteDataSource {
  Future<AuthResponseDto> login(LoginRequestDto request);
  Future<AuthResponseDto> register(RegisterRequestDto request);
  Future<UserDto> getMe();
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  const AuthRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<AuthResponseDto> login(LoginRequestDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: request.toJson(),
    );
    final data = response.data ?? <String, dynamic>{};
    return AuthResponseDto.fromJson(data);
  }

  @override
  Future<AuthResponseDto> register(RegisterRequestDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/register',
      data: request.toJson(),
    );
    final data = response.data ?? <String, dynamic>{};
    return AuthResponseDto.fromJson(data);
  }

  @override
  Future<UserDto> getMe() async {
    final response = await _dio.get<Map<String, dynamic>>('/users/me');
    final data = response.data ?? <String, dynamic>{};
    return UserDto.fromJson(data);
  }
}
