import 'package:dio/dio.dart';

import '../models/user_profile_dto.dart';
import '../models/user_search_item_dto.dart';
import '../models/user_session_item_dto.dart';

abstract interface class UsersRemoteDataSource {
  Future<UserProfileDto> getMe();

  Future<UserProfileDto> getUserById(String id);

  Future<List<UserSearchItemDto>> searchUsers({
    required String query,
    int page = 0,
    int size = 20,
  });

  Future<void> updateProfile({
    String? displayName,
    String? profilePictureUrl,
    String? phoneNumber,
    String? bio,
    int? age,
    String? region,
  });

  Future<List<UserSessionItemDto>> getSessions();

  Future<void> revokeSession(String sessionId);

  Future<void> deactivateAccount();
}

class UsersRemoteDataSourceImpl implements UsersRemoteDataSource {
  const UsersRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<UserProfileDto> getMe() async {
    final response = await _dio.get<Map<String, dynamic>>('/users/me');
    return UserProfileDto.fromJson(response.data ?? {});
  }

  @override
  Future<UserProfileDto> getUserById(String id) async {
    final response = await _dio.get<Map<String, dynamic>>('/users/$id');
    return UserProfileDto.fromJson(response.data ?? {});
  }

  @override
  Future<List<UserSearchItemDto>> searchUsers({
    required String query,
    int page = 0,
    int size = 20,
  }) async {
    final response = await _dio.get<dynamic>(
      '/users/search',
      queryParameters: {
        'q': query.trim(),
        'page': page,
        'size': size,
      },
    );

    final rawList = response.data;
    if (rawList is List) {
      return rawList
          .map((item) =>
              UserSearchItemDto.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    return [];
  }

  @override
  Future<void> updateProfile({
    String? displayName,
    String? profilePictureUrl,
    String? phoneNumber,
    String? bio,
    int? age,
    String? region,
  }) async {
    final body = <String, dynamic>{
      if (displayName != null) 'displayName': displayName,
      if (profilePictureUrl != null) 'profilePictureUrl': profilePictureUrl,
      if (phoneNumber != null) 'phoneNumber': phoneNumber,
      if (bio != null) 'bio': bio,
      if (age != null) 'age': age,
      if (region != null) 'region': region,
    };

    await _dio.patch<void>('/users/me/profile', data: body);
  }

  @override
  Future<List<UserSessionItemDto>> getSessions() async {
    final response = await _dio.get<dynamic>('/users/me/sessions');
    final rawList = response.data;
    if (rawList is List) {
      return rawList
          .map((item) =>
              UserSessionItemDto.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    return [];
  }

  @override
  Future<void> revokeSession(String sessionId) async {
    await _dio.delete<void>('/users/me/sessions/$sessionId');
  }

  @override
  Future<void> deactivateAccount() async {
    await _dio.delete<void>('/users/me');
  }
}
