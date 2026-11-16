enum AiModelIcon { sparkles, brain, code, zap }

class AiModelInfo {
  const AiModelInfo({
    required this.id,
    required this.name,
    required this.tagline,
    required this.badge,
    required this.contextWindow,
    required this.icon,
  });

  final String id;
  final String name;
  final String tagline;
  final String badge;
  final String contextWindow;
  final AiModelIcon icon;

  static const defaultModel = AiModelInfo(
    id: 'leo-ultra',
    name: 'LeoChat 4.5 Ultra',
    tagline: 'Flagship reasoning & multi-modal intelligence',
    badge: 'Default',
    contextWindow: '128k',
    icon: AiModelIcon.sparkles,
  );

  static const models = [
    defaultModel,
    AiModelInfo(
      id: 'leo-reasoner',
      name: 'Leo Reasoning R1',
      tagline: 'Deep chain-of-thought for science, math & logic',
      badge: 'Deep Think',
      contextWindow: '64k',
      icon: AiModelIcon.brain,
    ),
    AiModelInfo(
      id: 'leo-coder',
      name: 'Leo Code Architect',
      tagline: 'Expert full-stack programming, architecture & review',
      badge: 'Coding',
      contextWindow: '128k',
      icon: AiModelIcon.code,
    ),
    AiModelInfo(
      id: 'leo-fast',
      name: 'Leo Flash 3.5',
      tagline: 'Instant low-latency responses for quick tasks',
      badge: 'High Speed',
      contextWindow: '32k',
      icon: AiModelIcon.zap,
    ),
  ];
}
