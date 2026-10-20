import '../../domain/entities/message.dart';

class MessageReplyDto {
  const MessageReplyDto({
    required this.id,
    this.senderId,
    this.senderUsername,
    this.senderDisplayName,
    this.content,
    this.deleted = false,
  });

  factory MessageReplyDto.fromJson(Map<String, dynamic> json) {
    return MessageReplyDto(
      id: json['id'] as String? ?? '',
      senderId: json['senderId'] as String?,
      senderUsername: json['senderUsername'] as String?,
      senderDisplayName: json['senderDisplayName'] as String?,
      content: json['content'] as String?,
      deleted: json['deleted'] as bool? ?? false,
    );
  }

  final String id;
  final String? senderId;
  final String? senderUsername;
  final String? senderDisplayName;
  final String? content;
  final bool deleted;

  Map<String, dynamic> toJson() => {
        'id': id,
        'senderId': senderId,
        'senderUsername': senderUsername,
        'senderDisplayName': senderDisplayName,
        'content': content,
        'deleted': deleted,
      };

  MessageReply toDomain() => MessageReply(
        id: id,
        senderId: senderId,
        senderUsername: senderUsername,
        senderDisplayName: senderDisplayName,
        content: content,
        deleted: deleted,
      );
}

class ReactionUserDto {
  const ReactionUserDto({
    required this.id,
    required this.username,
    this.displayName,
  });

  factory ReactionUserDto.fromJson(Map<String, dynamic> json) {
    return ReactionUserDto(
      id: json['id'] as String? ?? '',
      username: json['username'] as String? ?? '',
      displayName: json['displayName'] as String?,
    );
  }

  final String id;
  final String username;
  final String? displayName;

  Map<String, dynamic> toJson() => {
        'id': id,
        'username': username,
        'displayName': displayName,
      };

  ReactionUser toDomain() => ReactionUser(
        id: id,
        username: username,
        displayName: displayName,
      );
}

class MessageReactionDto {
  const MessageReactionDto({
    required this.emoji,
    required this.count,
    this.users = const [],
  });

  factory MessageReactionDto.fromJson(Map<String, dynamic> json) {
    final rawUsers = json['users'] as List<dynamic>? ?? [];
    return MessageReactionDto(
      emoji: json['emoji'] as String? ?? '',
      count: (json['count'] as num?)?.toInt() ?? 0,
      users: rawUsers
          .map((u) => ReactionUserDto.fromJson(u as Map<String, dynamic>))
          .toList(),
    );
  }

  final String emoji;
  final int count;
  final List<ReactionUserDto> users;

  Map<String, dynamic> toJson() => {
        'emoji': emoji,
        'count': count,
        'users': users.map((u) => u.toJson()).toList(),
      };

  MessageReaction toDomain() => MessageReaction(
        emoji: emoji,
        count: count,
        users: users.map((u) => u.toDomain()).toList(),
      );
}

class MessageDto {
  const MessageDto({
    required this.id,
    required this.conversationId,
    this.senderId = '',
    this.senderUsername = '',
    this.senderDisplayName,
    this.seq = 0,
    this.clientMessageId = '',
    this.type = 'TEXT',
    this.content,
    this.replyToId,
    this.replyTo,
    this.reactions = const [],
    this.createdAt,
    this.editedAt,
    this.deletedAt,
    this.deleted = false,
  });

