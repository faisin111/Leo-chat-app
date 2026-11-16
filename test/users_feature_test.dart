import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/users/data/datasources/users_remote_data_source.dart';
import 'package:leochat/features/users/data/models/user_profile_dto.dart';
import 'package:leochat/features/users/data/models/user_search_item_dto.dart';
import 'package:leochat/features/users/data/models/user_session_item_dto.dart';
import 'package:leochat/features/users/data/repositories/users_repository_impl.dart';

class _FakeUsersRemoteDataSource implements UsersRemoteDataSource {
  @override
  Future<UserProfileDto> getMe() async => const UserProfileDto(
        id: 'u-1',
        username: 'alex',
        displayName: 'Alex Morgan',
        email: 'alex@example.com',
      );

  @override
  Future<UserProfileDto> getUserById(String id) async => UserProfileDto(
        id: id,
        username: 'user_$id',
      );

  @override
  Future<List<UserSearchItemDto>> searchUsers({
    required String query,
    int page = 0,
    int size = 20,
  }) async {
    return [
      const UserSearchItemDto(
        id: 'u-search-1',
        username: 'alice',
        displayName: 'Alice Smith',
      ),
      const UserSearchItemDto(
        id: 'u-search-2',
        username: 'bob',
        displayName: 'Bob Jones',
      ),
    ];
  }

  @override
  Future<void> updateProfile({
    String? displayName,
    String? profilePictureUrl,
    String? phoneNumber,
    String? bio,
    int? age,
    String? region,
  }) async {}

  @override
  Future<List<UserSessionItemDto>> getSessions() async {
    return [
      UserSessionItemDto(
        sessionId: 's-1',
        createdAt: DateTime.now(),
        deviceName: 'iPhone 15 Pro',
        platform: 'ios',
        active: true,
      ),
    ];
  }

  @override
  Future<void> revokeSession(String sessionId) async {}

  @override
  Future<void> deactivateAccount() async {}
}

void main() {
  group('Users Feature Tests', () {
    late UsersRepositoryImpl repo;
    late _FakeUsersRemoteDataSource remote;

    setUp(() {
      remote = _FakeUsersRemoteDataSource();
      repo = UsersRepositoryImpl(remote);
    });

    test('getMe returns UserProfile entity with effectiveName', () async {
      final res = await repo.getMe();
      expect(res.isRight(), isTrue);
      final profile = res.getOrElse((_) => throw Exception());
      expect(profile.id, 'u-1');
      expect(profile.effectiveName, 'Alex Morgan');
    });

    test('searchUsers parses and returns search results list', () async {
      final res = await repo.searchUsers(query: 'al');
      expect(res.isRight(), isTrue);
      final users = res.getOrElse((_) => []);
      expect(users.length, 2);
      expect(users.first.username, 'alice');
      expect(users.first.effectiveName, 'Alice Smith');
    });

    test('getSessions returns session items with device info', () async {
      final res = await repo.getSessions();
      expect(res.isRight(), isTrue);
      final sessions = res.getOrElse((_) => []);
      expect(sessions.length, 1);
      expect(sessions.first.deviceName, 'iPhone 15 Pro');
      expect(sessions.first.isCurrentDevice, isTrue);
    });

    test('updateProfile and revokeSession succeed without error', () async {
      final updateRes = await repo.updateProfile(displayName: 'Alexander');
      expect(updateRes.isRight(), isTrue);

      final revokeRes = await repo.revokeSession('s-1');
      expect(revokeRes.isRight(), isTrue);
    });
  });
}
