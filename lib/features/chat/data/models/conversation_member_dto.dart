import '../../domain/entities/conversation_member.dart';

class ConversationMemberDto {
  const ConversationMemberDto({
    required this.conversationId,
    required this.userId,
    required this.role,
    this.lastReadSeq = 0,
    this.mutedUntil,
    this.joinedAt,
    this.leftAt,
    this.pinnedAt,
    this.archivedAt,
    this.username,
    this.displayName,
    this.avatarUrl,
  });

  factory ConversationMemberDto.fromJson(Map<String, dynamic> json) {
    return ConversationMemberDto(
      conversationId: (json['conversationId'] ?? '').toString(),
      userId: (json['userId'] ?? json['id'] ?? '').toString(),
      role: (json['role'] ?? 'MEMBER').toString(),
      lastReadSeq: (json['lastReadSeq'] as num?)?.toInt() ?? 0,
      mutedUntil: json['mutedUntil'] != null
          ? DateTime.tryParse(json['mutedUntil'].toString())
          : null,
      joinedAt: json['joinedAt'] != null
          ? DateTime.tryParse(json['joinedAt'].toString())
          : null,
      leftAt: json['leftAt'] != null
          ? DateTime.tryParse(json['leftAt'].toString())
          : null,
      pinnedAt: json['pinnedAt'] != null
          ? DateTime.tryParse(json['pinnedAt'].toString())
          : null,
      archivedAt: json['archivedAt'] != null
          ? DateTime.tryParse(json['archivedAt'].toString())
          : null,
      username: json['username']?.toString(),
      displayName: json['displayName']?.toString(),
      avatarUrl: (json['avatarUrl'] ?? json['profilePictureUrl'])?.toString(),
    );
  }

  final String conversationId;
  final String userId;
  final String role;
  final int lastReadSeq;
  final DateTime? mutedUntil;
  final DateTime? joinedAt;
  final DateTime? leftAt;
  final DateTime? pinnedAt;
  final DateTime? archivedAt;
  final String? username;
  final String? displayName;
  final String? avatarUrl;

  ConversationMember toDomain() => ConversationMember(
        conversationId: conversationId,
        userId: userId,
        role: role,
        lastReadSeq: lastReadSeq,
        mutedUntil: mutedUntil,
        joinedAt: joinedAt,
        leftAt: leftAt,
        pinnedAt: pinnedAt,
        archivedAt: archivedAt,
        username: username,
        displayName: displayName,
        avatarUrl: avatarUrl,
      );
}
