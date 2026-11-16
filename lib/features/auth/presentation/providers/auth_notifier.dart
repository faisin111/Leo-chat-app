import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/auth_session.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/usecases/auth_use_cases.dart';
import 'auth_state.dart';

class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier({
    required AuthRepository repository,
    required LoginUseCase login,
    required RegisterUseCase register,
    required LogoutUseCase logout,
  })  : _repository = repository,
        _login = login,
        _register = register,
        _logout = logout,
        super(const AuthInitial());

  final AuthRepository _repository;
  final LoginUseCase _login;
  final RegisterUseCase _register;
  final LogoutUseCase _logout;

  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async {
    state = const AuthLoading();
    final result = await _login(
      username: username,
      password: password,
    );
    result.fold(
      (failure) => state = AuthError(failure.message),
      (session) => state = Authenticated(session),
    );
    return result;
  }

  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async {
    state = const AuthLoading();
    final result = await _register(
      username: username,
      email: email,
      password: password,
      displayName: displayName,
    );
    result.fold(
      (failure) => state = AuthError(failure.message),
      (session) => state = Authenticated(session),
    );
    return result;
  }

  Future<void> logout() async {
    state = const AuthLoading();
    await _logout();
    state = const AuthUnauthenticated();
  }

  Future<Result<void>> forgotPassword(String email) async {
    return _repository.forgotPassword(email: email);
  }

  Future<Result<void>> resetPassword({
    required String token,
    required String newPassword,
  }) async {
    return _repository.resetPassword(token: token, newPassword: newPassword);
  }

  Future<Result<void>> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    return _repository.changePassword(
      currentPassword: currentPassword,
      newPassword: newPassword,
    );
  }

  Future<Result<void>> verifyEmail(String token) async {
    return _repository.verifyEmail(token: token);
  }

  Future<Result<void>> resendVerification({String? email}) async {
    return _repository.resendVerification(email: email);
  }

  Future<void> logoutAll() async {
    state = const AuthLoading();
    await _repository.logoutAll();
    state = const AuthUnauthenticated();
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final repository = ref.watch(authRepositoryProvider);
  return AuthNotifier(
    repository: repository,
    login: LoginUseCase(repository),
    register: RegisterUseCase(repository),
    logout: LogoutUseCase(repository),
  );
});
