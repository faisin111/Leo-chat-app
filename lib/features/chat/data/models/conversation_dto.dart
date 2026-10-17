import '../../domain/entities/conversation.dart';

class ConversationDto {
  const ConversationDto({
    required this.id,
    this.type = 'DIRECT',
    this.title = '',
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
  });

  factory ConversationDto.fromJson(Map<String, dynamic> json) {
    final nestedTarget = _firstMap([
      json['targetUser'],
      json['target'],
      json['otherUser'],
      json['participant'],
      json['user'],
      json['contact'],
      json['recipient'],
    ]);
    final nestedMember = _firstMapFromList(
      json['members'] ?? json['participants'] ?? json['users'],
    );

    String? value(List<dynamic> values) {
      for (final item in values) {
        if (item is String && item.trim().isNotEmpty) return item.trim();
      }
      return null;
    }

    final target = nestedTarget ?? nestedMember;
    return ConversationDto(
      id: json['id']?.toString() ?? '',
      type: json['type']?.toString() ?? 'DIRECT',
      title: value([
            json['title'],
            json['name'],
            target?['displayName'],
            target?['name'],
            target?['username'],
          ]) ??
          '',
      avatarUrl: value([
        json['avatarUrl'],
        json['avatar'],
        json['imageUrl'],
        json['profileImageUrl'],
        json['profileImage'],
      ]),
      directKey: json['directKey']?.toString(),
      createdBy: json['createdBy']?.toString(),
      lastSeq: (json['lastSeq'] as num?)?.toInt() ?? 0,
      lastMessageAt: json['lastMessageAt'] != null
          ? DateTime.tryParse(json['lastMessageAt'] as String)
          : null,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
      status: json['status'] as String? ?? 'ACTIVE',
      targetUsername: value([
        json['targetUsername'],
        json['username'],
        target?['username'],
        target?['userName'],
      ]),
      targetDisplayName: value([
        json['targetDisplayName'],
        json['displayName'],
        target?['displayName'],
        target?['name'],
      ]),
      targetAvatarUrl: value([
        json['targetAvatarUrl'],
        target?['avatarUrl'],
        target?['avatar'],
        target?['profileImageUrl'],
        target?['profileImage'],
        target?['imageUrl'],
        target?['photoUrl'],
      ]),
      unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
    );
  }

  static Map<String, dynamic>? _firstMap(Iterable<dynamic> values) {
    for (final value in values) {
      if (value is Map) return Map<String, dynamic>.from(value);
    }
    return null;
  }

  static Map<String, dynamic>? _firstMapFromList(dynamic value) {
    if (value is! List) return null;
    for (final item in value) {
      if (item is Map) return Map<String, dynamic>.from(item);
    }
    return null;
  }

  final String id;
  final String type;
  final String title;
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

  Map<String, dynamic> toJson() => {
        'id': id,
        'type': type,
        'title': title,
        'avatarUrl': avatarUrl,
        'directKey': directKey,
        'createdBy': createdBy,
        'lastSeq': lastSeq,
        'lastMessageAt': lastMessageAt?.toIso8601String(),
        'createdAt': createdAt?.toIso8601String(),
        'status': status,
        'targetUsername': targetUsername,
        'targetDisplayName': targetDisplayName,
        'targetAvatarUrl': targetAvatarUrl,
        'unreadCount': unreadCount,
      };

  Conversation toDomain() {
    return Conversation(
      id: id,
      title: title,
      type: type,
      avatarUrl: avatarUrl,
      directKey: directKey,
      createdBy: createdBy,
      lastSeq: lastSeq,
      lastMessageAt: lastMessageAt,
      createdAt: createdAt,
      status: status,
      targetUsername: targetUsername,
      targetDisplayName: targetDisplayName,
      targetAvatarUrl: targetAvatarUrl,
      unreadCount: unreadCount,
    );
  }
}

class ConversationPageDto {
  const ConversationPageDto({
    required this.items,
    this.hasMore = false,
    this.nextCursor,
  });

  factory ConversationPageDto.fromJson(Map<String, dynamic> json) {
    final rawItems =
        (json['items'] ?? json['content'] ?? json['data']) as List<dynamic>? ??
            [];
    return ConversationPageDto(
      items: rawItems
          .map((e) => ConversationDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      hasMore: json['hasMore'] as bool? ?? false,
      nextCursor: json['nextCursor'] as String?,
    );
  }

  final List<ConversationDto> items;
  final bool hasMore;
  final String? nextCursor;
}
