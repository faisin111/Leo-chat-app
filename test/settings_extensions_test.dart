import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/constants/app_strings.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/settings/presentation/pages/appearance_page.dart';
import 'package:leochat/features/settings/presentation/pages/security_page.dart';
import 'package:leochat/features/settings/presentation/pages/sessions_page.dart';
import 'package:leochat/features/users/domain/entities/user_session_item.dart';
import 'package:leochat/features/users/domain/repositories/users_repository.dart';
import 'package:leochat/features/users/presentation/providers/users_providers.dart';

class _FakeUsersRepository implements UsersRepository {
  @override
  Future<Result<List<UserSessionItem>>> getSessions() async {
    return Right([
      UserSessionItem(
        sessionId: 'sess-1',
        deviceName: 'LeoChat for iPhone',
        ipAddress: '192.168.1.10',
        platform: 'iOS',
        active: true,
        createdAt: DateTime.now(),
        lastUsedAt: DateTime.now(),
      ),
      UserSessionItem(
        sessionId: 'sess-2',
        deviceName: 'Chrome on macOS',
        ipAddress: '10.0.0.1',
        platform: 'macOS',
        active: true,
        createdAt: DateTime.now().subtract(const Duration(hours: 3)),
        lastUsedAt: DateTime.now().subtract(const Duration(hours: 3)),
      ),
    ]);
  }

  @override
  Future<Result<void>> revokeSession(String sessionId) async {
    return const Right(null);
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  group('Settings Extensions Widget Tests', () {
    testWidgets('SessionsPage displays active sessions and device info',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            usersRepositoryProvider.overrideWithValue(_FakeUsersRepository()),
          ],
          child: const MaterialApp(
            home: SessionsPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.sessionsAndDevices), findsOneWidget);
      expect(find.text('LeoChat for iPhone'), findsOneWidget);
      expect(find.text('THIS DEVICE'), findsOneWidget);
      expect(find.text('Chrome on macOS'), findsOneWidget);
      expect(find.text('Revoke'), findsOneWidget);
    });

    testWidgets(
        'SecurityPage renders change password fields and action buttons',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 2400);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(() {
        tester.view.resetPhysicalSize();
        tester.view.resetDevicePixelRatio();
      });

      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: SecurityPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.security), findsOneWidget);
      expect(find.text('Change Password'), findsOneWidget);
      expect(find.text(AppStrings.currentPassword), findsOneWidget);
      expect(find.text(AppStrings.newPassword), findsOneWidget);
      expect(find.text(AppStrings.confirmPassword), findsOneWidget);
      expect(find.text(AppStrings.updatePassword), findsOneWidget);
      expect(find.text('Deactivate Account'), findsOneWidget);
    });

    testWidgets('AppearancePage renders theme selector and accent colors',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: AppearancePage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.appearance), findsOneWidget);
      expect(find.text('THEME MODE'), findsOneWidget);
      expect(find.text('Light'), findsOneWidget);
      expect(find.text('Dark'), findsOneWidget);
      expect(find.text('System'), findsOneWidget);
      expect(find.text('ACCENT COLOR'), findsOneWidget);
      expect(find.text('LIVE PREVIEW'), findsOneWidget);
    });
  });
}
