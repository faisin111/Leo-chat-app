import '../../../../core/network/result.dart';
import '../entities/auth_session.dart';
import '../entities/user.dart';

abstract interface class AuthRepository {
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  });

  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  });

  Future<Result<void>> logout();

  Future<Result<void>> logoutAll();

  Future<Result<void>> forgotPassword({required String email});

  Future<Result<void>> resetPassword({
    required String token,
    required String newPassword,
  });

  Future<Result<void>> changePassword({
    required String currentPassword,
    required String newPassword,
  });

  Future<Result<void>> verifyEmail({required String token});

  Future<Result<void>> resendVerification({String? email});

  Future<Result<User?>> getCurrentUser();
}
