class Conversation {
  const Conversation({
    required this.id,
    required this.title,
    this.type = 'DIRECT',
    this.avatarUrl,
    this.directKey,
    this.createdBy,
    this.lastSeq = 0,
    this.lastMessageAt,
    this.createdAt,
    this.status = 'ACTIVE',
    this.targetUsername,
    this.targetDisplayName,
    this.targetAvatarUrl,
    this.unreadCount = 0,
    this.lastMessageSnippet,
    this.isOnline = false,
    this.isTyping = false,
    this.typingUsernames = const [],
    this.isPinned = false,
    this.isMuted = false,
  });

  final String id;
  final String title;
  final String type;
  final String? avatarUrl;
  final String? directKey;
  final String? createdBy;
  final int lastSeq;
  final DateTime? lastMessageAt;
  final DateTime? createdAt;
  final String status;
  final String? targetUsername;
  final String? targetDisplayName;
  final String? targetAvatarUrl;
  final int unreadCount;
  final String? lastMessageSnippet;
  final bool isOnline;
  final bool isTyping;
  final List<String> typingUsernames;
  final bool isPinned;
  final bool isMuted;

  bool get isGroup => type.toUpperCase() == 'GROUP';

  String? get preferredAvatarUrl {
    for (final value in [avatarUrl, targetAvatarUrl]) {
      if (value != null && value.trim().isNotEmpty) return value;
    }
    return null;
  }

  String get effectiveTitle {
    if (isGroup && title.trim().isNotEmpty) return title;
    if (targetDisplayName != null && targetDisplayName!.trim().isNotEmpty) {
      return targetDisplayName!;
    }
    if (targetUsername != null && targetUsername!.trim().isNotEmpty) {
      return targetUsername!;
    }
    if (title.trim().isNotEmpty) return title;
    return 'Conversation';
  }

  Conversation copyWith({
    String? id,
    String? title,
    String? type,
    String? avatarUrl,
    String? directKey,
    String? createdBy,
    int? lastSeq,
    DateTime? lastMessageAt,
    DateTime? createdAt,
    String? status,
    String? targetUsername,
    String? targetDisplayName,
    String? targetAvatarUrl,
    int? unreadCount,
    String? lastMessageSnippet,
    bool? isOnline,
    bool? isTyping,
    List<String>? typingUsernames,
    bool? isPinned,
    bool? isMuted,
  }) {
    return Conversation(
      id: id ?? this.id,
      title: title ?? this.title,
      type: type ?? this.type,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      directKey: directKey ?? this.directKey,
      createdBy: createdBy ?? this.createdBy,
      lastSeq: lastSeq ?? this.lastSeq,
      lastMessageAt: lastMessageAt ?? this.lastMessageAt,
      createdAt: createdAt ?? this.createdAt,
      status: status ?? this.status,
      targetUsername: targetUsername ?? this.targetUsername,
      targetDisplayName: targetDisplayName ?? this.targetDisplayName,
      targetAvatarUrl: targetAvatarUrl ?? this.targetAvatarUrl,
      unreadCount: unreadCount ?? this.unreadCount,
      lastMessageSnippet: lastMessageSnippet ?? this.lastMessageSnippet,
      isOnline: isOnline ?? this.isOnline,
      isTyping: isTyping ?? this.isTyping,
      typingUsernames: typingUsernames ?? this.typingUsernames,
      isPinned: isPinned ?? this.isPinned,
      isMuted: isMuted ?? this.isMuted,
    );
  }
}
