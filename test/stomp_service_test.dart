import 'package:flutter_test/flutter_test.dart';

import 'package:leochat/core/network/stomp_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late StompService service;

  setUp(() {
    service =
        StompService(wsUrl: 'wss://leo-chat-backend.onrender.com/ws/websocket');
  });

  tearDown(() {
    service.dispose();
  });

  test('StompService initial status is disconnected', () {
    expect(
        service.connectionStatus.value, RealtimeConnectionStatus.disconnected);
  });

  test('StompService handles subscription and unsubscription lifecycle', () {
    service.subscribeToConversation('conv-123');
    service.unsubscribeFromConversation('conv-123');
    // Ensure no exceptions thrown when not connected
    expect(
        service.connectionStatus.value, RealtimeConnectionStatus.disconnected);
  });

  test('StompService disconnect resets state', () {
    service.disconnect();
    expect(
        service.connectionStatus.value, RealtimeConnectionStatus.disconnected);
  });
}
