import '../../domain/entities/auth_session.dart';
import 'user_dto.dart';

class AuthResponseDto {
  const AuthResponseDto({
    required this.accessToken,
    required this.refreshToken,
    this.tokenType = 'Bearer',
    required this.user,
  });

  factory AuthResponseDto.fromJson(Map<String, dynamic> json) {
    final accessToken = json['accessToken']?.toString() ??
        json['token']?.toString() ??
        json['jwt']?.toString() ??
        '';
    final refreshToken = json['refreshToken']?.toString() ?? '';
    final tokenType = json['tokenType']?.toString() ?? 'Bearer';

    final userJson = json['user'];
    final user = userJson is Map<String, dynamic>
        ? UserDto.fromJson(userJson)
        : UserDto.fromJson(json);

    return AuthResponseDto(
      accessToken: accessToken,
      refreshToken: refreshToken,
      tokenType: tokenType,
      user: user,
    );
  }

  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final UserDto user;

  Map<String, dynamic> toJson() => {
        'accessToken': accessToken,
        'refreshToken': refreshToken,
        'tokenType': tokenType,
        'user': user.toJson(),
      };

  AuthSession toDomain() => AuthSession(
        accessToken: accessToken,
        refreshToken: refreshToken,
        tokenType: tokenType,
        user: user.toDomain(),
      );
}
