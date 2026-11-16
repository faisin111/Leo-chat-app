class AdminUser {
  const AdminUser({
    required this.id,
    required this.username,
    required this.email,
    this.displayName,
    this.role = 'ROLE_USER',
    this.status = 'ACTIVE',
    this.profilePictureUrl,
    this.createdAt,
    this.lastSeenAt,
  });

  final String id;
  final String username;
  final String email;
  final String? displayName;
  final String role;
  final String status; // 'ACTIVE' | 'DISABLED' | 'BANNED'
  final String? profilePictureUrl;
  final DateTime? createdAt;
  final DateTime? lastSeenAt;

  String get effectiveName =>
      (displayName != null && displayName!.trim().isNotEmpty)
          ? displayName!.trim()
          : username;

  bool get isActive => status == 'ACTIVE';
  bool get isDisabled => status == 'DISABLED';
  bool get isBanned => status == 'BANNED';
  bool get isAdmin => role == 'ROLE_ADMIN';
}
