import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../domain/entities/call_session.dart';
import '../providers/call_notifier.dart';

class IncomingCallBanner extends ConsumerWidget {
  const IncomingCallBanner({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final callState = ref.watch(callNotifierProvider);
    final call = callState.incomingCall;

    if (call == null) return const SizedBox.shrink();

    final isVideo = call.callType == CallType.video;

    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(24),
        boxShadow: const [
          BoxShadow(
            color: Color(0x44000000),
            blurRadius: 20,
            offset: Offset(0, 6),
          ),
        ],
      ),
      child: Row(
        children: [
          LeoAvatar(
            name: call.targetUserName,
            imageUrl: call.targetAvatarUrl,
            radius: 24,
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  call.targetUserName,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 2),
                Row(
                  children: [
                    Icon(
                      isVideo
                          ? Icons.videocam_rounded
                          : Icons.phone_in_talk_rounded,
                      size: 13,
                      color: LeoColors.primary,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      isVideo ? 'Incoming Video Call' : 'Incoming Voice Call',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.white70,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          // Decline Button
          LeoPressable(
            onPressed: () =>
                ref.read(callNotifierProvider.notifier).rejectIncomingCall(),
            child: Container(
              width: 42,
              height: 42,
              decoration: const BoxDecoration(
                color: Color(0xFFEF4444),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.call_end_rounded,
                color: Colors.white,
                size: 20,
              ),
            ),
          ),
          const SizedBox(width: 10),
          // Accept Button
          LeoPressable(
            onPressed: () {
              ref.read(callNotifierProvider.notifier).acceptIncomingCall();
              context.push('/call');
            },
            child: Container(
              width: 42,
              height: 42,
              decoration: const BoxDecoration(
                color: Color(0xFF10B981),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.call_rounded,
                color: Colors.white,
                size: 20,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
