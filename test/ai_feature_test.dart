import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/ai/domain/entities/ai_message.dart';
import 'package:leochat/features/ai/domain/entities/ai_model.dart';
import 'package:leochat/features/ai/presentation/pages/ai_page.dart';
import 'package:leochat/features/ai/presentation/providers/ai_notifier.dart';

void main() {
  group('AiNotifier Unit Tests', () {
    test('initial state contains default model and empty messages', () {
      final notifier = AiNotifier();
      expect(notifier.state.selectedModel.id, equals('leo-ultra'));
      expect(notifier.state.messages, isEmpty);
      expect(notifier.state.isGenerating, isFalse);
    });

    test('selectModel updates selectedModel in state', () {
      final notifier = AiNotifier();
      final reasoner = AiModelInfo.models.firstWhere(
        (m) => m.id == 'leo-reasoner',
      );
      notifier.selectModel(reasoner);
      expect(notifier.state.selectedModel.id, equals('leo-reasoner'));
      expect(notifier.state.selectedModel.name, equals('Leo Reasoning R1'));
    });

    test('sendMessage adds user and assistant response in standalone mode',
        () async {
      final notifier = AiNotifier();
      await notifier.sendMessage('Hello Leo AI');
      expect(notifier.state.messages.length, equals(2));
      expect(notifier.state.messages.first.content, equals('Hello Leo AI'));
      expect(notifier.state.messages.last.role, equals(AiMessageRole.assistant));
    });

    test('clearHistory empties the messages list', () async {
      final notifier = AiNotifier();
      await notifier.sendMessage('Test');
      expect(notifier.state.messages, isNotEmpty);
      await notifier.clearHistory();
      expect(notifier.state.messages, isEmpty);
    });

    test('toggleDeepThink and toggleWebSearch update flags', () {
      final notifier = AiNotifier();
      expect(notifier.state.isDeepThinkEnabled, isFalse);
      notifier.toggleDeepThink();
      expect(notifier.state.isDeepThinkEnabled, isTrue);

      expect(notifier.state.isWebSearchEnabled, isTrue);
      notifier.toggleWebSearch();
      expect(notifier.state.isWebSearchEnabled, isFalse);
    });
  });

  group('AiPage Widget Tests', () {
    testWidgets('renders Leo AI title, model button, and composer',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            aiNotifierProvider.overrideWith((ref) => AiNotifier()),
          ],
          child: const MaterialApp(
            home: AiPage(),
          ),
        ),
      );
      await tester.pump();

      expect(find.text('LeoChat 4.5 Ultra'), findsOneWidget);
      expect(find.text('Ask Leo AI anything...'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
    });
  });
}
