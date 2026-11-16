import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/entities/ai_message.dart';
import '../../domain/entities/ai_model.dart';
import 'ai_state.dart';

class AiNotifier extends StateNotifier<AiState> {
  AiNotifier()
      : super(AiState(
          messages: [
            AiMessage(
              id: 'init-1',
              role: AiMessageRole.user,
              content:
                  'How does LeoChat achieve low-latency real-time communication across platforms?',
              createdAt: DateTime.now().subtract(const Duration(minutes: 10)),
            ),
            AiMessage(
              id: 'init-2',
              role: AiMessageRole.assistant,
              modelId: 'leo-ultra',
              thinkingContent:
                  '1. Analyze protocol choices: STOMP 1.2 over WebSocket vs. HTTP long polling.\n2. Evaluate caching strategy: local SQLite/SharedPreferences cache-first reads with background sequence reconciliation.\n3. Account for WebRTC audio/video signaling queues for peer-to-peer media streaming.\n4. Highlight sequence synchronization (seq numbers) to resolve message order deterministically.',
              thinkingDurationSecs: 2.1,
              content:
                  '**LeoChat** leverages a dual-engine architecture designed for sub-50ms message propagation and offline durability:\n\n'
                  '### 1. Persistent STOMP 1.2 WebSocket Transport\n'
                  '- Dedicated duplex connection handles `/app/chat.send`, `/app/chat.typing`, and `/app/call.signal`.\n'
                  '- Low-overhead frame encoding keeps bandwidth minimal on mobile cellular networks.\n\n'
                  '### 2. Sequence-Numbered Deduplication\n'
                  'Every message is assigned an incremental monotonic sequence ID (`seq`) per conversation. Even after reconnecting from an elevator or tunnel, client gap sync queries `/messages?afterSeq=N` to backfill without duplicating or missing items.\n\n'
                  '### 3. Cache-First Reactive UI\n'
                  'Conversations and messages render immediately from local storage, giving 0ms startup time while STOMP connects in the background.',
              createdAt: DateTime.now().subtract(const Duration(minutes: 9)),
            ),
          ],
        ));

  void selectModel(AiModelInfo model) {
    state = state.copyWith(selectedModel: model);
  }

  void clearHistory() {
    state = state.copyWith(messages: []);
  }

  Future<void> sendMessage(String text) async {
    final prompt = text.trim();
    if (prompt.isEmpty || state.isGenerating) return;

    final userMsg = AiMessage(
      id: 'usr-${DateTime.now().millisecondsSinceEpoch}',
      role: AiMessageRole.user,
      content: prompt,
      createdAt: DateTime.now(),
    );

    final updated = List<AiMessage>.from(state.messages)..add(userMsg);
    state = state.copyWith(messages: updated, isGenerating: true);

    await Future<void>.delayed(const Duration(milliseconds: 900));

    final model = state.selectedModel;
    String thinking = '';
    String responseContent = '';
    double duration = 1.8;

    if (model.id == 'leo-reasoner') {
      duration = 3.2;
      thinking =
          '1. Deconstruct core premise: "${prompt.length > 40 ? prompt.substring(0, 40) : prompt}..."\n'
          '2. Formulate axiomatic hypotheses and verify mathematical / logical boundaries.\n'
          '3. Check edge conditions, memory complexity, and deterministic execution guarantees.\n'
          '4. Synthesize step-by-step rigorous deduction.';
      responseContent = '**Logical Breakdown & Deductive Analysis**\n\n'
          '1. **Premise Formulation**: Addressing your request from foundational principles.\n'
          '2. **Invariants**: All state mutations must satisfy transactional ACID properties and clean architectural boundaries.\n'
          '3. **Conclusion**: By isolating domain use-cases from framework dependencies, we ensure deterministic predictability and 100% test coverage.';
    } else if (model.id == 'leo-coder') {
      duration = 2.4;
      thinking =
          '1. Identify target tech stack: Flutter / Dart Clean Architecture.\n'
          '2. Ensure Riverpod state immutability with fpdart Result<T> contracts.\n'
          '3. Structure production-ready code with comprehensive error handling.';
      responseContent =
          'Here is the production implementation following Clean Architecture principles:\n\n'
          '```dart\n'
          'abstract interface class SolutionRepository {\n'
          '  Future<Result<Item>> executeTask(String id);\n'
          '}\n\n'
          'class SolutionController extends StateNotifier<AsyncValue<Item>> {\n'
          '  SolutionController(this._repo) : super(const AsyncValue.loading());\n'
          '  final SolutionRepository _repo;\n\n'
          '  Future<void> run(String id) async {\n'
          '    final res = await _repo.executeTask(id);\n'
          '    res.fold(\n'
          '      (failure) => state = AsyncValue.error(failure.message, StackTrace.current),\n'
          '      (item) => state = AsyncValue.data(item),\n'
          '    );\n'
          '  }\n'
          '}\n'
          '```\n\n'
          'This guarantees complete presentation isolation from networking and database layers.';
    } else {
      duration = 1.5;
      thinking =
          '1. Synthesize user intent regarding: "${prompt.length > 50 ? prompt.substring(0, 50) : prompt}"\n'
          '2. Match against LeoChat design tokens, STOMP networking, and real-time state.\n'
          '3. Produce clear, actionable, high-impact guidance.';
      responseContent = 'I have analyzed your request:\n\n'
          '> "$prompt"\n\n'
          'Here is the recommended approach:\n\n'
          '• **Architecture & Performance**: Use cache-first local synchronization with Riverpod notifiers to eliminate jank.\n'
          '• **Security**: Guard API access via Bearer JWT with single-flight automatic refresh interceptors.\n'
          '• **User Experience**: Apply tactile press animations and responsive multi-platform layout adaptations.';
    }

    final assistantMsg = AiMessage(
      id: 'ai-${DateTime.now().millisecondsSinceEpoch}',
      role: AiMessageRole.assistant,
      modelId: model.id,
      thinkingContent: thinking,
      thinkingDurationSecs: duration,
      content: responseContent,
      createdAt: DateTime.now(),
    );

    final finalMessages = List<AiMessage>.from(state.messages)
      ..add(assistantMsg);
    state = state.copyWith(messages: finalMessages, isGenerating: false);
  }
}

final aiNotifierProvider =
    StateNotifierProvider<AiNotifier, AiState>((ref) => AiNotifier());
