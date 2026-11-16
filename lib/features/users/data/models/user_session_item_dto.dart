import '../../domain/entities/user_session_item.dart';

class UserSessionItemDto {
  const UserSessionItemDto({
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

  factory UserSessionItemDto.fromJson(Map<String, dynamic> json) {
    return UserSessionItemDto(
      sessionId: (json['sessionId'] ?? json['id'] ?? '').toString(),
      createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? '') ??
          DateTime.now(),
      lastUsedAt: json['lastUsedAt'] != null
          ? DateTime.tryParse(json['lastUsedAt'].toString())
          : null,
      expiresAt: json['expiresAt'] != null
          ? DateTime.tryParse(json['expiresAt'].toString())
          : null,
      logoutAt: json['logoutAt'] != null
          ? DateTime.tryParse(json['logoutAt'].toString())
          : null,
      userAgent: json['userAgent']?.toString(),
      ipAddress: json['ipAddress']?.toString(),
      deviceName: json['deviceName']?.toString(),
      platform: json['platform']?.toString(),
      appVersion: json['appVersion']?.toString(),
      active: json['active'] != false,
    );
  }

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

  UserSessionItem toDomain() => UserSessionItem(
        sessionId: sessionId,
        createdAt: createdAt,
        lastUsedAt: lastUsedAt,
        expiresAt: expiresAt,
        logoutAt: logoutAt,
        userAgent: userAgent,
        ipAddress: ipAddress,
        deviceName: deviceName,
        platform: platform,
        appVersion: appVersion,
        active: active,
      );
}
