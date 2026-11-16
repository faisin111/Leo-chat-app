import 'package:dio/dio.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/errors/app_failure.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/user_profile.dart';
import '../../domain/entities/user_search_item.dart';
import '../../domain/entities/user_session_item.dart';
import '../../domain/repositories/users_repository.dart';
import '../datasources/users_remote_data_source.dart';

class UsersRepositoryImpl implements UsersRepository {
  const UsersRepositoryImpl(this._remoteDataSource);

  final UsersRemoteDataSource _remoteDataSource;

  @override
  Future<Result<UserProfile>> getMe() async {
    try {
      final dto = await _remoteDataSource.getMe();
      return Right(dto.toDomain());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<UserProfile>> getUserById(String id) async {
    try {
      final dto = await _remoteDataSource.getUserById(id);
      return Right(dto.toDomain());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<List<UserSearchItem>>> searchUsers({
    required String query,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final dtos = await _remoteDataSource.searchUsers(
        query: query,
        page: page,
        size: size,
      );
      return Right(dtos.map((d) => d.toDomain()).toList());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> updateProfile({
    String? displayName,
    String? profilePictureUrl,
    String? phoneNumber,
    String? bio,
    int? age,
    String? region,
  }) async {
    try {
      await _remoteDataSource.updateProfile(
        displayName: displayName,
        profilePictureUrl: profilePictureUrl,
        phoneNumber: phoneNumber,
        bio: bio,
        age: age,
        region: region,
      );
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<List<UserSessionItem>>> getSessions() async {
    try {
      final dtos = await _remoteDataSource.getSessions();
      return Right(dtos.map((d) => d.toDomain()).toList());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> revokeSession(String sessionId) async {
    try {
      await _remoteDataSource.revokeSession(sessionId);
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> deactivateAccount() async {
    try {
      await _remoteDataSource.deactivateAccount();
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }
}
