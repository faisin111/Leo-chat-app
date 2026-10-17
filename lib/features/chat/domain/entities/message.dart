enum MessageSendStatus {
  sending,
  sent,
  failed,
}

class MessageReply {
  const MessageReply({
    required this.id,
    this.senderId,
    this.senderUsername,
    this.senderDisplayName,
    this.content,
    this.deleted = false,
  });

  final String id;
  final String? senderId;
  final String? senderUsername;
  final String? senderDisplayName;
  final String? content;
  final bool deleted;
}

class ReactionUser {
  const ReactionUser({
    required this.id,
    required this.username,
    this.displayName,
  });

  final String id;
  final String username;
  final String? displayName;
}

class MessageReaction {
  const MessageReaction({
    required this.emoji,
    required this.count,
    this.users = const [],
  });

  final String emoji;
  final int count;
  final List<ReactionUser> users;
}

class Message {
  const Message({
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
    this.sendStatus = MessageSendStatus.sent,
    this.isOwn = false,
    this.isRead = false,
  });

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
  final MessageReply? replyTo;
  final List<MessageReaction> reactions;
  final DateTime? createdAt;
  final DateTime? editedAt;
  final DateTime? deletedAt;
  final bool deleted;
  final MessageSendStatus sendStatus;
  final bool isOwn;
  final bool isRead;

  String get effectiveSenderName {
    if (senderDisplayName != null && senderDisplayName!.isNotEmpty) {
      return senderDisplayName!;
    }
    if (senderUsername.isNotEmpty) {
      return senderUsername;
    }
    return 'User';
  }

  Message copyWith({
    String? id,
    String? conversationId,
    String? senderId,
    String? senderUsername,
    String? senderDisplayName,
    int? seq,
    String? clientMessageId,
    String? type,
    String? content,
    String? replyToId,
    MessageReply? replyTo,
    List<MessageReaction>? reactions,
    DateTime? createdAt,
    DateTime? editedAt,
    DateTime? deletedAt,
    bool? deleted,
    MessageSendStatus? sendStatus,
    bool? isOwn,
    bool? isRead,
  }) {
    return Message(
      id: id ?? this.id,
      conversationId: conversationId ?? this.conversationId,
      senderId: senderId ?? this.senderId,
      senderUsername: senderUsername ?? this.senderUsername,
      senderDisplayName: senderDisplayName ?? this.senderDisplayName,
      seq: seq ?? this.seq,
      clientMessageId: clientMessageId ?? this.clientMessageId,
      type: type ?? this.type,
      content: content ?? this.content,
      replyToId: replyToId ?? this.replyToId,
      replyTo: replyTo ?? this.replyTo,
      reactions: reactions ?? this.reactions,
      createdAt: createdAt ?? this.createdAt,
      editedAt: editedAt ?? this.editedAt,
      deletedAt: deletedAt ?? this.deletedAt,
      deleted: deleted ?? this.deleted,
      sendStatus: sendStatus ?? this.sendStatus,
      isOwn: isOwn ?? this.isOwn,
      isRead: isRead ?? this.isRead,
    );
  }
}
