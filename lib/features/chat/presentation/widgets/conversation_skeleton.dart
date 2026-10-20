import 'package:flutter/material.dart';

import '../../../../core/widgets/common_widgets/feedback/leo_shimmer.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_skeleton.dart';

/// Skeleton loader with lightning shimmer effect for conversation list items.
class LeoConversationSkeletonList extends StatelessWidget {
  const LeoConversationSkeletonList({
    super.key,
    this.itemCount = 6,
  });

  final int itemCount;

  @override
  Widget build(BuildContext context) {
    return LeoShimmer(
      child: Column(
        children: List.generate(
          itemCount,
          (index) => const Padding(
            padding: EdgeInsets.symmetric(vertical: 8),
            child: Row(
              children: [
                LeoSkeleton.circle(size: 52),
                SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      LeoSkeleton(width: 130, height: 16, borderRadius: 6),
                      SizedBox(height: 8),
                      LeoSkeleton(height: 13, borderRadius: 5),
                    ],
                  ),
                ),
                SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    LeoSkeleton(width: 36, height: 11, borderRadius: 4),
                    SizedBox(height: 8),
                    LeoSkeleton.circle(size: 18),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
