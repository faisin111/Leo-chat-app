import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/auth_session.dart';
import '../../domain/usecases/auth_use_cases.dart';
import 'auth_state.dart';

class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier({
    required LoginUseCase login,
    required RegisterUseCase register,
    required LogoutUseCase logout,
  })  : _login = login,
        _register = register,
        _logout = logout,
        super(const AuthInitial());

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
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final repository = ref.watch(authRepositoryProvider);
  return AuthNotifier(
    login: LoginUseCase(repository),
    register: RegisterUseCase(repository),
    logout: LogoutUseCase(repository),
  );
});
