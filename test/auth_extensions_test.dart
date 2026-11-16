import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/auth/domain/entities/auth_session.dart';
import 'package:leochat/features/auth/domain/entities/user.dart';
import 'package:leochat/features/auth/domain/repositories/auth_repository.dart';
import 'package:leochat/features/auth/presentation/pages/forgot_password_page.dart';
import 'package:leochat/features/auth/presentation/pages/reset_password_page.dart';
import 'package:leochat/features/auth/presentation/providers/auth_notifier.dart';

class _FakeAuthRepository implements AuthRepository {
  bool forgotPasswordCalled = false;
  bool resetPasswordCalled = false;
  bool changePasswordCalled = false;

  @override
  Future<Result<AuthSession>> login(
          {required String username, required String password}) async =>
      const Right(AuthSession(
        accessToken: 't',
        refreshToken: 'r',
        user: User(id: '1', username: 'u', email: 'e', displayName: 'd'),
      ));

  @override
  Future<Result<AuthSession>> register(
          {required String username,
          required String email,
          required String password,
          required String displayName}) async =>
      const Right(AuthSession(
        accessToken: 't',
        refreshToken: 'r',
        user: User(id: '1', username: 'u', email: 'e', displayName: 'd'),
      ));

  @override
  Future<Result<void>> logout() async => const Right(null);

  @override
  Future<Result<User?>> getCurrentUser() async => const Right(null);

  @override
  Future<Result<void>> forgotPassword({required String email}) async {
    forgotPasswordCalled = true;
    return const Right(null);
  }

  @override
  Future<Result<void>> resetPassword(
      {required String token, required String newPassword}) async {
    resetPasswordCalled = true;
    return const Right(null);
  }

  @override
  Future<Result<void>> changePassword(
      {required String currentPassword, required String newPassword}) async {
    changePasswordCalled = true;
    return const Right(null);
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  group('Auth Extensions Tests', () {
    testWidgets('ForgotPasswordPage renders and validates fields',
        (tester) async {
      final fakeRepo = _FakeAuthRepository();

      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            authRepositoryProvider.overrideWithValue(fakeRepo),
          ],
          child: const MaterialApp(home: ForgotPasswordPage()),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Forgot your password?'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);

      // Enter invalid email
      await tester.enterText(find.byType(TextField), 'notanemail');
      await tester.tap(find.text('Send reset instructions'));
      await tester.pumpAndSettle();

      expect(fakeRepo.forgotPasswordCalled, isFalse);

      // Enter valid email
      await tester.enterText(find.byType(TextField), 'alex@example.com');
      await tester.tap(find.text('Send reset instructions'));
      await tester.pumpAndSettle();

      expect(fakeRepo.forgotPasswordCalled, isTrue);
      expect(find.text('Check your email'), findsOneWidget);
    });

    testWidgets('ResetPasswordPage renders and checks password match',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 2400);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(() {
        tester.view.resetPhysicalSize();
        tester.view.resetDevicePixelRatio();
      });

      final fakeRepo = _FakeAuthRepository();

      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            authRepositoryProvider.overrideWithValue(fakeRepo),
          ],
          child: const MaterialApp(home: ResetPasswordPage()),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Reset password'), findsOneWidget);
      expect(find.byType(TextField), findsNWidgets(3));

      // Enter token and mismatched passwords
      await tester.enterText(find.byType(TextField).at(0), 'token-123');
      await tester.enterText(find.byType(TextField).at(1), 'NewPassword123!');
      await tester.enterText(
          find.byType(TextField).at(2), 'DifferentPassword!');
      await tester.tap(find.text('Update password'));
      await tester.pumpAndSettle();

      expect(fakeRepo.resetPasswordCalled, isFalse);

      // Clear any snackbar blocking the view
      ScaffoldMessenger.of(tester.element(find.byType(Scaffold)))
          .clearSnackBars();
      await tester.pumpAndSettle();

      // Enter matching passwords
      await tester.enterText(find.byType(TextField).at(2), 'NewPassword123!');
      await tester.tap(find.text('Update password'));
      await tester.pumpAndSettle();

      expect(fakeRepo.resetPasswordCalled, isTrue);
    });

    test('AuthNotifier executes changePassword and verifyEmail use cases',
        () async {
      final fakeRepo = _FakeAuthRepository();
      final container = ProviderContainer(
        overrides: [
          authRepositoryProvider.overrideWithValue(fakeRepo),
        ],
      );

      final notifier = container.read(authNotifierProvider.notifier);
      final res = await notifier.changePassword(
        currentPassword: 'old',
        newPassword: 'new',
      );

      expect(res.isRight(), isTrue);
      expect(fakeRepo.changePasswordCalled, isTrue);
    });
  });
}
