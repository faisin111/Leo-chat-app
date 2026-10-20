import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/widgets/common_widgets/feedback/leo_shimmer.dart';
import 'package:leochat/core/widgets/common_widgets/feedback/leo_skeleton.dart';
import 'package:leochat/features/chat/presentation/widgets/conversation_skeleton.dart';
import 'package:leochat/features/chat/presentation/widgets/message_skeleton.dart';

void main() {
  group('LeoShimmer and Lightning Skeleton widgets', () {
    testWidgets('LeoShimmer renders child widget tree', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LeoShimmer(
              child: Text('Shimmering content'),
            ),
          ),
        ),
      );

      expect(find.text('Shimmering content'), findsOneWidget);
      expect(find.byType(LeoShimmer), findsOneWidget);
    });

    testWidgets('LeoConversationSkeletonList renders shimmer and skeletons',
        (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LeoConversationSkeletonList(itemCount: 4),
          ),
        ),
      );

      expect(find.byType(LeoShimmer), findsOneWidget);
      expect(find.byType(LeoSkeleton), findsWidgets);
    });

    testWidgets('LeoMessageSkeletonList renders shimmer and chat placeholders',
        (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LeoMessageSkeletonList(),
          ),
        ),
      );

      expect(find.byType(LeoShimmer), findsOneWidget);
      expect(find.byType(LeoSkeleton), findsWidgets);
    });
  });
}
