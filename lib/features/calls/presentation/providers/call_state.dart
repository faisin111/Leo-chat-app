import '../../domain/entities/call_session.dart';

class CallState {
  const CallState({
    this.activeCall,
    this.incomingCall,
  });

  final CallSession? activeCall;
  final CallSession? incomingCall;

  bool get hasActiveCall => activeCall != null;
  bool get hasIncomingCall => incomingCall != null;

  CallState copyWith({
    CallSession? activeCall,
    CallSession? incomingCall,
    bool clearActiveCall = false,
    bool clearIncomingCall = false,
  }) {
    return CallState(
      activeCall: clearActiveCall ? null : (activeCall ?? this.activeCall),
      incomingCall:
          clearIncomingCall ? null : (incomingCall ?? this.incomingCall),
    );
  }
}
