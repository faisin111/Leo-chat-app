class UserSearchItem {
  const UserSearchItem({
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

  final String id;
  final String username;
  final String? displayName;
  final String? profilePictureUrl;
  final String? bio;
  final String? region;
  final int? age;
  final String status;
  final DateTime? lastSeenAt;

  String get effectiveName =>
      (displayName != null && displayName!.trim().isNotEmpty)
          ? displayName!.trim()
          : username;
}
