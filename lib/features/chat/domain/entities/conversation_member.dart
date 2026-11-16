class ConversationMember {
  const ConversationMember({
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

  final String conversationId;
  final String userId;
  final String role; // 'OWNER' | 'ADMIN' | 'MEMBER'
  final int lastReadSeq;
  final DateTime? mutedUntil;
  final DateTime? joinedAt;
  final DateTime? leftAt;
  final DateTime? pinnedAt;
  final DateTime? archivedAt;
  final String? username;
  final String? displayName;
  final String? avatarUrl;

  bool get isOwner => role == 'OWNER';
  bool get isAdmin => role == 'ADMIN' || isOwner;

  String get effectiveName =>
      (displayName != null && displayName!.trim().isNotEmpty)
          ? displayName!.trim()
          : (username ?? 'Member');
}
