import 'package:flutter/material.dart';

import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';

class AiThinkingBox extends StatefulWidget {
  const AiThinkingBox({
    super.key,
    required this.thinkingContent,
    this.durationSecs,
  });

  final String thinkingContent;
  final double? durationSecs;

  @override
  State<AiThinkingBox> createState() => _AiThinkingBoxState();
}

class _AiThinkingBoxState extends State<AiThinkingBox> {
  bool _isExpanded = false;

  @override
  Widget build(BuildContext context) {
    final durationLabel = widget.durationSecs != null
        ? '${widget.durationSecs!.toStringAsFixed(1)}s'
        : '';

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          LeoPressable(
            onPressed: () => setState(() => _isExpanded = !_isExpanded),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              child: Row(
                children: [
                  const Icon(
                    Icons.psychology_rounded,
                    size: 18,
                    color: Color(0xFF64748B),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Thinking Process $durationLabel',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF475569),
                    ),
                  ),
                  const Spacer(),
                  Icon(
                    _isExpanded
                        ? Icons.keyboard_arrow_up_rounded
                        : Icons.keyboard_arrow_down_rounded,
                    size: 18,
                    color: const Color(0xFF64748B),
                  ),
                ],
              ),
            ),
          ),
          if (_isExpanded)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.fromLTRB(14, 0, 14, 12),
              child: Text(
                widget.thinkingContent,
                style: const TextStyle(
                  fontSize: 12,
                  fontFamily: 'monospace',
                  height: 1.5,
                  color: Color(0xFF334155),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
