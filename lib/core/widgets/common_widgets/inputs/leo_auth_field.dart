import 'package:flutter/material.dart';

import '../../../theme/fields.dart';

class LeoAuthField extends StatelessWidget {
  const LeoAuthField({
    super.key,
    required this.label,
    required this.hint,
    this.icon,
    required this.controller,
    this.obscureText = false,
    this.suffix,
  });

  final String label;
  final String hint;
  final IconData? icon;
  final TextEditingController controller;
  final bool obscureText;
  final Widget? suffix;

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: Color(0xFF78716C),
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: controller,
            obscureText: obscureText,
            style: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w600,
              color: Color(0xFF1C1917),
            ),
            decoration: LeoFields.auth(
              hint: hint,
              icon: icon,
              suffix: suffix,
            ),
          ),
        ],
      );
}
