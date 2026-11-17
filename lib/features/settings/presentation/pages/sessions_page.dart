import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../users/presentation/providers/users_providers.dart';

class SessionsPage extends ConsumerWidget {
  const SessionsPage({super.key});

  IconData _getDeviceIcon(String? platform, String? userAgent) {
    final p = (platform ?? '').toLowerCase();
    final ua = (userAgent ?? '').toLowerCase();

    if (p.contains('ios') || p.contains('android') || ua.contains('mobile')) {
      return Icons.smartphone_rounded;
    }
    if (ua.contains('ipad') || ua.contains('tablet')) {
      return Icons.tablet_mac_rounded;
    }
    return Icons.laptop_mac_rounded;
  }

  String _formatRelativeTime(DateTime? dt) {
    if (dt == null) return 'Active recently';
    final diff = DateTime.now().difference(dt);
    if (diff.inMinutes < 2) return 'Active just now';
    if (diff.inHours < 1) return 'Active ${diff.inMinutes}m ago';
    if (diff.inDays < 1) return 'Active ${diff.inHours}h ago';
    return 'Active ${diff.inDays}d ago';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessionsAsync = ref.watch(userSessionsProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor:
          isDark ? LeoColors.darkBackground : const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: isDark ? LeoColors.darkSurface : Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        leading: LeoPressable(
          child: IconButton(
            icon: Icon(
              Icons.arrow_back_rounded,
              color: isDark ? LeoColors.darkTextPrimary : LeoColors.textPrimary,
            ),
            onPressed: () => context.pop(),
          ),
        ),
        title: Text(
          AppStrings.sessionsAndDevices,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            color: isDark ? LeoColors.darkTextPrimary : LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Security tip card
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: isDark
                      ? const Color(0xFF451A03).withOpacity(0.4)
                      : const Color(0xFFFEF3C7),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(
                    color: isDark
                        ? const Color(0xFF78350F)
                        : const Color(0xFFFDE68A),
                  ),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(
                      Icons.shield_outlined,
                      color: Color(0xFFD97706),
                      size: 22,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'If you see a device you do not recognize, sign out immediately and update your password in Security settings.',
                        style: TextStyle(
                          fontSize: 13,
                          color: isDark
                              ? const Color(0xFFFDE68A)
                              : const Color(0xFF92400E),
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              Text(
                'LOGGED IN DEVICES',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: isDark
                      ? LeoColors.darkTextSecondary
                      : LeoColors.textSecondary,
                ),
              ),
              const SizedBox(height: 12),

              sessionsAsync.when(
                data: (sessions) {
                  if (sessions.isEmpty) {
                    return Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(32),
                      decoration: BoxDecoration(
                        color: isDark ? LeoColors.darkSurface : Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: isDark ? LeoColors.darkBorder : LeoColors.border,
                        ),
                      ),
                      child: Column(
                        children: [
                          const Icon(Icons.devices_other_rounded,
                              size: 40, color: LeoColors.textMuted),
                          const SizedBox(height: 12),
                          Text(
                            'Current device is active',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: isDark
                                  ? LeoColors.darkTextPrimary
                                  : LeoColors.textPrimary,
                            ),
                          ),
                        ],
                      ),
                    );
                  }

                  return ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: sessions.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (context, index) {
                      final s = sessions[index];
                      final icon = _getDeviceIcon(s.platform, s.userAgent);
                      final isCurrent = s.isCurrentDevice && index == 0;

                      return Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: isDark ? LeoColors.darkSurface : Colors.white,
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(
                            color: isCurrent
                                ? LeoColors.primary.withOpacity(0.5)
                                : (isDark
                                    ? LeoColors.darkBorder
                                    : LeoColors.border),
                          ),
                        ),
                        child: Row(
                          children: [
                            Container(
                              width: 44,
                              height: 44,
                              decoration: BoxDecoration(
                                color: isCurrent
                                    ? LeoColors.primarySoft
                                    : (isDark
                                        ? LeoColors.darkSurfaceElevated
                                        : const Color(0xFFF1F5F9)),
                                borderRadius: BorderRadius.circular(14),
                              ),
                              child: Icon(
                                icon,
                                color: isCurrent
                                    ? LeoColors.primary
                                    : (isDark
                                        ? LeoColors.darkTextSecondary
                                        : const Color(0xFF475569)),
                                size: 24,
                              ),
                            ),
                            const SizedBox(width: 14),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Row(
                                    children: [
                                      Flexible(
                                        child: Text(
                                          s.deviceName ??
                                              s.platform ??
                                              'LeoChat Session',
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: TextStyle(
                                            fontSize: 15,
                                            fontWeight: FontWeight.w700,
                                            color: isDark
                                                ? LeoColors.darkTextPrimary
                                                : LeoColors.textPrimary,
                                          ),
                                        ),
                                      ),
                                      if (isCurrent) ...[
                                        const SizedBox(width: 8),
                                        Container(
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 6, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: const Color(0xFF10B981)
                                                .withOpacity(0.15),
                                            borderRadius:
                                                BorderRadius.circular(6),
                                          ),
                                          child: const Text(
                                            'THIS DEVICE',
                                            style: TextStyle(
                                              fontSize: 9,
                                              fontWeight: FontWeight.w900,
                                              color: Color(0xFF10B981),
                                            ),
                                          ),
                                        ),
                                      ],
                                    ],
                                  ),
                                  const SizedBox(height: 3),
                                  Text(
                                    '${s.ipAddress ?? "IP Protected"} · ${_formatRelativeTime(s.lastUsedAt)}',
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: isDark
                                          ? LeoColors.darkTextSecondary
                                          : LeoColors.textSecondary,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            if (!isCurrent)
                              LeoPressable(
                                onPressed: () async {
                                  final res = await ref
                                      .read(usersRepositoryProvider)
                                      .revokeSession(s.sessionId);
                                  res.fold(
                                    (f) => LeoSnackBar.showError(
                                        context, f.message),
                                    (_) {
                                      LeoSnackBar.showSuccess(
                                          context, 'Session revoked');
                                      ref.invalidate(userSessionsProvider);
                                    },
                                  );
                                },
                                child: Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 10, vertical: 6),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFEF4444)
                                        .withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                  child: const Text(
                                    'Revoke',
                                    style: TextStyle(
                                      fontSize: 12,
                                      fontWeight: FontWeight.w700,
                                      color: Color(0xFFEF4444),
                                    ),
                                  ),
                                ),
                              ),
                          ],
                        ),
                      );
                    },
                  );
                },
                loading: () => const Center(
                  child: Padding(
                    padding: EdgeInsets.all(32),
                    child: CircularProgressIndicator(color: LeoColors.primary),
                  ),
                ),
                error: (e, _) => Center(
                  child: Text(
                    'Error: $e',
                    style: TextStyle(
                      color: isDark
                          ? LeoColors.darkTextPrimary
                          : LeoColors.textPrimary,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
