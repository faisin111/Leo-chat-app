import 'user.dart';

class AuthSession {
  const AuthSession({
    required this.accessToken,
    required this.refreshToken,
    required this.user,
    this.tokenType = 'Bearer',
  });

  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final User user;

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AuthSession &&
          runtimeType == other.runtimeType &&
          accessToken == other.accessToken &&
          refreshToken == other.refreshToken &&
          tokenType == other.tokenType &&
          user == other.user;

  @override
  int get hashCode =>
      accessToken.hashCode ^
      refreshToken.hashCode ^
      tokenType.hashCode ^
      user.hashCode;
}
