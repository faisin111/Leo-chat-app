import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';

class SharedMediaPage extends StatefulWidget {
  const SharedMediaPage({
    super.key,
    this.conversationTitle = 'Shared with Maya',
  });

  final String conversationTitle;

  @override
  State<SharedMediaPage> createState() => _SharedMediaPageState();
}

class _SharedMediaPageState extends State<SharedMediaPage> {
  int _selectedFilter = 0; // 0: Media, 1: Files, 2: Links

  final _photos = const [
    'assets/images/shared_1.png',
    'assets/images/shared_2.png',
    'assets/images/shared_3.png',
    'assets/images/shared_4.png',
    'assets/images/shared_5.png',
    'assets/images/shared_6.png',
  ];

  final _files = const [
    _SharedFileInfo(
      icon: Icons.article_rounded,
      title: 'Coast weekend.pdf',
      subtitle: 'Maya Chen · 2.4 MB · Today',
    ),
    _SharedFileInfo(
      icon: Icons.confirmation_number_rounded,
      title: 'Train tickets.pdf',
      subtitle: 'You · 840 KB · Oct 16',
    ),
    _SharedFileInfo(
      icon: Icons.music_note_rounded,
      title: 'Trip playlist.m4a',
      subtitle: 'Maya Chen · 4.8 MB · Oct 15',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: const Color(0xFFFAF7F4),
        elevation: 0,
        scrolledUnderElevation: 0,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.conversationTitle,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w900,
                letterSpacing: -0.5,
                color: LeoColors.textPrimary,
              ),
            ),
            const SizedBox(height: 2),
            const Text(
              'Everything, all in one place',
              style: TextStyle(
                fontSize: 12.5,
                fontWeight: FontWeight.w500,
                color: LeoColors.textSecondary,
              ),
            ),
          ],
        ),
        actions: [
          LeoPressable(
            child: IconButton(
              icon: const Icon(Icons.search_rounded,
                  color: LeoColors.textPrimary, size: 24),
              onPressed: () {},
            ),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Filter Pills
              Row(
                children: [
                  _FilterPill(
                    label: 'Media 18',
                    isSelected: _selectedFilter == 0,
                    onTap: () => setState(() => _selectedFilter = 0),
                  ),
                  const SizedBox(width: 8),
                  _FilterPill(
                    label: 'Files 4',
                    isSelected: _selectedFilter == 1,
                    onTap: () => setState(() => _selectedFilter = 1),
                  ),
                  const SizedBox(width: 8),
                  _FilterPill(
                    label: 'Links 2',
                    isSelected: _selectedFilter == 2,
                    onTap: () => setState(() => _selectedFilter = 2),
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Section: October 2026
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'October 2026',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  LeoPressable(
                    onPressed: () {},
                    child: const Text(
                      'Select',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w800,
                        color: LeoColors.primary,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),

              // Grid of photos (2 rows x 3 columns)
              GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  mainAxisSpacing: 10,
                  crossAxisSpacing: 10,
                  childAspectRatio: 0.9,
                ),
                itemCount: _photos.length,
                itemBuilder: (context, index) {
                  return LeoPressable(
                    onPressed: () {
                      _showPhotoPreview(context, _photos[index]);
                    },
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(16),
                      child: Image.asset(
                        _photos[index],
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => Container(
                          color: const Color(0xFFF3ECE5),
                          child: const Icon(Icons.image_outlined,
                              color: Color(0xFF78716C)),
                        ),
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 26),

              // Section: Recent files
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Recent files',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  LeoPressable(
                    onPressed: () {},
                    child: const Text(
                      'View all',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w800,
                        color: LeoColors.primary,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // Files Card
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: _files.length,
                  separatorBuilder: (_, __) => const Divider(
                      height: 1, indent: 68, color: Color(0xFFF3ECE5)),
                  itemBuilder: (context, index) {
                    final file = _files[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 12),
                      child: Row(
                        children: [
                          Container(
                            width: 44,
                            height: 44,
                            decoration: BoxDecoration(
                              color: const Color(0xFFFFEEE3),
                              borderRadius: BorderRadius.circular(14),
                            ),
                            child: Icon(
                              file.icon,
                              color: LeoColors.primary,
                              size: 22,
                            ),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  file.title,
                                  style: const TextStyle(
                                    fontSize: 15,
                                    fontWeight: FontWeight.w800,
                                    color: LeoColors.textPrimary,
                                  ),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  file.subtitle,
                                  style: const TextStyle(
                                    fontSize: 12.5,
                                    fontWeight: FontWeight.w500,
                                    color: Color(0xFF78716C),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          LeoPressable(
                            onPressed: () {
                              LeoSnackBar.showSuccess(
                                  context, 'Downloading ${file.title}...');
                            },
                            child: const Padding(
                              padding: EdgeInsets.all(8),
                              child: Icon(
                                Icons.download_rounded,
                                color: Color(0xFF78716C),
                                size: 22,
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 20),

              // Footer Info
              const Row(
                children: [
                  Icon(
                    Icons.info_outline_rounded,
                    size: 16,
                    color: Color(0xFF78716C),
                  ),
                  SizedBox(width: 8),
                  Text(
                    'Photos and files shared in this conversation.',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: Color(0xFF78716C),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  void _showPhotoPreview(BuildContext context, String assetPath) {
    showDialog<void>(
      context: context,
      builder: (context) => Dialog(
        backgroundColor: Colors.transparent,
        insetPadding: const EdgeInsets.all(16),
        child: Stack(
          alignment: Alignment.topRight,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(20),
              child: Image.asset(assetPath, fit: BoxFit.contain),
            ),
            IconButton(
              icon: const Icon(Icons.close_rounded,
                  color: Colors.white, size: 28),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      ),
    );
  }
}

class _SharedFileInfo {
  const _SharedFileInfo({
    required this.icon,
    required this.title,
    required this.subtitle,
  });

  final IconData icon;
  final String title;
  final String subtitle;
}

class _FilterPill extends StatelessWidget {
  const _FilterPill({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return LeoPressable(
      onPressed: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? LeoColors.primary : const Color(0xFFF3ECE5),
          borderRadius: BorderRadius.circular(999),
        ),
        child: Text(
          label,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w800,
            color: isSelected ? Colors.white : const Color(0xFFC05621),
          ),
        ),
      ),
    );
  }
}
