import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:leochat/core/constants/app_strings.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/admin/domain/entities/admin_report.dart';
import 'package:leochat/features/admin/domain/entities/admin_stats.dart';
import 'package:leochat/features/admin/domain/entities/admin_user.dart';
import 'package:leochat/features/admin/domain/repositories/admin_repository.dart';
import 'package:leochat/features/admin/presentation/pages/admin_dashboard_page.dart';
import 'package:leochat/features/admin/presentation/pages/admin_reports_page.dart';
import 'package:leochat/features/admin/presentation/pages/admin_users_page.dart';
import 'package:leochat/features/admin/presentation/providers/admin_providers.dart';

class _FakeAdminRepository implements AdminRepository {
  @override
  Future<Result<AdminStats>> getStatsOverview() async {
    return const Right(AdminStats(
      totalUsers: 1420,
      totalConversations: 850,
      totalReports: 12,
    ));
  }

  @override
  Future<Result<List<AdminUser>>> getUsers({
    String? query,
    String? status,
    int? cursor,
    int limit = 50,
  }) async {
    return const Right([
      AdminUser(
        id: 'u1',
        username: 'alice',
        email: 'alice@leo.chat',
        status: 'ACTIVE',
        role: 'ROLE_USER',
      ),
      AdminUser(
        id: 'u2',
        username: 'spammer',
        email: 'spam@bad.com',
        status: 'BANNED',
        role: 'ROLE_USER',
      ),
    ]);
  }

  @override
  Future<Result<List<AdminReport>>> getReports({
    String? status,
    int? cursor,
  }) async {
    return Right([
      AdminReport(
        id: 'rep-1',
        reporterId: 'u1',
        targetType: 'USER',
        targetId: 'u2',
        reason: 'Harassment and abusive content',
        status: 'OPEN',
        createdAt: DateTime.now(),
      ),
    ]);
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  group('Admin Feature Widget Tests', () {
    testWidgets('AdminDashboardPage displays metric cards and quick actions',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 2400);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(() {
        tester.view.resetPhysicalSize();
        tester.view.resetDevicePixelRatio();
      });

      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            adminRepositoryProvider.overrideWithValue(_FakeAdminRepository()),
          ],
          child: const MaterialApp(
            home: AdminDashboardPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.adminConsole), findsOneWidget);
      expect(find.text('1420'), findsOneWidget);
      expect(find.text('850'), findsOneWidget);
      expect(find.text('12'), findsOneWidget);
      expect(find.text('User Management'), findsOneWidget);
      expect(find.text('Report Moderation'), findsOneWidget);
    });

    testWidgets('AdminUsersPage displays user list items with badges',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            adminRepositoryProvider.overrideWithValue(_FakeAdminRepository()),
          ],
          child: const MaterialApp(
            home: AdminUsersPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.userManagement), findsOneWidget);
      expect(find.text('alice'), findsOneWidget);
      expect(find.text('spammer'), findsOneWidget);
      expect(find.text('ACTIVE'), findsAtLeastNWidgets(1));
      expect(find.text('BANNED'), findsAtLeastNWidgets(1));
    });

    testWidgets('AdminReportsPage displays moderation reports with reasons',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            adminRepositoryProvider.overrideWithValue(_FakeAdminRepository()),
          ],
          child: const MaterialApp(
            home: AdminReportsPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.reportModeration), findsOneWidget);
      expect(find.text('Harassment and abusive content'), findsOneWidget);
      expect(find.text('OPEN'), findsAtLeastNWidgets(1));
    });
  });
}
