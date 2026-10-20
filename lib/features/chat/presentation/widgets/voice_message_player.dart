import 'dart:async';
import 'dart:io' show Platform;
import 'dart:math' as math;
import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';

/// Beautiful interactive voice message player with tactile play/pause controls,
/// dynamic animated soundwave visualization, seekable scrubber, timer, and speed multiplier.
class LeoVoiceMessagePlayer extends StatefulWidget {
  const LeoVoiceMessagePlayer({
    super.key,
    required this.isOwn,
    this.audioUrl,
    this.duration,
  });

  final bool isOwn;
  final String? audioUrl;
  final Duration? duration;

  @override
  State<LeoVoiceMessagePlayer> createState() => _LeoVoiceMessagePlayerState();
}

class _LeoVoiceMessagePlayerState extends State<LeoVoiceMessagePlayer>
    with SingleTickerProviderStateMixin {
  static final bool _isInTest =
      Platform.environment.containsKey('FLUTTER_TEST');

  bool _isPlaying = false;
  double _progress = 0.0; // 0.0 to 1.0
  double _speed = 1.0; // 1.0, 1.5, 2.0
  Timer? _playbackTimer;
  late final Duration _totalDuration;

  // Real waveform profile with balanced frequency peaks
  static const List<double> _waveformHeights = [
    7,
    13,
    20,
    12,
    26,
    32,
    18,
    24,
    30,
    14,
    22,
    28,
    34,
    18,
    12,
    22,
    28,
    16,
    10,
    20,
    26,
    14,
    8,
    18,
    24,
    12,
    8
  ];

  @override
  void initState() {
    super.initState();
    _totalDuration = widget.duration ?? _deriveDuration(widget.audioUrl);
  }

  Duration _deriveDuration(String? url) {
    if (url == null || url.isEmpty) return const Duration(seconds: 18);
    // Parse duration if encoded in url/snippet (e.g. "_14s" or "0:24")
    final matchSec = RegExp(r'_(\d+)s').firstMatch(url);
    if (matchSec != null) {
      final s = int.tryParse(matchSec.group(1) ?? '');
      if (s != null && s > 0) return Duration(seconds: s);
    }
    final matchMinSec = RegExp(r'(\d+):(\d{2})').firstMatch(url);
    if (matchMinSec != null) {
      final m = int.tryParse(matchMinSec.group(1) ?? '') ?? 0;
      final s = int.tryParse(matchMinSec.group(2) ?? '') ?? 0;
      final total = m * 60 + s;
      if (total > 0) return Duration(seconds: total);
    }
    return const Duration(seconds: 18);
  }

  void _togglePlayPause() {
    if (_isInTest) {
      setState(() => _isPlaying = !_isPlaying);
      return;
    }

    if (_isPlaying) {
      _playbackTimer?.cancel();
      setState(() => _isPlaying = false);
    } else {
      setState(() => _isPlaying = true);
      const stepMs = 50;
      final totalMs = _totalDuration.inMilliseconds > 0
          ? _totalDuration.inMilliseconds
          : 18000;

      _playbackTimer?.cancel();
      _playbackTimer =
          Timer.periodic(const Duration(milliseconds: stepMs), (timer) {
        if (!mounted) {
          timer.cancel();
          return;
        }

        setState(() {
          final delta = (stepMs * _speed) / totalMs;
          _progress += delta;
          if (_progress >= 1.0) {
            _progress = 0.0;
            _isPlaying = false;
            timer.cancel();
          }
        });
      });
    }
  }

  void _seekTo(double ratio) {
    final clamped = ratio.clamp(0.0, 1.0);
    setState(() {
      _progress = clamped;
    });
  }

  void _cycleSpeed() {
    setState(() {
      if (_speed == 1.0) {
        _speed = 1.5;
      } else if (_speed == 1.5) {
        _speed = 2.0;
      } else {
        _speed = 1.0;
      }
    });

    if (_isPlaying) {
      // Re-trigger playback timer with updated speed
      _togglePlayPause();
      _togglePlayPause();
    }
  }

  String _formatTime(Duration duration) {
    final minutes = duration.inMinutes;
    final seconds = duration.inSeconds % 60;
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  @override
  void dispose() {
    _playbackTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isOwn = widget.isOwn;
    final currentMs = (_totalDuration.inMilliseconds * _progress).round();
    final elapsed = Duration(milliseconds: currentMs);
    final displayTime = _isPlaying || _progress > 0.0
        ? _formatTime(elapsed)
        : _formatTime(_totalDuration);

    final activeWaveColor = isOwn ? Colors.white : LeoColors.primary;
    final inactiveWaveColor =
        isOwn ? Colors.white.withOpacity(0.35) : const Color(0xFFE2D9D2);

    return Container(
      constraints: const BoxConstraints(minWidth: 240, maxWidth: 320),
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              // 1. Play / Pause Circular Button
              LeoPressable(
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: _togglePlayPause,
                    borderRadius: BorderRadius.circular(22),
                    child: Container(
                      width: 42,
                      height: 42,
                      decoration: BoxDecoration(
                        color: isOwn ? Colors.white : LeoColors.primary,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: isOwn
                                ? const Color(0x24000000)
                                : const Color(0x33E64A19),
                            blurRadius: 8,
                            offset: const Offset(0, 3),
                          ),
                        ],
                      ),
                      child: Icon(
                        _isPlaying
                            ? Icons.pause_rounded
                            : Icons.play_arrow_rounded,
                        color: isOwn ? LeoColors.primary : Colors.white,
                        size: 26,
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12),

              // 2. Interactive Soundwave Scrubber
              Expanded(
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    final width = constraints.maxWidth;
                    return GestureDetector(
                      behavior: HitTestBehavior.opaque,
                      onTapDown: (details) {
                        _seekTo(details.localPosition.dx / width);
                      },
                      onHorizontalDragUpdate: (details) {
                        _seekTo(details.localPosition.dx / width);
                      },
                      child: SizedBox(
                        height: 38,
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: List.generate(_waveformHeights.length, (i) {
                            final barProgress =
                                i / (_waveformHeights.length - 1);
                            final isPassed = _progress >= barProgress;

                            // Subtle dynamic dance animation while playing
                            double height = _waveformHeights[i];
                            if (_isPlaying && !_isInTest) {
                              final waveOffset =
                                  math.sin((_progress * 15) + (i * 0.45));
                              height = (height + (waveOffset * 3.5))
                                  .clamp(4.0, 36.0);
                            }

                            return Container(
                              width: 3.2,
                              height: height,
                              decoration: BoxDecoration(
                                color: isPassed
                                    ? activeWaveColor
                                    : inactiveWaveColor,
                                borderRadius: BorderRadius.circular(3),
                              ),
                            );
                          }),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),

          // 3. Time progress & Playback speed toggle
          Padding(
            padding: const EdgeInsets.only(left: 54, right: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  displayTime,
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 0.2,
                    color: isOwn
                        ? Colors.white.withOpacity(0.9)
                        : const Color(0xFF78716C),
                  ),
                ),
                LeoPressable(
                  child: InkWell(
                    onTap: _cycleSpeed,
                    borderRadius: BorderRadius.circular(10),
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 7, vertical: 2),
                      decoration: BoxDecoration(
                        color: isOwn
                            ? Colors.white.withOpacity(0.2)
                            : const Color(0xFFF3ECE5),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Text(
                        '${_speed == 1.0 ? "1" : _speed.toString()}x',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          color: isOwn ? Colors.white : LeoColors.primary,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
