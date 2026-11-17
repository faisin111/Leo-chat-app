import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../domain/entities/conversation.dart';
import '../../domain/entities/message.dart';
import '../providers/chat_notifier.dart';

class SharedMediaPage extends ConsumerStatefulWidget {
  const SharedMediaPage({
    super.key,
    this.conversation,
    this.conversationTitle = 'Shared media & files',
  });

  final Conversation? conversation;
  final String conversationTitle;

  @override
  ConsumerState<SharedMediaPage> createState() => _SharedMediaPageState();
}

class _SharedMediaPageState extends ConsumerState<SharedMediaPage> {
  int _selectedFilter = 0; // 0: Media, 1: Files, 2: Links

  bool _isImage(Message m) {
    final t = m.type.toUpperCase();
    if (t == 'IMAGE' || t == 'IMAGE_MESSAGE') return true;
    final c = (m.content ?? '').toLowerCase();
    return c.endsWith('.png') ||
        c.endsWith('.jpg') ||
        c.endsWith('.jpeg') ||
        c.endsWith('.webp') ||
        c.endsWith('.gif');
  }

  bool _isFile(Message m) {
    if (_isImage(m)) return false;
    final t = m.type.toUpperCase();
    if (t == 'FILE' || t == 'DOCUMENT' || t == 'AUDIO' || t == 'VIDEO') {
      return true;
    }
    final c = (m.content ?? '').toLowerCase();
    return c.endsWith('.pdf') ||
        c.endsWith('.doc') ||
        c.endsWith('.docx') ||
        c.endsWith('.zip') ||
        c.endsWith('.mp3') ||
        c.endsWith('.mp4');
  }

  bool _hasLink(Message m) {
    if (_isImage(m) || _isFile(m)) return false;
    final content = m.content ?? '';
    return content.contains('http://') || content.contains('https://');
  }

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(chatNotifierProvider);
    final convId = widget.conversation?.id ?? chatState.selectedConversationId;

    final messages = chatState.messages.where((m) {
      if (convId != null && convId.isNotEmpty) {
        return m.conversationId == convId;
      }
      return true;
    }).toList();

    final mediaMessages = messages.where(_isImage).toList();
    final fileMessages = messages.where(_isFile).toList();
    final linkMessages = messages.where(_hasLink).toList();

    final title = widget.conversation?.title ?? widget.conversationTitle;

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
              'Shared in $title',
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w900,
                letterSpacing: -0.5,
                color: LeoColors.textPrimary,
              ),
            ),
            const SizedBox(height: 2),
            const Text(
              'Everything in one place',
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: LeoColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Filter Pills Header
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              child: Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: const Color(0xFFEFE8E1),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  children: [
                    _buildFilterPill(0, 'Media (${mediaMessages.length})'),
                    _buildFilterPill(1, 'Files (${fileMessages.length})'),
                    _buildFilterPill(2, 'Links (${linkMessages.length})'),
                  ],
                ),
              ),
            ),

            // Content Area
            Expanded(
              child: IndexedStack(
                index: _selectedFilter,
                children: [
                  _buildMediaGrid(mediaMessages),
                  _buildFilesList(fileMessages),
                  _buildLinksList(linkMessages),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFilterPill(int index, String label) {
    final isSelected = _selectedFilter == index;
    return Expanded(
      child: LeoPressable(
        onPressed: () => setState(() => _selectedFilter = index),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? Colors.white : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
            boxShadow: isSelected
                ? const [
                    BoxShadow(
                      color: Color(0x0A000000),
                      blurRadius: 4,
                      offset: Offset(0, 2),
                    ),
                  ]
                : null,
          ),
          child: Text(
            label,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 13,
              fontWeight: isSelected ? FontWeight.w800 : FontWeight.w600,
              color: isSelected ? LeoColors.primary : const Color(0xFF78716C),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildMediaGrid(List<Message> mediaItems) {
    if (mediaItems.isEmpty) {
      return _buildEmptyState(
        icon: Icons.photo_library_outlined,
        title: 'No media shared yet',
        subtitle: 'Photos and videos sent in this chat will appear here.',
      );
    }

    return GridView.builder(
      padding: const EdgeInsets.all(20),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
        childAspectRatio: 1,
      ),
      itemCount: mediaItems.length,
      itemBuilder: (context, index) {
        final item = mediaItems[index];
        final url = item.content ?? '';

        return ClipRRect(
          borderRadius: BorderRadius.circular(14),
          child: Container(
            color: const Color(0xFFE2E8F0),
            child: url.startsWith('http')
                ? Image.network(
                    url,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const Center(
                      child: Icon(Icons.broken_image_rounded,
                          color: Color(0xFF94A3B8)),
                    ),
                  )
                : const Center(
                    child: Icon(Icons.image_rounded,
                        color: Color(0xFF94A3B8), size: 32),
                  ),
          ),
        );
      },
    );
  }

  Widget _buildFilesList(List<Message> fileItems) {
    if (fileItems.isEmpty) {
      return _buildEmptyState(
        icon: Icons.insert_drive_file_outlined,
        title: 'No files shared yet',
        subtitle: 'Documents, audio, and attachments will appear here.',
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      itemCount: fileItems.length,
      separatorBuilder: (_, __) =>
          const Divider(height: 1, indent: 64, color: Color(0xFFF3ECE5)),
      itemBuilder: (context, index) {
        final item = fileItems[index];
        final content = item.content ?? 'Shared file';
        final fileName =
            content.contains('/') ? content.split('/').last : content;

        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: const Color(0xFFEFF6FF),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.description_rounded,
                    color: Color(0xFF3B82F6), size: 24),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      fileName,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: LeoColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      item.type,
                      style: const TextStyle(
                        fontSize: 12,
                        color: LeoColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildLinksList(List<Message> linkItems) {
    if (linkItems.isEmpty) {
      return _buildEmptyState(
        icon: Icons.link_rounded,
        title: 'No links shared yet',
        subtitle: 'Web links and URLs sent in this chat will appear here.',
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      itemCount: linkItems.length,
      separatorBuilder: (_, __) =>
          const Divider(height: 1, indent: 64, color: Color(0xFFF3ECE5)),
      itemBuilder: (context, index) {
        final item = linkItems[index];
        final content = item.content ?? '';
        final urlMatch =
            RegExp(r'https?://[^\s]+').firstMatch(content)?.group(0) ?? content;

        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: LeoColors.primarySoft,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.link_rounded,
                    color: LeoColors.primary, size: 24),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      urlMatch,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: LeoColors.primary,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      content,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 12,
                        color: LeoColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildEmptyState({
    required IconData icon,
    required String title,
    required String subtitle,
  }) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 60,
              height: 60,
              decoration: BoxDecoration(
                color: const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(30),
              ),
              child: Icon(icon, size: 28, color: const Color(0xFF78716C)),
            ),
            const SizedBox(height: 16),
            Text(
              title,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w800,
                color: LeoColors.textPrimary,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 13,
                color: LeoColors.textSecondary,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
