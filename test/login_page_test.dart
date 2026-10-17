import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/auth/domain/entities/auth_session.dart';
import 'package:leochat/features/auth/domain/entities/user.dart';
import 'package:leochat/features/auth/domain/repositories/auth_repository.dart';
import 'package:leochat/features/auth/presentation/pages/login_page.dart';

class _MockAuthRepository implements AuthRepository {
  bool loginCalled = false;

  @override
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async {
    loginCalled = true;
    return const Right(AuthSession(
      accessToken: 'token_1',
      refreshToken: 'refresh_1',
      user: User(
        id: 'u-1',
        username: 'alex',
        email: 'alex@example.com',
        displayName: 'Alex',
      ),
    ));
  }

  @override
  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async =>
      const Right(AuthSession(
        accessToken: 'token',
        refreshToken: 'refresh',
        user: User(
          id: 'u-1',
          username: 'alex',
          email: 'alex@example.com',
          displayName: 'Alex',
        ),
      ));

  @override
  Future<Result<void>> logout() async => const Right(null);

  @override
  Future<Result<User?>> getCurrentUser() async => const Right(null);
}

void main() {
  testWidgets('login page uses username and password fields', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: LoginPage()));
    await tester.pumpAndSettle();

    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
    expect(find.text('Your people.\nA little closer.'), findsOneWidget);
  });

  testWidgets('tapping sign in with empty fields shows error snackbar',
      (tester) async {
    await tester.pumpWidget(const MaterialApp(home: LoginPage()));
    await tester.pumpAndSettle();

    await tester.ensureVisible(find.text('Sign in'));
    await tester.tap(find.text('Sign in'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Please fill in all required fields.'), findsOneWidget);
  });

  testWidgets('successful login calls repository and shows success snackbar',
      (tester) async {
    final mockRepo = _MockAuthRepository();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          authRepositoryProvider.overrideWithValue(mockRepo),
        ],
        child: const MaterialApp(home: LoginPage()),
      ),
    );
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField).at(0), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(1), 'password123');
    await tester.ensureVisible(find.text('Sign in'));
    await tester.tap(find.text('Sign in'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(mockRepo.loginCalled, isTrue);
    expect(find.text('Welcome back! You’re signed in.'), findsOneWidget);
  });
}
