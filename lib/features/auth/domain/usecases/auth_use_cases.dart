import '../../../../core/network/result.dart';
import '../entities/auth_session.dart';
import '../entities/user.dart';
import '../repositories/auth_repository.dart';

/// Application actions keep presentation controllers independent from
/// repository method details.
class LoginUseCase {
  const LoginUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<AuthSession>> call({
    required String username,
    required String password,
  }) =>
      _repository.login(username: username, password: password);
}

class RegisterUseCase {
  const RegisterUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<AuthSession>> call({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) =>
      _repository.register(
        username: username,
        email: email,
        password: password,
        displayName: displayName,
      );
}

class LogoutUseCase {
  const LogoutUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call() => _repository.logout();
}

class GetCurrentUserUseCase {
  const GetCurrentUserUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<User?>> call() => _repository.getCurrentUser();
}
