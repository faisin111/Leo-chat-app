import '../../../../core/network/result.dart';
import '../entities/user_profile.dart';
import '../entities/user_search_item.dart';
import '../entities/user_session_item.dart';

abstract interface class UsersRepository {
  Future<Result<UserProfile>> getMe();

  Future<Result<UserProfile>> getUserById(String id);

  Future<Result<List<UserSearchItem>>> searchUsers({
    required String query,
    int page = 0,
    int size = 20,
  });

  Future<Result<void>> updateProfile({
    String? displayName,
    String? profilePictureUrl,
    String? phoneNumber,
    String? bio,
    int? age,
    String? region,
  });

  Future<Result<List<UserSessionItem>>> getSessions();

  Future<Result<void>> revokeSession(String sessionId);

  Future<Result<void>> deactivateAccount();
}
