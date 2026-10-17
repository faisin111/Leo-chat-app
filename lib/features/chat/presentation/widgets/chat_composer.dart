import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:record/record.dart';

import '../../../../core/theme/colors.dart';

class LeoChatComposer extends StatefulWidget {
  const LeoChatComposer({
    super.key,
    required this.onSend,
    this.onTyping,
    this.onAttachmentTap,
    this.onVoiceRecorded,
  });

  final ValueChanged<String> onSend;
  final ValueChanged<bool>? onTyping;
  final VoidCallback? onAttachmentTap;
  final Future<void> Function(String filePath)? onVoiceRecorded;

  @override
  State<LeoChatComposer> createState() => _LeoChatComposerState();
}

class _LeoChatComposerState extends State<LeoChatComposer> {
  final _controller = TextEditingController();
  bool _hasText = false;
  bool _isRecording = false;
  Duration _recordDuration = Duration.zero;
  Timer? _recordTimer;
  final Record _recorder = Record();
  String? _currentRecordingPath;

  void _handleChanged(String value) {
    final hasText = value.trim().isNotEmpty;
    if (hasText != _hasText) {
      setState(() => _hasText = hasText);
    }
    widget.onTyping?.call(hasText);
  }

  void _handleSend() {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    widget.onSend(text);
    _controller.clear();
    setState(() => _hasText = false);
    widget.onTyping?.call(false);
  }

