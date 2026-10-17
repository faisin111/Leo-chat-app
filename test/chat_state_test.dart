import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/chat/presentation/providers/chat_state.dart';

void main() {
  test('clearing typing state removes the indicator text', () {
    const state = ChatState(typingStatusText: 'Noah is typing...');

    final cleared = state.copyWith(clearTypingStatusText: true);

    expect(cleared.typingStatusText, isNull);
  });
}
