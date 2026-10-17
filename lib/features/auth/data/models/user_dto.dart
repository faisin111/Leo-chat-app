import '../../domain/entities/user.dart';

class UserDto {
  const UserDto({
    required this.id,
    required this.username,
    required this.email,
    required this.displayName,
    this.avatarUrl,
    this.role = 'USER',
    this.status = 'ACTIVE',
  });

  factory UserDto.fromJson(Map<String, dynamic> json) => UserDto(
        id: json['id']?.toString() ?? '',
        username: json['username']?.toString() ?? '',
        email: json['email']?.toString() ?? '',
        displayName: json['displayName']?.toString() ??
            json['username']?.toString() ??
            '',
        avatarUrl: json['avatarUrl']?.toString() ?? json['avatar']?.toString(),
        role: json['role']?.toString() ?? 'USER',
        status: json['status']?.toString() ?? 'ACTIVE',
      );

  final String id;
  final String username;
  final String email;
  final String displayName;
  final String? avatarUrl;
  final String role;
  final String status;

  Map<String, dynamic> toJson() => {
        'id': id,
        'username': username,
        'email': email,
        'displayName': displayName,
        if (avatarUrl != null) 'avatarUrl': avatarUrl,
        'role': role,
        'status': status,
      };

  User toDomain() => User(
        id: id,
        username: username,
        email: email,
        displayName: displayName,
        avatarUrl: avatarUrl,
        role: role,
        status: status,
      );

  factory UserDto.fromDomain(User user) => UserDto(
        id: user.id,
        username: user.username,
        email: user.email,
        displayName: user.displayName,
        avatarUrl: user.avatarUrl,
        role: user.role,
        status: user.status,
      );
}
