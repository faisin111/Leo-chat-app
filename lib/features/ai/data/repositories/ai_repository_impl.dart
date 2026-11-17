import 'package:dio/dio.dart';
import 'package:fpdart/fpdart.dart';

import '../../../../core/errors/app_failure.dart';
import '../../../../core/network/result.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import '../../domain/repositories/ai_repository.dart';
import '../datasources/ai_remote_data_source.dart';

class AiRepositoryImpl implements AiRepository {
  const AiRepositoryImpl(this._remoteDataSource);

  final AiRemoteDataSource _remoteDataSource;

  @override
  Future<Result<Conversation>> getOrCreateConversation() async {
    try {
      final dto = await _remoteDataSource.getOrCreateConversation();
      return Right(dto.toDomain());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<void>> deleteConversation() async {
    try {
      await _remoteDataSource.deleteConversation();
      return const Right(null);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<Message>> chat({
    required String conversationId,
    required String prompt,
    String? clientMessageId,
  }) async {
    try {
      final dto = await _remoteDataSource.chat(
        conversationId: conversationId,
        prompt: prompt,
        clientMessageId: clientMessageId,
      );
      return Right(dto.toDomain());
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }

  @override
  Future<Result<Map<String, dynamic>>> executeAction(
      Map<String, dynamic> request) async {
    try {
      final data = await _remoteDataSource.executeAction(request);
      return Right(data);
    } on DioException catch (e) {
      return Left(failureFromDio(e));
    } catch (e) {
      return Left(UnknownFailure(e.toString()));
    }
  }
}
