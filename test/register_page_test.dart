import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/auth/domain/entities/auth_session.dart';
import 'package:leochat/features/auth/domain/entities/user.dart';
import 'package:leochat/features/auth/domain/repositories/auth_repository.dart';
import 'package:leochat/features/auth/presentation/pages/register_page.dart';

class _MockRegisterAuthRepository implements AuthRepository {
  bool registerCalled = false;

  @override
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async =>
      const Right(AuthSession(
        accessToken: 't',
        refreshToken: 'r',
        user: User(
          id: 'u-1',
          username: 'alex',
          email: 'alex@example.com',
          displayName: 'Alex',
        ),
      ));

  @override
  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async {
    registerCalled = true;
    return Right(AuthSession(
      accessToken: 'token_reg',
      refreshToken: 'refresh_reg',
      user: User(
        id: 'u-reg',
        username: username,
        email: email,
        displayName: displayName,
      ),
    ));
  }

  @override
  Future<Result<void>> logout() async => const Right(null);

  @override
  Future<Result<User?>> getCurrentUser() async => const Right(null);
}

void main() {
  testWidgets('register page renders account fields', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: RegisterPage()));
    await tester.pumpAndSettle();

    expect(find.text('Make yourself\nat home.'), findsOneWidget);
    expect(find.text('Display name'), findsOneWidget);
    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Email address'), findsOneWidget);
    expect(find.text('Create password'), findsOneWidget);
    expect(
        find.text(
            'I agree to the Terms of Service and Privacy Policy. Let’s keep this a kind place.'),
        findsOneWidget);
  });

  testWidgets('register page validates empty fields and shows snackbar error',
      (tester) async {
    await tester.pumpWidget(const MaterialApp(home: RegisterPage()));
    await tester.pumpAndSettle();

    await tester.ensureVisible(find.text('Create account'));
    await tester.tap(find.text('Create account'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Please fill in all required fields.'), findsOneWidget);
  });

  testWidgets('register page validates invalid email and shows snackbar error',
      (tester) async {
    await tester.pumpWidget(const MaterialApp(home: RegisterPage()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField).at(0), 'Alex Morgan');
    await tester.enterText(find.byType(TextField).at(1), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(2), 'invalid-email');
    await tester.enterText(find.byType(TextField).at(3), 'password123');

    await tester.ensureVisible(find.text('Create account'));
    await tester.tap(find.text('Create account'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Please enter a valid email address.'), findsOneWidget);
  });

  testWidgets('register page validates short password and shows snackbar error',
      (tester) async {
    await tester.pumpWidget(const MaterialApp(home: RegisterPage()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField).at(0), 'Alex Morgan');
    await tester.enterText(find.byType(TextField).at(1), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(2), 'alex@example.com');
    await tester.enterText(find.byType(TextField).at(3), 'short');

    await tester.ensureVisible(find.text('Create account'));
    await tester.tap(find.text('Create account'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(
        find.text('Password must be at least 8 characters.'), findsOneWidget);
  });

  testWidgets(
      'register page succeeds with valid inputs and shows success snackbar',
      (tester) async {
    final mockRepo = _MockRegisterAuthRepository();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          authRepositoryProvider.overrideWithValue(mockRepo),
        ],
        child: const MaterialApp(home: RegisterPage()),
      ),
    );
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField).at(0), 'Alex Morgan');
    await tester.enterText(find.byType(TextField).at(1), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(2), 'alex@example.com');
    await tester.enterText(find.byType(TextField).at(3), 'securePassword123');

    await tester.ensureVisible(find.text('Create account'));
    await tester.tap(find.text('Create account'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(mockRepo.registerCalled, isTrue);
    expect(find.text('Account created successfully! Welcome to LeoChat.'),
        findsOneWidget);
  });
}
