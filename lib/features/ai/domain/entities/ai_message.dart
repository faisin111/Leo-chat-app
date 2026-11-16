enum AiMessageRole { user, assistant, system }

class AiMessage {
  const AiMessage({
    required this.id,
    required this.role,
    required this.content,
    required this.createdAt,
    this.modelId,
    this.thinkingContent,
    this.thinkingDurationSecs,
    this.isGenerating = false,
  });

  final String id;
  final AiMessageRole role;
  final String content;
  final DateTime createdAt;
  final String? modelId;
  final String? thinkingContent;
  final double? thinkingDurationSecs;
  final bool isGenerating;

  bool get isUser => role == AiMessageRole.user;
  bool get hasThinking =>
      thinkingContent != null && thinkingContent!.trim().isNotEmpty;
}
