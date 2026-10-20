import 'package:flutter/material.dart';

import '../../../../core/widgets/common_widgets/feedback/leo_shimmer.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_skeleton.dart';

/// Skeleton loader with lightning shimmer effect for chat messages in conversation detail view.
class LeoMessageSkeletonList extends StatelessWidget {
  const LeoMessageSkeletonList({super.key});

  @override
  Widget build(BuildContext context) {
    return LeoShimmer(
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 16),
        physics: const NeverScrollableScrollPhysics(),
        children: const [
          Center(
            child: LeoSkeleton(width: 80, height: 12, borderRadius: 6),
          ),
          SizedBox(height: 24),
          // Incoming message with avatar
          Row(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              LeoSkeleton.circle(size: 34),
              SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    LeoSkeleton(width: 90, height: 11, borderRadius: 4),
                    SizedBox(height: 6),
                    LeoSkeleton(width: 250, height: 56, borderRadius: 18),
                  ],
                ),
              ),
            ],
          ),
          SizedBox(height: 20),
          // Outgoing message
          Align(
            alignment: Alignment.centerRight,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                LeoSkeleton(width: 210, height: 48, borderRadius: 18),
                SizedBox(height: 4),
                LeoSkeleton(width: 44, height: 9, borderRadius: 3),
              ],
            ),
          ),
          SizedBox(height: 20),
          // Another incoming message (taller)
          Row(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              LeoSkeleton.circle(size: 34),
              SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    LeoSkeleton(width: 70, height: 11, borderRadius: 4),
                    SizedBox(height: 6),
                    LeoSkeleton(width: 280, height: 74, borderRadius: 18),
                  ],
                ),
              ),
            ],
          ),
          SizedBox(height: 20),
          // Another outgoing message
          Align(
            alignment: Alignment.centerRight,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                LeoSkeleton(width: 160, height: 42, borderRadius: 18),
                SizedBox(height: 4),
                LeoSkeleton(width: 40, height: 9, borderRadius: 3),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
