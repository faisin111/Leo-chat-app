import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/stomp_service.dart';
import 'package:leochat/features/calls/domain/entities/call_session.dart';
import 'package:leochat/features/calls/presentation/providers/call_notifier.dart';
import 'package:leochat/features/calls/presentation/widgets/incoming_call_dialog.dart';

class _FakeStompService implements StompService {
  final _callSignalController =
      StreamController<Map<String, dynamic>>.broadcast();

  @override
  Stream<Map<String, dynamic>> get callSignalStream =>
      _callSignalController.stream;

  Map<String, dynamic>? lastSentSignal;

  @override
  bool sendCallSignal(Map<String, dynamic> payload) {
    lastSentSignal = payload;
    return true;
  }

  void emitSignal(Map<String, dynamic> payload) {
    _callSignalController.add(payload);
  }

  @override
  dynamic noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  group('CallNotifier Unit Tests', () {
    late _FakeStompService fakeStompService;
    late CallNotifier notifier;

    setUp(() {
      fakeStompService = _FakeStompService();
      notifier = CallNotifier(fakeStompService);
    });

    tearDown(() {
      notifier.dispose();
    });

    test('startCall initiates outgoing session and sends OFFER signal', () {
      notifier.startCall(
        conversationId: 'c1',
        targetUserId: 'u2',
        targetUserName: 'Alice',
        callType: CallType.video,
      );

      expect(notifier.state.hasActiveCall, isTrue);
      expect(notifier.state.activeCall?.status, equals(CallStatus.outgoing));
      expect(notifier.state.activeCall?.targetUserName, equals('Alice'));
      expect(fakeStompService.lastSentSignal, isNotNull);
      expect(fakeStompService.lastSentSignal?['action'], equals('OFFER'));
      expect(fakeStompService.lastSentSignal?['callType'], equals('video'));
    });

    test('receiving OFFER signal sets incomingCall state', () async {
      fakeStompService.emitSignal({
        'action': 'OFFER',
        'callId': 'incoming-123',
        'conversationId': 'c1',
        'fromUserId': 'u2',
        'fromUsername': 'Bob',
        'callType': 'audio',
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(notifier.state.hasIncomingCall, isTrue);
      expect(notifier.state.incomingCall?.callId, equals('incoming-123'));
      expect(notifier.state.incomingCall?.targetUserName, equals('Bob'));
      expect(notifier.state.incomingCall?.callType, equals(CallType.audio));
    });

    test('receiving INVITE signal sets incomingCall state', () async {
      fakeStompService.emitSignal({
        'action': 'INVITE',
        'callId': 'incoming-invite-1',
        'conversationId': 'c1',
        'fromUserId': 'u2',
        'fromUsername': 'Bob',
        'callType': 'video',
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(notifier.state.hasIncomingCall, isTrue);
      expect(notifier.state.incomingCall?.callId, equals('incoming-invite-1'));
      expect(notifier.state.incomingCall?.targetUserName, equals('Bob'));
      expect(notifier.state.incomingCall?.callType, equals(CallType.video));
    });

    test('receiving UNAVAILABLE signal clears active call', () async {
      notifier.startCall(
        conversationId: 'c1',
        targetUserId: 'u2',
        targetUserName: 'Alice',
        callType: CallType.audio,
      );
      expect(notifier.state.hasActiveCall, isTrue);

      fakeStompService.emitSignal({
        'action': 'UNAVAILABLE',
        'callId': notifier.state.activeCall!.callId,
        'conversationId': 'c1',
        'fromUserId': 'u2',
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(notifier.state.hasActiveCall, isFalse);
    });

    test('receiving DELIVERED signal keeps outgoing call active', () async {
      notifier.startCall(
        conversationId: 'c1',
        targetUserId: 'u2',
        targetUserName: 'Alice',
        callType: CallType.audio,
      );
      expect(notifier.state.hasActiveCall, isTrue);

      fakeStompService.emitSignal({
        'action': 'DELIVERED',
        'callId': notifier.state.activeCall!.callId,
        'conversationId': 'c1',
      });

      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(notifier.state.hasActiveCall, isTrue);
      expect(notifier.state.activeCall?.status, equals(CallStatus.outgoing));
    });

    test('rejectIncomingCall clears incomingCall and sends REJECT signal',
        () async {
      fakeStompService.emitSignal({
        'action': 'OFFER',
        'callId': 'incoming-456',
        'conversationId': 'c1',
        'fromUserId': 'u3',
        'fromUsername': 'Charlie',
        'callType': 'video',
      });

      await Future<void>.delayed(const Duration(milliseconds: 20));

      notifier.rejectIncomingCall();

      expect(notifier.state.hasIncomingCall, isFalse);
      expect(fakeStompService.lastSentSignal?['action'], equals('REJECT'));
    });

    test('toggleMute, toggleSpeaker, and flipCamera mutate activeCall state',
        () {
      notifier.startCall(
        conversationId: 'c1',
        targetUserId: 'u2',
        targetUserName: 'Alice',
        callType: CallType.video,
      );

      expect(notifier.state.activeCall?.isMuted, isFalse);
      notifier.toggleMute();
      expect(notifier.state.activeCall?.isMuted, isTrue);

      expect(notifier.state.activeCall?.isSpeakerOn, isFalse);
      notifier.toggleSpeaker();
      expect(notifier.state.activeCall?.isSpeakerOn, isTrue);

      expect(notifier.state.activeCall?.isCameraFront, isTrue);
      notifier.flipCamera();
      expect(notifier.state.activeCall?.isCameraFront, isFalse);
    });
  });

  group('Calls Widget Tests', () {
    testWidgets(
        'IncomingCallBanner renders caller info and accept/decline buttons',
        (tester) async {
      final fakeStomp = _FakeStompService();
      final callNotifier = CallNotifier(fakeStomp);

      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            stompServiceProvider.overrideWithValue(fakeStomp),
            callNotifierProvider.overrideWith((ref) => callNotifier),
          ],
          child: const MaterialApp(
            home: Scaffold(
              body: IncomingCallBanner(),
            ),
          ),
        ),
      );

      // Initially no banner
      expect(find.text('Incoming audio call...'), findsNothing);

      // Trigger incoming call
      fakeStomp.emitSignal({
        'action': 'OFFER',
        'callId': 'call-99',
        'conversationId': 'c99',
        'fromUserId': 'u99',
        'fromUsername': 'Sarah Connor',
        'callType': 'audio',
      });

      await tester.pump(const Duration(milliseconds: 50));

      expect(find.text('Sarah Connor'), findsOneWidget);
      expect(find.text('Incoming Voice Call'), findsOneWidget);
      expect(find.byIcon(Icons.call_end_rounded), findsOneWidget);
      expect(find.byIcon(Icons.call_rounded), findsOneWidget);
    });
  });
}
