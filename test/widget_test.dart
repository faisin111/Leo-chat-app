import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fpdart/fpdart.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:leochat/app/app.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/auth/domain/entities/auth_session.dart';
import 'package:leochat/features/auth/domain/entities/user.dart';
import 'package:leochat/features/auth/domain/repositories/auth_repository.dart';

class _FakeAuthRepository implements AuthRepository {
  @override
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async =>
      const Right(AuthSession(
        accessToken: 'test_token',
        refreshToken: 'test_refresh',
        user: User(
          id: '1',
          username: 'alexmorgan',
          email: 'alex@example.com',
          displayName: 'Alex Morgan',
        ),
      ));

  @override
  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async =>
      const Right(AuthSession(
        accessToken: 'test_token',
        refreshToken: 'test_refresh',
        user: User(
          id: '1',
          username: 'alexmorgan',
          email: 'alex@example.com',
          displayName: 'Alex Morgan',
        ),
      ));

  @override
  Future<Result<void>> logout() async => const Right(null);

  @override
  Future<Result<User?>> getCurrentUser() async => const Right(null);
}

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);

  testWidgets('LeoChat starts with splash then opens onboarding',
      (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LeoChatApp()));
    await tester.pump();

    expect(find.text('LeoChat'), findsOneWidget);
    await tester.pump(const Duration(milliseconds: 1000));
    await tester.pumpAndSettle();

    expect(find.text('Stay close to your people.'), findsOneWidget);
  });

  testWidgets('onboarding skip opens login then chat shell', (tester) async {
    await tester.pumpWidget(ProviderScope(
      overrides: [
        authRepositoryProvider.overrideWithValue(_FakeAuthRepository()),
      ],
      child: const LeoChatApp(),
    ));
    await tester.pump(const Duration(milliseconds: 1000));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Skip'));
    await tester.pumpAndSettle();

    expect(find.text('Username'), findsOneWidget);
    await tester.enterText(find.byType(TextField).at(0), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(1), 'password123');
    await tester.ensureVisible(find.text('Sign in'));
    await tester.tap(find.text('Sign in'));
    await tester.pumpAndSettle();

    expect(find.text('Your chats'), findsOneWidget);
  });
}
