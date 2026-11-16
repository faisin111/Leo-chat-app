import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../domain/entities/admin_report.dart';
import '../providers/admin_providers.dart';

class AdminReportsPage extends ConsumerWidget {
  const AdminReportsPage({super.key});

  static const _filters = ['ALL', 'OPEN', 'RESOLVED', 'DISMISSED'];

  void _showReportDialog(
      BuildContext context, WidgetRef ref, AdminReport report) {
    final resolutionController = TextEditingController();

    showDialog<void>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: Text(
            'Report: ${report.reason}',
            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 18),
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Target: ${report.targetType} (${report.targetId})',
                style: const TextStyle(
                    fontSize: 12, color: LeoColors.textSecondary),
              ),
              const SizedBox(height: 8),
              if (report.details != null && report.details!.isNotEmpty) ...[
                Text(
                  'Details: ${report.details}',
                  style: const TextStyle(
                      fontSize: 14, color: LeoColors.textPrimary),
                ),
                const SizedBox(height: 14),
              ],
              TextField(
                controller: resolutionController,
                decoration: const InputDecoration(
                  labelText: 'Resolution note',
                  hintText: 'e.g. Warning issued or Verified no violation',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            if (report.targetType == 'MESSAGE')
              TextButton(
                onPressed: () async {
                  Navigator.pop(dialogContext);
                  final res =
                      await ref.read(adminRepositoryProvider).deleteMessage(
                            messageId: report.targetId,
                            reason: 'Deleted by moderator for ${report.reason}',
                          );
                  res.fold(
                    (f) => LeoSnackBar.showError(context, f.message),
                    (_) {
                      LeoSnackBar.showSuccess(
                          context, 'Reported message deleted');
                      ref.invalidate(adminReportsListProvider);
                    },
                  );
                },
                child: const Text('Delete Message',
                    style: TextStyle(color: Color(0xFFEF4444))),
              ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: LeoColors.primary,
                foregroundColor: Colors.white,
              ),
              onPressed: () async {
                final resolution = resolutionController.text.trim();
                if (resolution.isEmpty) return;
                Navigator.pop(dialogContext);

                final res =
                    await ref.read(adminRepositoryProvider).resolveReport(
                          reportId: report.id,
                          resolution: resolution,
                        );
                res.fold(
                  (f) => LeoSnackBar.showError(context, f.message),
                  (_) {
                    LeoSnackBar.showSuccess(
                        context, 'Report resolved successfully');
                    ref.invalidate(adminReportsListProvider);
                  },
                );
              },
              child: const Text('Resolve'),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentFilter = ref.watch(adminReportsFilterProvider);
    final reportsAsync = ref.watch(adminReportsListProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => context.pop(),
          ),
        ),
        title: const Text(
          AppStrings.reportModeration,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            color: LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Filter Pills
            Container(
              height: 40,
              margin: const EdgeInsets.symmetric(vertical: 12),
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _filters.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, index) {
                  final filter = _filters[index];
                  final isSelected = filter == currentFilter;

                  return LeoPressable(
                    onPressed: () {
                      ref.read(adminReportsFilterProvider.notifier).state =
                          filter;
                    },
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 14, vertical: 8),
                      decoration: BoxDecoration(
                        color: isSelected ? LeoColors.primary : Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color:
                              isSelected ? LeoColors.primary : LeoColors.border,
                        ),
                      ),
                      child: Text(
                        filter,
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          color: isSelected
                              ? Colors.white
                              : LeoColors.textSecondary,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),

            // Reports List
            Expanded(
              child: reportsAsync.when(
                data: (reports) {
                  if (reports.isEmpty) {
                    return const Center(
                      child: Text(
                        'No reports found in this category',
                        style: TextStyle(
                          fontSize: 14,
                          color: LeoColors.textSecondary,
                        ),
                      ),
                    );
                  }

                  return ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: reports.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (context, index) {
                      final report = reports[index];

                      Color badgeColor = const Color(0xFFEF4444);
                      if (report.isResolved) {
                        badgeColor = const Color(0xFF10B981);
                      }

                      return LeoPressable(
                        onPressed: () =>
                            _showReportDialog(context, ref, report),
                        child: Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(color: LeoColors.border),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                        horizontal: 8, vertical: 3),
                                    decoration: BoxDecoration(
                                      color: badgeColor.withOpacity(0.12),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: Text(
                                      report.status,
                                      style: TextStyle(
                                        fontSize: 10,
                                        fontWeight: FontWeight.w800,
                                        color: badgeColor,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    report.targetType,
                                    style: const TextStyle(
                                      fontSize: 12,
                                      fontWeight: FontWeight.w700,
                                      color: LeoColors.textSecondary,
                                    ),
                                  ),
                                  const Spacer(),
                                  const Icon(
                                    Icons.chevron_right_rounded,
                                    color: Color(0xFF94A3B8),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              Text(
                                report.reason,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w800,
                                  color: LeoColors.textPrimary,
                                ),
                              ),
                              if (report.details != null &&
                                  report.details!.isNotEmpty) ...[
                                const SizedBox(height: 4),
                                Text(
                                  report.details!,
                                  style: const TextStyle(
                                    fontSize: 13,
                                    color: LeoColors.textSecondary,
                                  ),
                                ),
                              ],
                              if (report.resolution != null) ...[
                                const SizedBox(height: 8),
                                Container(
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFF1F5F9),
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                  child: Text(
                                    'Resolution: ${report.resolution}',
                                    style: const TextStyle(
                                      fontSize: 11,
                                      fontStyle: FontStyle.italic,
                                      color: Color(0xFF475569),
                                    ),
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                      );
                    },
                  );
                },
                loading: () => const Center(
                  child: CircularProgressIndicator(color: LeoColors.primary),
                ),
                error: (e, _) =>
                    Center(child: Text('Error loading reports: $e')),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
