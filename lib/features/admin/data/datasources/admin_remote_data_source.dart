import 'package:dio/dio.dart';

import '../../domain/entities/admin_report.dart';
import '../../domain/entities/admin_stats.dart';
import '../../domain/entities/admin_user.dart';

abstract interface class AdminRemoteDataSource {
  Future<AdminStats> getStatsOverview();

  Future<List<AdminUser>> getUsers({
    String? query,
    String? status,
    int? cursor,
    int limit = 50,
  });

  Future<void> updateUserStatus(String userId, String status, {String? reason});

  Future<void> forceLogoutUser(String userId);

  Future<void> deleteUser(String userId);

  Future<List<AdminReport>> getReports({String? status, int? cursor});

  Future<void> resolveReport(String reportId, String resolution,
      {String? action});

  Future<void> deleteMessage(String messageId, String reason);
}

class AdminRemoteDataSourceImpl implements AdminRemoteDataSource {
  const AdminRemoteDataSourceImpl(this._dio);

  final Dio _dio;

  @override
  Future<AdminStats> getStatsOverview() async {
    final response =
        await _dio.get<Map<String, dynamic>>('/admin/stats/overview');
    final data = response.data ?? {};
    return AdminStats(
      totalUsers: (data['totalUsers'] as num?)?.toInt() ?? 0,
      totalConversations: (data['totalConversations'] as num?)?.toInt() ?? 0,
      totalReports: (data['totalReports'] as num?)?.toInt() ?? 0,
    );
  }

  @override
  Future<List<AdminUser>> getUsers({
    String? query,
    String? status,
    int? cursor,
    int limit = 50,
  }) async {
    final params = <String, dynamic>{
      'limit': limit,
      if (query != null && query.trim().isNotEmpty) 'q': query.trim(),
      if (status != null && status != 'ALL') 'status': status,
      if (cursor != null) 'cursor': cursor,
    };

    final response = await _dio.get<Map<String, dynamic>>(
      '/admin/users',
      queryParameters: params,
    );

    final rawList = response.data?['items'];
    if (rawList is List) {
      return rawList.map((item) {
        final m = item as Map<String, dynamic>;
        return AdminUser(
          id: (m['id'] ?? '').toString(),
          username: (m['username'] ?? '').toString(),
          email: (m['email'] ?? '').toString(),
          displayName: m['displayName']?.toString(),
          role: (m['role'] ?? 'ROLE_USER').toString(),
          status: (m['status'] ?? 'ACTIVE').toString(),
          profilePictureUrl: m['profilePictureUrl']?.toString(),
          createdAt: m['createdAt'] != null
              ? DateTime.tryParse(m['createdAt'].toString())
              : null,
          lastSeenAt: m['lastSeenAt'] != null
              ? DateTime.tryParse(m['lastSeenAt'].toString())
              : null,
        );
      }).toList();
    }
    return [];
  }

  @override
  Future<void> updateUserStatus(String userId, String status,
      {String? reason}) async {
    await _dio.patch<void>(
      '/admin/users/$userId/status',
      data: {'status': status, if (reason != null) 'reason': reason},
    );
  }

  @override
  Future<void> forceLogoutUser(String userId) async {
    await _dio.post<void>('/admin/users/$userId/force-logout');
  }

  @override
  Future<void> deleteUser(String userId) async {
    await _dio.delete<void>('/admin/users/$userId');
  }

  @override
  Future<List<AdminReport>> getReports({String? status, int? cursor}) async {
    final params = <String, dynamic>{
      if (status != null && status != 'ALL') 'status': status,
      if (cursor != null) 'cursor': cursor,
    };

    final response = await _dio.get<Map<String, dynamic>>(
      '/admin/reports',
      queryParameters: params,
    );

    final rawList = response.data?['items'];
    if (rawList is List) {
      return rawList.map((item) {
        final m = item as Map<String, dynamic>;
        return AdminReport(
          id: (m['id'] ?? '').toString(),
          reporterId: (m['reporterId'] ?? '').toString(),
          targetType: (m['targetType'] ?? 'MESSAGE').toString(),
          targetId: (m['targetId'] ?? '').toString(),
          reason: (m['reason'] ?? 'OTHER').toString(),
          details: m['details']?.toString(),
          status: (m['status'] ?? 'OPEN').toString(),
          createdAt: DateTime.tryParse(m['createdAt']?.toString() ?? '') ??
              DateTime.now(),
          resolvedAt: m['resolvedAt'] != null
              ? DateTime.tryParse(m['resolvedAt'].toString())
              : null,
          resolvedBy: m['resolvedBy']?.toString(),
          resolution: m['resolution']?.toString(),
        );
      }).toList();
    }
    return [];
  }

  @override
  Future<void> resolveReport(String reportId, String resolution,
      {String? action}) async {
    await _dio.patch<void>(
      '/admin/reports/$reportId',
      data: {
        'resolution': resolution,
        if (action != null) 'action': action,
      },
    );
  }

  @override
  Future<void> deleteMessage(String messageId, String reason) async {
    await _dio.delete<void>(
      '/admin/messages/$messageId',
      queryParameters: {'reason': reason},
    );
  }
}
