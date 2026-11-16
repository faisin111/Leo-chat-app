class SendMessageRequestDto {
  const SendMessageRequestDto({
    required this.conversationId,
    required this.clientMessageId,
    required this.content,
    this.type = 'TEXT',
    this.replyToId,
    this.storageKey,
    this.fileName,
    this.mimeType,
    this.sizeBytes,
  });

  final String conversationId;
  final String clientMessageId;
  final String content;
  final String type;
  final String? replyToId;
  final String? storageKey;
  final String? fileName;
  final String? mimeType;
  final int? sizeBytes;

  Map<String, dynamic> toJson() => {
        'conversationId': conversationId,
        'clientMessageId': clientMessageId,
        'content': content,
        'type': type,
        if (replyToId != null) 'replyToId': replyToId,
        if (storageKey != null) 'storageKey': storageKey,
        if (fileName != null) 'fileName': fileName,
        if (mimeType != null) 'mimeType': mimeType,
        if (sizeBytes != null) 'sizeBytes': sizeBytes,
      };
}
