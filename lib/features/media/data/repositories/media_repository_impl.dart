import 'package:dio/dio.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/errors/app_failure.dart';
import '../../../../core/network/result.dart';
import '../../domain/repositories/media_repository.dart';
import '../datasources/media_remote_data_source.dart';

class MediaRepositoryImpl implements MediaRepository {
  const MediaRepositoryImpl(this._remote);

  final MediaRemoteDataSource _remote;

  @override
  Future<Result<String>> uploadVoice({required String filePath}) async {
    try {
      return Right(await _remote.uploadVoice(filePath: filePath));
    } on DioException catch (error) {
      return Left(failureFromDio(error));
    } catch (error) {
      return Left(UnknownFailure(error.toString()));
    }
  }
}
