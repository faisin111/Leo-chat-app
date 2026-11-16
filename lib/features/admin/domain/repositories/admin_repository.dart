import '../../../../core/network/result.dart';
import '../entities/admin_report.dart';
import '../entities/admin_stats.dart';
import '../entities/admin_user.dart';

abstract interface class AdminRepository {
  Future<Result<AdminStats>> getStatsOverview();

  Future<Result<List<AdminUser>>> getUsers({
    String? query,
    String? status,
    int? cursor,
    int limit = 50,
  });

  Future<Result<void>> updateUserStatus({
    required String userId,
    required String status,
    String? reason,
  });

  Future<Result<void>> forceLogoutUser(String userId);

  Future<Result<void>> deleteUser(String userId);

  Future<Result<List<AdminReport>>> getReports({
    String? status,
    int? cursor,
  });

  Future<Result<void>> resolveReport({
    required String reportId,
    required String resolution,
    String? action,
  });

  Future<Result<void>> deleteMessage({
    required String messageId,
    required String reason,
  });
}
