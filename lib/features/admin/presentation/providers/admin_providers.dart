import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../data/datasources/admin_remote_data_source.dart';
import '../../data/repositories/admin_repository_impl.dart';
import '../../domain/entities/admin_report.dart';
import '../../domain/entities/admin_stats.dart';
import '../../domain/entities/admin_user.dart';
import '../../domain/repositories/admin_repository.dart';

final adminRemoteDataSourceProvider = Provider<AdminRemoteDataSource>((ref) {
  final dio = ref.watch(dioProvider);
  return AdminRemoteDataSourceImpl(dio);
});

final adminRepositoryProvider = Provider<AdminRepository>((ref) {
  final remote = ref.watch(adminRemoteDataSourceProvider);
  return AdminRepositoryImpl(remote);
});

final adminStatsProvider = FutureProvider.autoDispose<AdminStats>((ref) async {
  final repo = ref.watch(adminRepositoryProvider);
  final result = await repo.getStatsOverview();
  return result.fold(
    (l) => const AdminStats(
      totalUsers: 0,
      totalConversations: 0,
      totalReports: 0,
    ),
    (stats) => stats,
  );
});

final adminUsersFilterProvider = StateProvider<String>((ref) => 'ALL');
final adminUsersQueryProvider = StateProvider<String>((ref) => '');

final adminUsersListProvider =
    FutureProvider.autoDispose<List<AdminUser>>((ref) async {
  final repo = ref.watch(adminRepositoryProvider);
  final status = ref.watch(adminUsersFilterProvider);
  final query = ref.watch(adminUsersQueryProvider);

  final result = await repo.getUsers(
    status: status,
    query: query,
  );
  return result.fold((l) => [], (users) => users);
});

final adminReportsFilterProvider = StateProvider<String>((ref) => 'ALL');

final adminReportsListProvider =
    FutureProvider.autoDispose<List<AdminReport>>((ref) async {
  final repo = ref.watch(adminRepositoryProvider);
  final status = ref.watch(adminReportsFilterProvider);

  final result = await repo.getReports(status: status);
  return result.fold((l) => [], (reports) => reports);
});
