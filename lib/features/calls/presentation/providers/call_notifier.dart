import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/network/stomp_service.dart';
import '../../domain/entities/call_session.dart';
import 'call_state.dart';

class CallNotifier extends StateNotifier<CallState> {
  CallNotifier(this._stompService) : super(const CallState()) {
    _signalSub = _stompService.callSignalStream.listen(_onSignalReceived);
  }

  final StompService _stompService;
  StreamSubscription<Map<String, dynamic>>? _signalSub;
  Timer? _callDurationTimer;

  void _onSignalReceived(Map<String, dynamic> data) {
    final action = data['action']?.toString().toUpperCase();
    final callId = data['callId']?.toString() ?? '';
    final conversationId = data['conversationId']?.toString() ?? '';
    final fromUserId = data['fromUserId']?.toString() ?? '';
    final fromUsername = data['fromUsername']?.toString() ?? 'Caller';

    switch (action) {
      case 'INVITE':
      case 'OFFER':
        final callTypeStr = data['callType']?.toString().toLowerCase();
        final callType =
            callTypeStr == 'video' ? CallType.video : CallType.audio;

        state = state.copyWith(
          incomingCall: CallSession(
            callId: callId,
            conversationId: conversationId,
            targetUserId: fromUserId,
            targetUserName: fromUsername,
            callType: callType,
            status: CallStatus.incoming,
            isCaller: false,
          ),
        );
        break;

      case 'DELIVERED':
        if (state.activeCall != null &&
            state.activeCall!.status == CallStatus.outgoing) {
          state = state.copyWith(
            activeCall: state.activeCall!.copyWith(status: CallStatus.outgoing),
          );
        }
        break;

      case 'UNAVAILABLE':
        _stopTimer();
        state = state.copyWith(
          clearActiveCall: true,
          clearIncomingCall: true,
        );
        break;

      case 'ANSWER':
        if (state.activeCall != null) {
          state = state.copyWith(
            activeCall: state.activeCall!.copyWith(
              status: CallStatus.connected,
            ),
          );
          _startTimer();
        }
        break;

      case 'REJECT':
      case 'BUSY':
      case 'END':
        _stopTimer();
        state = state.copyWith(
          clearActiveCall: true,
          clearIncomingCall: true,
        );
        break;
    }
  }

  void startCall({
    required String conversationId,
    required String targetUserId,
    required String targetUserName,
    String? targetAvatarUrl,
    required CallType callType,
  }) {
    final callId = 'call-${DateTime.now().millisecondsSinceEpoch}';

    final session = CallSession(
      callId: callId,
      conversationId: conversationId,
      targetUserId: targetUserId,
      targetUserName: targetUserName,
      targetAvatarUrl: targetAvatarUrl,
      callType: callType,
      status: CallStatus.outgoing,
      isCaller: true,
    );

    state = state.copyWith(activeCall: session);

    _stompService.sendCallSignal({
      'callId': callId,
      'conversationId': conversationId,
      'targetUserId': targetUserId,
      'action': 'OFFER',
      'callType': callType.name,
    });
  }

  void acceptIncomingCall() {
    final incoming = state.incomingCall;
    if (incoming == null) return;

    final active = incoming.copyWith(status: CallStatus.connected);
    state = state.copyWith(
      activeCall: active,
      clearIncomingCall: true,
    );

    _stompService.sendCallSignal({
      'callId': incoming.callId,
      'conversationId': incoming.conversationId,
      'targetUserId': incoming.targetUserId,
      'action': 'ANSWER',
    });

    _startTimer();
  }

  void rejectIncomingCall() {
    final incoming = state.incomingCall;
    if (incoming == null) return;

    _stompService.sendCallSignal({
      'callId': incoming.callId,
      'conversationId': incoming.conversationId,
      'targetUserId': incoming.targetUserId,
      'action': 'REJECT',
    });

    state = state.copyWith(clearIncomingCall: true);
  }

  void endCall() {
    final active = state.activeCall;
    if (active != null) {
      _stompService.sendCallSignal({
        'callId': active.callId,
        'conversationId': active.conversationId,
        'targetUserId': active.targetUserId,
        'action': 'END',
      });
    }

    _stopTimer();
    state = state.copyWith(
      clearActiveCall: true,
      clearIncomingCall: true,
    );
  }

  void toggleMute() {
    if (state.activeCall == null) return;
    state = state.copyWith(
      activeCall: state.activeCall!.copyWith(
        isMuted: !state.activeCall!.isMuted,
      ),
    );
  }

  void toggleSpeaker() {
    if (state.activeCall == null) return;
    state = state.copyWith(
      activeCall: state.activeCall!.copyWith(
        isSpeakerOn: !state.activeCall!.isSpeakerOn,
      ),
    );
  }

  void flipCamera() {
    if (state.activeCall == null) return;
    state = state.copyWith(
      activeCall: state.activeCall!.copyWith(
        isCameraFront: !state.activeCall!.isCameraFront,
      ),
    );
  }

  void _startTimer() {
    _stopTimer();
    _callDurationTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (state.activeCall != null &&
          state.activeCall!.status == CallStatus.connected) {
        state = state.copyWith(
          activeCall: state.activeCall!.copyWith(
            connectedDuration: Duration(seconds: timer.tick),
          ),
        );
      }
    });
  }

  void _stopTimer() {
    _callDurationTimer?.cancel();
    _callDurationTimer = null;
  }

  @override
  void dispose() {
    _signalSub?.cancel();
    _stopTimer();
    super.dispose();
  }
}

final callNotifierProvider =
    StateNotifierProvider<CallNotifier, CallState>((ref) {
  final stompService = ref.watch(stompServiceProvider);
  return CallNotifier(stompService);
});
