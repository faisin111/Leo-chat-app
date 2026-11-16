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

class ForgotPasswordUseCase {
  const ForgotPasswordUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call(String email) =>
      _repository.forgotPassword(email: email);
}

class ResetPasswordUseCase {
  const ResetPasswordUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call({
    required String token,
    required String newPassword,
  }) =>
      _repository.resetPassword(token: token, newPassword: newPassword);
}

class ChangePasswordUseCase {
  const ChangePasswordUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call({
    required String currentPassword,
    required String newPassword,
  }) =>
      _repository.changePassword(
        currentPassword: currentPassword,
        newPassword: newPassword,
      );
}

class VerifyEmailUseCase {
  const VerifyEmailUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call(String token) =>
      _repository.verifyEmail(token: token);
}

class ResendVerificationUseCase {
  const ResendVerificationUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call({String? email}) =>
      _repository.resendVerification(email: email);
}

class LogoutAllUseCase {
  const LogoutAllUseCase(this._repository);
  final AuthRepository _repository;

  Future<Result<void>> call() => _repository.logoutAll();
}
