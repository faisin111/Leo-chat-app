class User {
  const User({
    required this.id,
    required this.username,
    required this.email,
    required this.displayName,
    this.avatarUrl,
    this.role = 'USER',
    this.status = 'ACTIVE',
  });

  final String id;
  final String username;
  final String email;
  final String displayName;
  final String? avatarUrl;
  final String role;
  final String status;

  bool get isAdmin =>
      role.toUpperCase() == 'ROLE_ADMIN' || role.toUpperCase() == 'ADMIN';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is User &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          username == other.username &&
          email == other.email &&
          displayName == other.displayName &&
          avatarUrl == other.avatarUrl &&
          role == other.role &&
          status == other.status;

  @override
  int get hashCode =>
      id.hashCode ^
      username.hashCode ^
      email.hashCode ^
      displayName.hashCode ^
      avatarUrl.hashCode ^
      role.hashCode ^
      status.hashCode;
}
