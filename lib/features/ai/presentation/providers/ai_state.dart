import '../../domain/entities/ai_message.dart';
import '../../domain/entities/ai_model.dart';

class AiState {
  const AiState({
    this.selectedModel = AiModelInfo.defaultModel,
    this.messages = const [],
    this.isGenerating = false,
  });

  final AiModelInfo selectedModel;
  final List<AiMessage> messages;
  final bool isGenerating;

  AiState copyWith({
    AiModelInfo? selectedModel,
    List<AiMessage>? messages,
    bool? isGenerating,
  }) {
    return AiState(
      selectedModel: selectedModel ?? this.selectedModel,
      messages: messages ?? this.messages,
      isGenerating: isGenerating ?? this.isGenerating,
    );
  }
}
