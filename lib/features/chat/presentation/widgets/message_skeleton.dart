import 'package:flutter/material.dart';

import '../../../../core/widgets/common_widgets/feedback/leo_skeleton.dart';

/// Skeleton loader for chat messages inside the conversation detail view.
class LeoMessageSkeletonList extends StatelessWidget {
  const LeoMessageSkeletonList({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(24, 20, 24, 16),
      physics: const NeverScrollableScrollPhysics(),
      children: const [
        Center(child: LeoSkeleton(width: 90, height: 12, borderRadius: 6)),
        SizedBox(height: 24),
        // Incoming message skeleton
        Align(
          alignment: Alignment.centerLeft,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              LeoSkeleton(width: 100, height: 12, borderRadius: 4),
              SizedBox(height: 6),
              LeoSkeleton(width: 260, height: 60, borderRadius: 18),
            ],
          ),
        ),
        SizedBox(height: 18),
        // Outgoing message skeleton
        Align(
          alignment: Alignment.centerRight,
          child: LeoSkeleton(width: 220, height: 50, borderRadius: 18),
        ),
        SizedBox(height: 18),
        // Another incoming message skeleton
        Align(
          alignment: Alignment.centerLeft,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              LeoSkeleton(width: 80, height: 12, borderRadius: 4),
              SizedBox(height: 6),
              LeoSkeleton(width: 300, height: 75, borderRadius: 18),
            ],
          ),
        ),
        SizedBox(height: 18),
        // Another outgoing message skeleton
        Align(
          alignment: Alignment.centerRight,
          child: LeoSkeleton(width: 180, height: 42, borderRadius: 18),
        ),
      ],
    );
  }
}
