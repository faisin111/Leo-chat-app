class AdminReport {
  const AdminReport({
    required this.id,
    required this.reporterId,
    required this.targetType,
    required this.targetId,
    required this.reason,
    this.details,
    required this.status, // 'OPEN' | 'INVESTIGATING' | 'RESOLVED' | 'DISMISSED'
    required this.createdAt,
    this.resolvedAt,
    this.resolvedBy,
    this.resolution,
  });

  final String id;
  final String reporterId;
  final String targetType; // 'MESSAGE' | 'USER' | 'CONVERSATION'
  final String targetId;
  final String reason;
  final String? details;
  final String status;
  final DateTime createdAt;
  final DateTime? resolvedAt;
  final String? resolvedBy;
  final String? resolution;

  bool get isOpen => status == 'OPEN';
  bool get isResolved => status == 'RESOLVED';
}
