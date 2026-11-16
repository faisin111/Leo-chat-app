import 'package:dio/dio.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/errors/app_failure.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/admin_report.dart';
import '../../domain/entities/admin_stats.dart';
import '../../domain/entities/admin_user.dart';
import '../../domain/repositories/admin_repository.dart';
import '../datasources/admin_remote_data_source.dart';

class AdminRepositoryImpl implements AdminRepository {
  const AdminRepositoryImpl(this._remoteDataSource);

  final AdminRemoteDataSource _remoteDataSource;

  @override
  Future<Result<AdminStats>> getStatsOverview() async {
    try {
      final stats = await _remoteDataSource.getStatsOverview();
      return Right(stats);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<List<AdminUser>>> getUsers({
    String? query,
    String? status,
    int? cursor,
    int limit = 50,
  }) async {
    try {
      final users = await _remoteDataSource.getUsers(
        query: query,
        status: status,
        cursor: cursor,
        limit: limit,
      );
      return Right(users);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> updateUserStatus({
    required String userId,
    required String status,
    String? reason,
  }) async {
    try {
      await _remoteDataSource.updateUserStatus(userId, status, reason: reason);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> forceLogoutUser(String userId) async {
    try {
      await _remoteDataSource.forceLogoutUser(userId);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> deleteUser(String userId) async {
    try {
      await _remoteDataSource.deleteUser(userId);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<List<AdminReport>>> getReports({
    String? status,
    int? cursor,
  }) async {
    try {
      final reports = await _remoteDataSource.getReports(
        status: status,
        cursor: cursor,
      );
      return Right(reports);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> resolveReport({
    required String reportId,
    required String resolution,
    String? action,
  }) async {
    try {
      await _remoteDataSource.resolveReport(reportId, resolution,
          action: action);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> deleteMessage({
    required String messageId,
    required String reason,
  }) async {
    try {
      await _remoteDataSource.deleteMessage(messageId, reason);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }
}