  factory MessageDto.fromJson(Map<String, dynamic> json) {
    final rawReactions = json['reactions'] as List<dynamic>? ?? [];
    return MessageDto(
      id: json['id'] as String? ?? '',
      conversationId: json['conversationId'] as String? ?? '',
      senderId: json['senderId'] as String? ?? '',
      senderUsername: json['senderUsername'] as String? ?? '',
      senderDisplayName: json['senderDisplayName'] as String?,
      seq: (json['seq'] as num?)?.toInt() ?? 0,
      clientMessageId: json['clientMessageId'] as String? ?? '',
      type: json['type'] as String? ?? 'TEXT',
      content: json['content'] as String?,
      replyToId: json['replyToId'] as String?,
      replyTo: json['replyTo'] != null
          ? MessageReplyDto.fromJson(json['replyTo'] as Map<String, dynamic>)
          : null,
      reactions: rawReactions
          .map((r) => MessageReactionDto.fromJson(r as Map<String, dynamic>))
          .toList(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
      editedAt: json['editedAt'] != null
          ? DateTime.tryParse(json['editedAt'] as String)
          : null,
      deletedAt: json['deletedAt'] != null
          ? DateTime.tryParse(json['deletedAt'] as String)
          : null,
      deleted: json['deleted'] as bool? ?? false,
    );
  }

  final String id;
  final String conversationId;
  final String senderId;
  final String senderUsername;
  final String? senderDisplayName;
  final int seq;
  final String clientMessageId;
  final String type;
  final String? content;
  final String? replyToId;
  final MessageReplyDto? replyTo;
  final List<MessageReactionDto> reactions;
  final DateTime? createdAt;
  final DateTime? editedAt;
  final DateTime? deletedAt;
  final bool deleted;

  Map<String, dynamic> toJson() => {
        'id': id,
        'conversationId': conversationId,
        'senderId': senderId,
        'senderUsername': senderUsername,
        'senderDisplayName': senderDisplayName,
        'seq': seq,
        'clientMessageId': clientMessageId,
        'type': type,
        'content': content,
        'replyToId': replyToId,
        'replyTo': replyTo?.toJson(),
        'reactions': reactions.map((r) => r.toJson()).toList(),
        'createdAt': createdAt?.toIso8601String(),
        'editedAt': editedAt?.toIso8601String(),
        'deletedAt': deletedAt?.toIso8601String(),
        'deleted': deleted,
      };

  factory MessageDto.fromDomain(Message m) {
    return MessageDto(
      id: m.id,
      conversationId: m.conversationId,
      senderId: m.senderId,
      senderUsername: m.senderUsername,
      senderDisplayName: m.senderDisplayName,
      seq: m.seq,
      clientMessageId: m.clientMessageId,
      type: m.type,
      content: m.content,
      replyToId: m.replyToId,
      replyTo: m.replyTo != null
          ? MessageReplyDto(
              id: m.replyTo!.id,
              senderId: m.replyTo!.senderId,
              senderUsername: m.replyTo!.senderUsername,
              senderDisplayName: m.replyTo!.senderDisplayName,
              content: m.replyTo!.content,
              deleted: m.replyTo!.deleted,
            )
          : null,
      reactions: m.reactions
          .map((r) => MessageReactionDto(
                emoji: r.emoji,
                count: r.count,
                users: r.users
                    .map((u) => ReactionUserDto(
                          id: u.id,
                          username: u.username,
                          displayName: u.displayName,
                        ))
                    .toList(),
              ))
          .toList(),
      createdAt: m.createdAt,
      editedAt: m.editedAt,
      deletedAt: m.deletedAt,
      deleted: m.deleted,
    );
  }

  Message toDomain({String currentUserId = '', String currentUsername = ''}) {
    final isOwn = (currentUserId.isNotEmpty && senderId == currentUserId) ||
        (currentUsername.isNotEmpty && senderUsername == currentUsername);
    return Message(
      id: id,
      conversationId: conversationId,
      senderId: senderId,
      senderUsername: senderUsername,
      senderDisplayName: senderDisplayName,
      seq: seq,
      clientMessageId: clientMessageId,
      type: type,
      content: content,
      replyToId: replyToId,
      replyTo: replyTo?.toDomain(),
      reactions: reactions.map((r) => r.toDomain()).toList(),
      createdAt: createdAt,
      editedAt: editedAt,
      deletedAt: deletedAt,
      deleted: deleted,
      isOwn: isOwn,
      sendStatus: MessageSendStatus.sent,
    );
  }
}

class MessagePageDto {
  const MessagePageDto({
    required this.items,
    this.hasMore = false,
    this.nextCursor,
  });

  factory MessagePageDto.fromJson(Map<String, dynamic> json) {
    final rawItems = json['items'] as List<dynamic>? ?? [];
    return MessagePageDto(
      items: rawItems
          .map((e) => MessageDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      hasMore: json['hasMore'] as bool? ?? false,
      nextCursor: json['nextCursor'] as String?,
    );
  }

  final List<MessageDto> items;
  final bool hasMore;
  final String? nextCursor;
}
