import '../../domain/entities/user_profile.dart';

class UserProfileDto {
  const UserProfileDto({
    required this.id,
    required this.username,
    this.displayName,
    this.email,
    this.role,
    this.profilePictureUrl,
    this.phoneNumber,
    this.bio,
    this.age,
    this.region,
    this.isEmailVerified = false,
  });

  factory UserProfileDto.fromJson(Map<String, dynamic> json) {
    return UserProfileDto(
      id: (json['id'] ?? '').toString(),
      username: (json['username'] ?? '').toString(),
      displayName: json['displayName']?.toString(),
      email: json['email']?.toString(),
      role: json['role']?.toString(),
      profilePictureUrl: json['profilePictureUrl']?.toString(),
      phoneNumber: json['phoneNumber']?.toString(),
      bio: json['bio']?.toString(),
      age: json['age'] is num ? (json['age'] as num).toInt() : null,
      region: json['region']?.toString(),
      isEmailVerified:
          json['isEmailVerified'] == true || json['emailVerified'] == true,
    );
  }

  final String id;
  final String username;
  final String? displayName;
  final String? email;
  final String? role;
  final String? profilePictureUrl;
  final String? phoneNumber;
  final String? bio;
  final int? age;
  final String? region;
  final bool isEmailVerified;

  UserProfile toDomain() => UserProfile(
        id: id,
        username: username,
        displayName: displayName,
        email: email,
        role: role,
        profilePictureUrl: profilePictureUrl,
        phoneNumber: phoneNumber,
        bio: bio,
        age: age,
        region: region,
        isEmailVerified: isEmailVerified,
      );
}
