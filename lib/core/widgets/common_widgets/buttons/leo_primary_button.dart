import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

class LeoPrimaryButton extends StatelessWidget {
  const LeoPrimaryButton(
      {super.key, required this.label, this.onPressed, this.icon});

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;

  @override
  Widget build(BuildContext context) => icon == null
      ? FilledButton(onPressed: onPressed, child: Text(label))
      : FilledButton.icon(
          onPressed: onPressed, icon: Icon(icon), label: Text(label));
}
