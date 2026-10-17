class SendMessageRequestDto {
  const SendMessageRequestDto({
    required this.conversationId,
    required this.clientMessageId,
    required this.content,
    this.type = 'TEXT',
    this.replyToId,
  });

  final String conversationId;
  final String clientMessageId;
  final String content;
  final String type;
  final String? replyToId;

  Map<String, dynamic> toJson() => {
        'conversationId': conversationId,
        'clientMessageId': clientMessageId,
        'content': content,
        'type': type,
        if (replyToId != null) 'replyToId': replyToId,
      };
}
