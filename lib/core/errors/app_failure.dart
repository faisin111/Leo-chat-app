import 'package:dio/dio.dart';

sealed class AppFailure {
  const AppFailure(this.message);
  final String message;
}

final class NetworkFailure extends AppFailure {
  const NetworkFailure(super.message);
}

final class ApiFailure extends AppFailure {
  const ApiFailure({required String message, this.code, this.statusCode})
      : super(message);
  final String? code;
  final int? statusCode;
}

final class UnauthorizedFailure extends AppFailure {
  const UnauthorizedFailure() : super('Your session has expired.');
}

final class UnknownFailure extends AppFailure {
  const UnknownFailure(super.message);
}

AppFailure failureFromDio(DioException error) {
  if (error.type == DioExceptionType.connectionError ||
      error.type == DioExceptionType.connectionTimeout ||
      error.type == DioExceptionType.receiveTimeout) {
    return const NetworkFailure(
        'Unable to connect. Check your internet connection.');
  }
  if (error.response?.statusCode == 401) return const UnauthorizedFailure();
  final body = error.response?.data;
  if (body is Map<String, dynamic>) {
    return ApiFailure(
      message: body['message']?.toString() ?? 'Request failed.',
      code: body['code']?.toString(),
      statusCode: error.response?.statusCode,
    );
  }
  return UnknownFailure(error.message ?? 'Unexpected request failure.');
}
