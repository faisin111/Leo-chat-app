import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../data/datasources/users_remote_data_source.dart';
import '../../data/repositories/users_repository_impl.dart';
import '../../domain/entities/user_profile.dart';
import '../../domain/entities/user_search_item.dart';
import '../../domain/entities/user_session_item.dart';
import '../../domain/repositories/users_repository.dart';

final usersRemoteDataSourceProvider = Provider<UsersRemoteDataSource>((ref) {
  final dio = ref.watch(dioProvider);
  return UsersRemoteDataSourceImpl(dio);
});

final usersRepositoryProvider = Provider<UsersRepository>((ref) {
  final remote = ref.watch(usersRemoteDataSourceProvider);
  return UsersRepositoryImpl(remote);
});

final currentUserProfileProvider =
    FutureProvider.autoDispose<UserProfile?>((ref) async {
  final repo = ref.watch(usersRepositoryProvider);
  final result = await repo.getMe();
  return result.fold((l) => null, (profile) => profile);
});

final userSessionsProvider =
    FutureProvider.autoDispose<List<UserSessionItem>>((ref) async {
  final repo = ref.watch(usersRepositoryProvider);
  final result = await repo.getSessions();
  return result.fold((l) => [], (sessions) => sessions);
});

final userSearchQueryProvider = StateProvider<String>((ref) => '');

final userSearchResultsProvider =
    FutureProvider.autoDispose<List<UserSearchItem>>((ref) async {
  final query = ref.watch(userSearchQueryProvider).trim();
  if (query.isEmpty) return [];
  final repo = ref.watch(usersRepositoryProvider);
  final result = await repo.searchUsers(query: query);
  return result.fold((l) => [], (users) => users);
});
