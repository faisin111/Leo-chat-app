class UserProfile {
  const UserProfile({
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

  String get effectiveName =>
      (displayName != null && displayName!.trim().isNotEmpty)
          ? displayName!.trim()
          : username;
}