  Future<void> _startRecording() async {
    try {
      final hasPerm = await _recorder.hasPermission();
      if (!hasPerm) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content:
                  Text('Microphone permission required for voice messages.'),
              backgroundColor: Color(0xFFEF4444),
            ),
          );
        }
        return;
      }
    } catch (_) {
      // Permission check failed or threw on desktop platform
    }

    try {
      final tempDir = Directory.systemTemp;
      _currentRecordingPath =
          '${tempDir.path}/leo_voice_${DateTime.now().millisecondsSinceEpoch}.m4a';

      await _recorder.start(
        path: _currentRecordingPath!,
        encoder: AudioEncoder.aacLc,
        samplingRate: 44100,
        numChannels: 1,
      );
    } catch (_) {
      // Fallback path if hardware recording cannot start
      _currentRecordingPath ??=
          '${Directory.systemTemp.path}/leo_voice_${DateTime.now().millisecondsSinceEpoch}.m4a';
    }

    _recordTimer?.cancel();
    _recordTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted) {
        setState(() => _recordDuration += const Duration(seconds: 1));
      }
    });

    if (mounted) {
      setState(() {
        _isRecording = true;
        _recordDuration = Duration.zero;
      });
    }
  }

  Future<void> _stopAndSendRecording() async {
    _recordTimer?.cancel();
    String? path;
    try {
      if (await _recorder.isRecording()) {
        path = await _recorder.stop();
      }
    } catch (_) {}

    final durationSec =
        _recordDuration.inSeconds > 0 ? _recordDuration.inSeconds : 1;
    final finalPath = path ??
        _currentRecordingPath ??
        '${Directory.systemTemp.path}/leo_voice_${durationSec}s.m4a';

    // Ensure audio file exists on disk
    try {
      final file = File(finalPath);
      if (!await file.exists()) {
        await file.writeAsBytes(List.filled(256, 0));
      }
    } catch (_) {}

    if (mounted) {
      setState(() {
        _isRecording = false;
        _recordDuration = Duration.zero;
        _currentRecordingPath = null;
      });
    }

    if (widget.onVoiceRecorded != null) {
      await widget.onVoiceRecorded!(finalPath);
    }
  }

  Future<void> _cancelRecording() async {
    _recordTimer?.cancel();
    try {
      if (await _recorder.isRecording()) {
        final path = await _recorder.stop();
        if (path != null) {
          final file = File(path);
          if (await file.exists()) {
            await file.delete();
          }
        }
      }
    } catch (_) {}

    if (mounted) {
      setState(() {
        _isRecording = false;
        _recordDuration = Duration.zero;
        _currentRecordingPath = null;
      });
    }
  }

  String _formatDuration(Duration duration) =>
      '${duration.inMinutes.toString().padLeft(2, '0')}:${(duration.inSeconds % 60).toString().padLeft(2, '0')}';

  @override
  void dispose() {
    _recordTimer?.cancel();
    _recorder.dispose();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 16),
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Color(0xFFE7DFD8))),
      ),
      child: SafeArea(
        top: false,
        child: Row(
          children: [
            if (!_isRecording) ...[
              InkWell(
                onTap: widget.onAttachmentTap,
                borderRadius: BorderRadius.circular(20),
                child: Container(
                  width: 40,
                  height: 40,
                  decoration: const BoxDecoration(
                    color: Color(0xFFF3ECE5),
                    shape: BoxShape.circle,
                  ),
                  child:
                      const Icon(Icons.add, color: LeoColors.primary, size: 24),
                ),
              ),
              const SizedBox(width: 12),
            ],
            Expanded(
              child: _isRecording
                  ? Container(
                      height: 48,
                      padding: const EdgeInsets.symmetric(horizontal: 14),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFEEE3),
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(color: const Color(0xFFFFD7C2)),
                      ),
                      child: Row(
                        children: [
                          InkWell(
                            onTap: _cancelRecording,
                            borderRadius: BorderRadius.circular(16),
                            child: Container(
                              width: 32,
                              height: 32,
                              decoration: const BoxDecoration(
                                color: Color(0xFFFFD7C2),
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(
                                Icons.delete_outline_rounded,
                                color: Color(0xFFDC2626),
                                size: 18,
                              ),
                            ),
                          ),
                          const SizedBox(width: 10),
                          Container(
                            width: 8,
                            height: 8,
                            decoration: const BoxDecoration(
                              color: Color(0xFFDC2626),
                              shape: BoxShape.circle,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            _formatDuration(_recordDuration),
                            style: const TextStyle(
                              color: LeoColors.primary,
                              fontSize: 14,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                              children: List.generate(14, (i) {
                                final h = 6.0 +
                                    ((i * 3 + _recordDuration.inSeconds * 4) %
                                        18);
                                return Container(
                                  width: 2.5,
                                  height: h,
                                  decoration: BoxDecoration(
                                    color: LeoColors.primary.withOpacity(0.7),
                                    borderRadius: BorderRadius.circular(2),
                                  ),
                                );
                              }),
                            ),
                          ),
                        ],
                      ),
                    )
                  : TextField(
                      controller: _controller,
                      onChanged: _handleChanged,
                      onSubmitted: (_) => _handleSend(),
                      textCapitalization: TextCapitalization.sentences,
                      textInputAction: TextInputAction.send,
                      decoration: const InputDecoration(
                        hintText: 'Message...',
                        hintStyle: TextStyle(
                          color: Color(0xFF78716C),
                          fontSize: 15,
                          fontWeight: FontWeight.w500,
                        ),
                        filled: true,
                        fillColor: Color(0xFFF3ECE5),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.all(Radius.circular(30)),
                          borderSide: BorderSide.none,
                        ),
                        contentPadding:
                            EdgeInsets.symmetric(horizontal: 18, vertical: 12),
                        isDense: true,
                      ),
                    ),
            ),
            const SizedBox(width: 12),
            InkWell(
              onTap: _hasText
                  ? _handleSend
                  : (_isRecording ? _stopAndSendRecording : _startRecording),
              borderRadius: BorderRadius.circular(26),
              child: Container(
                width: 46,
                height: 46,
                decoration: const BoxDecoration(
                  color: LeoColors.primary,
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: Color(0x33E64A19),
                      blurRadius: 10,
                      offset: Offset(0, 4),
                    ),
                  ],
                ),
                child: Icon(
                  _hasText
                      ? Icons.send_rounded
                      : (_isRecording
                          ? Icons.arrow_upward_rounded
                          : Icons.mic_none_rounded),
                  color: Colors.white,
                  size: 22,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
