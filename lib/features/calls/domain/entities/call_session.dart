enum CallType { audio, video }

enum CallStatus { idle, outgoing, incoming, connecting, connected, ended }

enum CallSignalAction {
  offer,
  answer,
  iceCandidate,
  reject,
  busy,
  end,
  delivered,
  unavailable,
}

class CallSession {
  const CallSession({
    required this.callId,
    required this.conversationId,
    required this.targetUserId,
    required this.targetUserName,
    this.targetAvatarUrl,
    required this.callType,
    required this.status,
    required this.isCaller,
    this.isMuted = false,
    this.isSpeakerOn = false,
    this.isCameraFront = true,
    this.connectedDuration = Duration.zero,
  });

  final String callId;
  final String conversationId;
  final String targetUserId;
  final String targetUserName;
  final String? targetAvatarUrl;
  final CallType callType;
  final CallStatus status;
  final bool isCaller;
  final bool isMuted;
  final bool isSpeakerOn;
  final bool isCameraFront;
  final Duration connectedDuration;

  CallSession copyWith({
    String? callId,
    String? conversationId,
    String? targetUserId,
    String? targetUserName,
    String? targetAvatarUrl,
    CallType? callType,
    CallStatus? status,
    bool? isCaller,
    bool? isMuted,
    bool? isSpeakerOn,
    bool? isCameraFront,
    Duration? connectedDuration,
  }) {
    return CallSession(
      callId: callId ?? this.callId,
      conversationId: conversationId ?? this.conversationId,
      targetUserId: targetUserId ?? this.targetUserId,
      targetUserName: targetUserName ?? this.targetUserName,
      targetAvatarUrl: targetAvatarUrl ?? this.targetAvatarUrl,
      callType: callType ?? this.callType,
      status: status ?? this.status,
      isCaller: isCaller ?? this.isCaller,
      isMuted: isMuted ?? this.isMuted,
      isSpeakerOn: isSpeakerOn ?? this.isSpeakerOn,
      isCameraFront: isCameraFront ?? this.isCameraFront,
      connectedDuration: connectedDuration ?? this.connectedDuration,
    );
  }
}
