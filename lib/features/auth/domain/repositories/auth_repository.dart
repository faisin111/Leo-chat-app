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

  Future<Result<User?>> getCurrentUser();
}
