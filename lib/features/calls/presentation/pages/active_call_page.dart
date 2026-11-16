import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../domain/entities/call_session.dart';
import '../providers/call_notifier.dart';

class ActiveCallPage extends ConsumerWidget {
  const ActiveCallPage({super.key});

  String _formatDuration(Duration duration) {
    final mins = duration.inMinutes.toString().padLeft(2, '0');
    final secs = (duration.inSeconds % 60).toString().padLeft(2, '0');
    return '$mins:$secs';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final callState = ref.watch(callNotifierProvider);
    final call = callState.activeCall;

    if (call == null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (context.mounted) context.pop();
      });
      return const Scaffold(backgroundColor: Color(0xFF0F172A));
    }

    final isConnected = call.status == CallStatus.connected;
    final isVideo = call.callType == CallType.video;

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: Stack(
          children: [
            // Background / Video Placeholder
            if (isVideo)
              Positioned.fill(
                child: Container(
                  color: const Color(0xFF1E293B),
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.videocam_rounded,
                          size: 64,
                          color: Colors.white.withOpacity(0.2),
                        ),
                        const SizedBox(height: 12),
                        Text(
                          call.isCameraFront
                              ? 'Front camera active'
                              : 'Back camera active',
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.white.withOpacity(0.5),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),

            // Top Status Bar
            Positioned(
              top: 16,
              left: 16,
              right: 16,
              child: Row(
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.black.withOpacity(0.4),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      children: [
                        Icon(
                          isVideo
                              ? Icons.videocam_rounded
                              : Icons.phone_rounded,
                          size: 16,
                          color: Colors.white,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          isVideo ? 'Leo Video' : 'Leo Audio',
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                            color: Colors.white,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const Spacer(),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: isConnected
                          ? const Color(0xFF10B981).withOpacity(0.25)
                          : Colors.black.withOpacity(0.4),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      children: [
                        Container(
                          width: 8,
                          height: 8,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: isConnected
                                ? const Color(0xFF10B981)
                                : Colors.amber,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          isConnected
                              ? _formatDuration(call.connectedDuration)
                              : (call.status == CallStatus.outgoing
                                  ? 'Calling...'
                                  : 'Connecting...'),
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                            color: Colors.white,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            // Center: Avatar & Caller info
            Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      if (isConnected)
                        Container(
                          width: 140,
                          height: 140,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: LeoColors.primary.withOpacity(0.15),
                          ),
                        ),
                      Container(
                        padding: const EdgeInsets.all(4),
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          color: Colors.white,
                        ),
                        child: LeoAvatar(
                          name: call.targetUserName,
                          imageUrl: call.targetAvatarUrl,
                          radius: 54,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  Text(
                    call.targetUserName,
                    style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    isConnected
                        ? AppStrings.callConnected
                        : (call.status == CallStatus.outgoing
                            ? 'Calling...'
                            : AppStrings.callConnecting),
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: Colors.white.withOpacity(0.7),
                    ),
                  ),
                ],
              ),
            ),

            // Bottom Call Controls
            Positioned(
              bottom: 32,
              left: 24,
              right: 24,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  // Mute Button
                  _CallControlButton(
                    icon: call.isMuted
                        ? Icons.mic_off_rounded
                        : Icons.mic_rounded,
                    label: call.isMuted ? 'Unmute' : AppStrings.muteMic,
                    isActive: call.isMuted,
                    onTap: () =>
                        ref.read(callNotifierProvider.notifier).toggleMute(),
                  ),

                  // Speaker Button
                  _CallControlButton(
                    icon: call.isSpeakerOn
                        ? Icons.volume_up_rounded
                        : Icons.volume_down_rounded,
                    label: AppStrings.speaker,
                    isActive: call.isSpeakerOn,
                    onTap: () =>
                        ref.read(callNotifierProvider.notifier).toggleSpeaker(),
                  ),

                  // Flip Camera (for video)
                  if (isVideo)
                    _CallControlButton(
                      icon: Icons.flip_camera_ios_rounded,
                      label: AppStrings.switchCamera,
                      onTap: () =>
                          ref.read(callNotifierProvider.notifier).flipCamera(),
                    ),

                  // End Call Button
                  LeoPressable(
                    onPressed: () {
                      ref.read(callNotifierProvider.notifier).endCall();
                      context.pop();
                    },
                    child: Container(
                      width: 64,
                      height: 64,
                      decoration: const BoxDecoration(
                        color: Color(0xFFEF4444),
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: Color(0x66EF4444),
                            blurRadius: 16,
                            offset: Offset(0, 4),
                          ),
                        ],
                      ),
                      child: const Icon(
                        Icons.call_end_rounded,
                        color: Colors.white,
                        size: 30,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CallControlButton extends StatelessWidget {
  const _CallControlButton({
    required this.icon,
    required this.label,
    required this.onTap,
    this.isActive = false,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        LeoPressable(
          onPressed: onTap,
          child: Container(
            width: 54,
            height: 54,
            decoration: BoxDecoration(
              color: isActive ? Colors.white : Colors.white.withOpacity(0.15),
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              color: isActive ? const Color(0xFF0F172A) : Colors.white,
              size: 24,
            ),
          ),
        ),
        const SizedBox(height: 6),
        Text(
          label,
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            color: Colors.white.withOpacity(0.8),
          ),
        ),
      ],
    );
  }
}
