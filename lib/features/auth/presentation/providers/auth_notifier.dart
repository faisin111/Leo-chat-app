import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/network/result.dart';
import '../../domain/entities/auth_session.dart';
import '../../domain/repositories/auth_repository.dart';
import 'auth_state.dart';

class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier(this._repository) : super(const AuthInitial());

  final AuthRepository _repository;

  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async {
    state = const AuthLoading();
    final result = await _repository.login(
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
    final result = await _repository.register(
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
    await _repository.logout();
    state = const AuthUnauthenticated();
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(ref.watch(authRepositoryProvider));
});
