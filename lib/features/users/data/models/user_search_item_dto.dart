import '../../domain/entities/user_search_item.dart';

class UserSearchItemDto {
  const UserSearchItemDto({
    required this.id,
    required this.username,
    this.displayName,
    this.profilePictureUrl,
    this.bio,
    this.region,
    this.age,
    this.status = 'ACTIVE',
    this.lastSeenAt,
  });

  factory UserSearchItemDto.fromJson(Map<String, dynamic> json) {
    DateTime? parsedLastSeen;
    final rawDate = json['lastSeenAt']?.toString();
    if (rawDate != null && rawDate.isNotEmpty) {
      parsedLastSeen = DateTime.tryParse(rawDate);
    }

    return UserSearchItemDto(
      id: (json['id'] ?? '').toString(),
      username: (json['username'] ?? '').toString(),
      displayName: json['displayName']?.toString(),
      profilePictureUrl: json['profilePictureUrl']?.toString(),
      bio: json['bio']?.toString(),
      region: json['region']?.toString(),
      age: json['age'] is num ? (json['age'] as num).toInt() : null,
      status: (json['status'] ?? 'ACTIVE').toString(),
      lastSeenAt: parsedLastSeen,
    );
  }

  final String id;
  final String username;
  final String? displayName;
  final String? profilePictureUrl;
  final String? bio;
  final String? region;
  final int? age;
  final String status;
  final DateTime? lastSeenAt;

  UserSearchItem toDomain() => UserSearchItem(
        id: id,
        username: username,
        displayName: displayName,
        profilePictureUrl: profilePictureUrl,
        bio: bio,
        region: region,
        age: age,
        status: status,
        lastSeenAt: lastSeenAt,
      );
}
