import '../../domain/entities/ai_message.dart';
import '../../domain/entities/ai_model.dart';

class AiState {
  const AiState({
    this.selectedModel = AiModelInfo.defaultModel,
    this.messages = const [],
    this.isGenerating = false,
    this.conversationId,
    this.errorMessage,
    this.isDeepThinkEnabled = false,
    this.isWebSearchEnabled = true,
    this.pendingAction,
  });

  final AiModelInfo selectedModel;
  final List<AiMessage> messages;
  final bool isGenerating;
  final String? conversationId;
  final String? errorMessage;
  final bool isDeepThinkEnabled;
  final bool isWebSearchEnabled;
  final Map<String, dynamic>? pendingAction;

  AiState copyWith({
    AiModelInfo? selectedModel,
    List<AiMessage>? messages,
    bool? isGenerating,
    String? conversationId,
    String? errorMessage,
    bool? isDeepThinkEnabled,
    bool? isWebSearchEnabled,
    Map<String, dynamic>? pendingAction,
    bool clearError = false,
    bool clearPendingAction = false,
  }) {
    return AiState(
      selectedModel: selectedModel ?? this.selectedModel,
      messages: messages ?? this.messages,
      isGenerating: isGenerating ?? this.isGenerating,
      conversationId: conversationId ?? this.conversationId,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
      isDeepThinkEnabled: isDeepThinkEnabled ?? this.isDeepThinkEnabled,
      isWebSearchEnabled: isWebSearchEnabled ?? this.isWebSearchEnabled,
      pendingAction:
          clearPendingAction ? null : (pendingAction ?? this.pendingAction),
    );
  }
}
