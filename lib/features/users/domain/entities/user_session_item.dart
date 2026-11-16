class UserSessionItem {
  const UserSessionItem({
    required this.sessionId,
    required this.createdAt,
    this.lastUsedAt,
    this.expiresAt,
    this.logoutAt,
    this.userAgent,
    this.ipAddress,
    this.deviceName,
    this.platform,
    this.appVersion,
    this.active = true,
  });

  final String sessionId;
  final DateTime createdAt;
  final DateTime? lastUsedAt;
  final DateTime? expiresAt;
  final DateTime? logoutAt;
  final String? userAgent;
  final String? ipAddress;
  final String? deviceName;
  final String? platform;
  final String? appVersion;
  final bool active;

  bool get isCurrentDevice => active && logoutAt == null;
}
