Leo Chat — Frontend Design System, Architecture & Flutter Implementation Guide

This document is the definitive frontend reference for the Leo Chat application. It captures the complete theme specification, color palette, typography, layout architecture, UI/UX interaction models, and backend/WebSocket contracts needed to build an identical, high-performance companion Flutter mobile and desktop application.

1. Design System & Theme Specification

Leo Chat uses a sleek, modern, warm-toned design language built on Tailwind CSS variables. The primary brand identity features a distinctive warm coral/orange-red accent paired with slate neutrals, high-contrast surfaces, and soft rounded geometry.

1.1 Color Palette

All primary colors are specified in HSL (from web :root) and mapped to Hex 8-digit ARGB for Flutter.

Brand & Action Colors
Semantic Role	Web HSL Variable	Hex (Light)	Hex (Dark)	Flutter Color	Notes
Primary (Brand)	15 90% 55%	#F35A26	#F35A26	const Color(0xFFF35A26)	Vibrant warm coral / orange-red. Used for CTAs, active pills, own message bubbles, accents.
Primary Foreground	0 0% 100%	#FFFFFF	#FFFFFF	const Color(0xFFFFFFFF)	Text on primary brand surfaces.
Primary Soft	15 90% 96%	#FEF3EE	#2D1912	const Color(0xFFFEF3EE)	Light tint for hover states, selected lists, active badges.
Primary Border	15 90% 55% / 20%	#F35A2633	#F35A2633	const Color(0x33F35A26)	20% opacity border for reply quotes, active cards.
Destructive / Error	0 84% 60%	#EF4444	#DC2626	const Color(0xFFEF4444)	Used for delete actions, banned badges, error alerts.
Success / Online	150 84% 40%	#10B981	#059669	const Color(0xFF10B981)	Online presence indicator dots, positive toasts.
Warning / Suspended	38 92% 50%	#F59E0B	#D97706	const Color(0xFFF59E0B)	Flagged reports, unverified badges.
Neutral & Surface Colors
Surface / Element	Web Tailwind Class	Hex (Light)	Hex (Dark)	Flutter Color
App Background	bg-slate-50 / var(--background)	#F8FAFC	#0F172A	const Color(0xFFF8FAFC)
Card / Container Surface	bg-white / var(--card)	#FFFFFF	#1E293B	const Color(0xFFFFFFFF)
Sidebar / Master List	bg-white	#FFFFFF	#1E293B	const Color(0xFFFFFFFF)
Navigation Rail / Bar	bg-slate-950	#020617	#020617	const Color(0xFF020617)
Text Primary (Headings)	text-slate-900	#0F172A	#F8FAFC	const Color(0xFF0F172A)
Text Secondary (Body/Meta)	text-slate-600	#475569	#94A3B8	const Color(0xFF475569)
Text Muted / Subtitle	text-slate-400	#94A3B8	#64748B	const Color(0xFF94A3B8)
Borders / Dividers	border-slate-100 / border-slate-200	#E2E8F0	#334155	const Color(0xFFE2E8F0)
Input Fields Background	bg-slate-100/70	#F1F5F9	#1E293B	const Color(0xFFF1F5F9)
Chat Bubble Colors
Bubble Type	Web Class	Background Color	Text Color	Border / Corners
Own Message	bg-primary text-white	#F35A26	#FFFFFF	Rounded 16px, Top-Right corner rounded 4px (rounded-tr-sm).
Incoming Message	bg-slate-100 text-slate-800	#F1F5F9	#1E293B	Rounded 16px, Top-Left corner rounded 4px (rounded-tl-sm).
Deleted Message	italic opacity-70	Same background	#64748B (50% opacity)	Italic text with Trash2 icon.
Replied Parent Quote (Own)	bg-black/10 text-white/90	rgba(0,0,0,0.10)	#FFFFFF	Left border 2px solid #FFFFFF99.
Replied Parent Quote (Other)	bg-slate-200/70 text-slate-700	#E2E8F0	#334155	Left border 2px solid #F35A26.
Document Extension Badges

When displaying documents, badge backgrounds are dynamically colored by extension:

PDF: Red theme (#FEE2E2 bg, #B91C1C text)
DOC / DOCX: Blue theme (#DBEAFE bg, #1D4ED8 text)
XLS / XLSX / CSV: Emerald theme (#D1FAE5 bg, #047857 text)
PPT / PPTX: Orange theme (#FFEDD5 bg, #C2410C text)
ZIP / RAR / 7Z: Amber theme (#FEF3C7 bg, #B45309 text)
Default / Code: Indigo theme (#E0E7FF bg, #4338CA text)
1.2 Typography System

The app standardizes on Inter (system fallback: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto).

Style Name	Font Size	Line Height	Font Weight	Letter Spacing	Flutter TextStyle
Display Header	24px (text-2xl)	32px	700 (Bold)	-0.02em	fontSize: 24, fontWeight: FontWeight.w700
Section Header	18px (text-lg)	24px	700 (Bold)	-0.01em	fontSize: 18, fontWeight: FontWeight.w700
Conversation Title	14px (text-sm)	20px	600 (SemiBold)	0	fontSize: 14, fontWeight: FontWeight.w600
Message Body	14px (text-sm)	20px	400 (Regular)	0	fontSize: 14, fontWeight: FontWeight.w400
Subtext / Snippet	12px (text-xs)	16px	400 (Regular)	0	fontSize: 12, fontWeight: FontWeight.w400, color: slate500
Timestamp / Badge	10px (text-[10px])	14px	600 (SemiBold)	0.02em	fontSize: 10, fontWeight: FontWeight.w600
Label / Category	11px (text-[11px])	14px	700 (Bold)	0.05em (Uppercase)	fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.5
1.3 Shapes, Borders & Elevation
Buttons & Action Cards: Rounded 12px (rounded-xl in Tailwind) or 16px (rounded-2xl).
Modal Dialogs & Sheets: Rounded 24px (rounded-3xl).
Pills / Status Chips: Rounded 9999px (rounded-full).
Avatar Shapes: Circular (rounded-full).
Elevation / Shadows:
shadow-xs: 0 1px 2px rgba(0,0,0,0.05)
shadow-sm: 0 1px 3px rgba(0,0,0,0.1), 0 1px 2px rgba(0,0,0,0.06)
shadow-xl: 0 20px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1)
2. Layout Structure & Responsive Adaptation

The Leo Chat web layout is designed as an adaptive Master-Detail interface. In Flutter, this directly maps to a responsive split-view for tablets/desktops and a stack navigation for mobile devices.

2.1 Navigation Structure
Desktop / Tablet View (width >= 768dp)
Left Navigation Rail (width: 64dp, bg: #020617):
Top: App Logo (MessageSquare icon in Coral).
Navigation Items (Vertical Column):
Chats (/app) with unread count pill badge.
People (/app/people).
Settings (/app/settings).
Admin (/app/admin) — conditionally visible if user role is ADMIN.
Bottom: Help icon, Logout button, User Avatar profile shortcut.
Conversation List Sidebar (width: 340dp, bg: #FFFFFF):
Header: "Messages" title + "● Online" indicator + "New Chat" icon button.
Search Bar: Debounced input with clear button.
Filter Chips: All, Unread, Groups, Archived.
Paginated Virtual List: Conversation tiles with unread badges, pin icons, mute icons, and last message previews.
Active Chat Detail Area (flex: 1, bg: #FFFFFF):
Conversation Header (User/Group title, avatar, call icons, search toggle, options menu).
Message History (Scroll view, infinite scroll up for older messages).
Floating Action / Typing indicators.
Fixed Bottom Input Bar: Textarea, Emoji trigger, Paperclip Attachment menu, Send button.
Right Profile Drawer (width: 340dp): Slides in from right when clicking header info button.
Mobile View (width < 768dp)
Chat List Mode:
Conversation List takes 100% viewport width.
Bottom Navigation Bar replaces the Left Navigation Rail (h: 56dp, icons: Chats, People, Settings, Admin).
Active Chat Mode:
Navigating to /app/c/:conversationId pushes the full-screen ConversationPage.
The Bottom Navigation Bar and Conversation List are hidden (hidden md:flex).
The Chat Header displays a prominent Back Button (ArrowLeft) to return to the list.
3. Core Features & Functional Modules
3.1 Authentication & Session Lifecycle
Token Handling for Flutter
Bearer Authentication: Store accessToken and refreshToken in flutter_secure_storage.
Auto-Refresh Interceptor: Intercept 401 Unauthorized responses in Dio. Request POST /api/v1/auth/refresh with { "refreshToken": "..." }. Update stored tokens and replay original request.
Session Verification: Call GET /api/v1/users/me on app launch to bootstrap user profile.
Email Verification Gate: If user.emailVerified == false, route to VerifyEmailPage.
Password Change Gate: If user.forcePasswordChange == true, route to ChangePasswordRequiredPage.
3.2 Real-time STOMP WebSocket Protocol

Leo Chat communicates in real time over STOMP (over SockJS/WebSocket) with the base URL:

Endpoint: wss://leo-chat-backend.onrender.com/ws/websocket (or HTTP SockJS fallback).
WebSocket Connection Headers
json
{
  "Authorization": "Bearer <accessToken>"
}
Inbound Subscriptions
Destination	Purpose	Payload DTO
/user/queue/acks	Private acknowledgements when a sent message is persisted	Message object
/user/queue/errors	Private validation or business rule errors	{ code: string, message: string }
/user/queue/notifications	Personal notifications (system alerts, invites)	Notification object
/topic/conversations.{id}	Real-time messages, edits, reactions, deletions broadcast	Message object
/topic/conversations.{id}.typing	Broadcast typing status for a conversation	{ userId, username, displayName, isTyping }
Outbound Publications
Destination	Method	Payload
/app/chat.send	Send a new message	{ conversationId, clientMessageId, content, type, replyToId }
/app/chat.read	Send read receipt up to sequence	{ conversationId, upToSeq }
/app/chat.typing	Broadcast typing state	{ conversationId, isTyping }
3.3 Messaging Engine & Chat Page Rules

Sequence-based Ordering (seq):

Messages are strictly ordered by ascending sequence numbers (seq).
Every message sent creates an optimistic entry using a client-generated UUID (clientMessageId).
When the backend broadcasts the persisted message over /topic/conversations.{id} or /user/queue/acks, match and replace the optimistic item using clientMessageId.

Soft Deletions:

Deleted messages return:
json
{
  "id": "uuid",
  "deleted": true,
  "content": null
}
Render bubble as "This message was deleted" with an italicized trash icon.
Suppress reaction badges, replied quotes, and attachment links for deleted items.

Message Replies (replyTo):

Each message includes a non-recursive replyTo preview:
json
"replyTo": {
  "id": "uuid",
  "senderId": "uuid",
  "senderUsername": "alice",
  "senderDisplayName": "Alice Wonderland",
  "content": "Original message content snippet",
  "deleted": false
}
Render a compact quotation block inside the child bubble. Tapping the quote scrolls the viewport to the parent message.

Message Reactions:

Reaction data structure:
json
"reactions": [
  {
    "emoji": "👍",
    "count": 2,
    "users": [
      { "id": "uuid-1", "username": "alice", "displayName": "Alice" },
      { "id": "uuid-2", "username": "bob", "displayName": "Bob" }
    ]
  }
]
Clicking a pill triggers POST /api/v1/conversations/{id}/messages/{messageId}/reactions (or DELETE if already reacted).
Optimistic toggling updates the badge count and active highlight immediately.

Read Receipts:

Single check (✓): Sent and acknowledged by server.
Double check (✓✓ in white/primary): Read by recipient (targetMember.lastReadSeq >= msg.seq).
3.4 Attachment & Internal Storage File Flow

In the web app, all attachments are picked directly from internal device storage (never by URL link typing).

Content Format for Attachments:
If no caption: msg.content = "<url>"
If caption provided: msg.content = "<url>\n<caption text>"
When rendering:
Split (msg.content || '').split('\n').
First line is the media URL; subsequent lines are the user caption.
3.5 Administration Module (ROLE_ADMIN)

When the authenticated user has role ADMIN, the Admin navigation tab is exposed:

Dashboard Page: Real-time KPI summary (Total Users, Active Conversations, Open Reports) and activity breakdown.
User Management:
User table with cursor pagination and search filter.
Status transitions: ACTIVE ↔ DISABLED ↔ BANNED.
Force logout and account deletion.
Conversations Oversight:
Inspect all direct and group conversations.
Archive or soft-delete abusive channels.
Moderation & Reports:
Triage reported messages, users, or conversations.
Statuses: OPEN, INVESTIGATING, RESOLVED, DISMISSED.
Ability to delete reported messages directly from the report resolution view.
Audit Logs:
Paginated ledger recording all admin actions (actor, action, targetType, ipAddress, createdAt).
4. Flutter Project Blueprint & Code Templates
4.1 Recommended Flutter Packages (pubspec.yaml)
yaml
dependencies:
  flutter:
    sdk: flutter
  # State Management
  flutter_bloc: ^8.1.3   # or flutter_riverpod: ^2.5.1
  # Networking & HTTP
  dio: ^5.4.3+1
  flutter_secure_storage: ^9.0.0
  # Real-time WebSockets & STOMP
  stomp_dart_client: ^1.0.1
  # Media & File Picking (Internal Storage)
  image_picker: ^1.0.7
  file_picker: ^8.0.0
  cached_network_image: ^3.3.1
  open_filex: ^4.4.0
  # UI & Presentation
  lucide_icons: ^0.257.0   # Matches web Lucide icons exactly
  intl: ^0.19.0
  shimmer: ^3.0.0          # Matches web shimmer animation
4.2 Flutter Theme Configuration (theme.dart)
dart
import 'package:flutter/material.dart';
class LeoTheme {
  // Brand Colors
  static const Color primary = Color(0xFFF35A26);
  static const Color primaryForeground = Color(0xFFFFFFFF);
  static const Color primarySoft = Color(0xFFFEF3EE);
  static const Color destructive = Color(0xFFEF4444);
  static const Color onlineGreen = Color(0xFF10B981);
  // Neutral Colors (Light)
  static const Color bgLight = Color(0xFFF8FAFC);
  static const Color cardLight = Color(0xFFFFFFFF);
  static const Color slate950 = Color(0xFF020617);
  static const Color slate900 = Color(0xFF0F172A);
  static const Color slate800 = Color(0xFF1E293B);
  static const Color slate700 = Color(0xFF334155);
  static const Color slate600 = Color(0xFF475569);
  static const Color slate500 = Color(0xFF64748B);
  static const Color slate400 = Color(0xFF94A3B8);
  static const Color slate200 = Color(0xFFE2E8F0);
  static const Color slate100 = Color(0xFFF1F5F9);
  static ThemeData lightTheme() {
    return ThemeData(
      useMaterial3: true,
      fontFamily: 'Inter',
      scaffoldBackgroundColor: bgLight,
      colorScheme: const ColorScheme(
        brightness: Brightness.light,
        primary: primary,
        onPrimary: primaryForeground,
        secondary: slate800,
        onSecondary: Colors.white,
        error: destructive,
        onError: Colors.white,
        surface: cardLight,
        onSurface: slate900,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.white,
        foregroundColor: slate900,
        elevation: 0,
        scrolledUnderElevation: 0,
        titleTextStyle: TextStyle(
          fontFamily: 'Inter',
          fontSize: 18,
          fontWeight: FontWeight.w700,
          color: slate900,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: slate100,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide.none,
        ),
        hintStyle: const TextStyle(
          color: slate400,
          fontSize: 14,
          fontFamily: 'Inter',
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primary,
          foregroundColor: Colors.white,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          textStyle: const TextStyle(
            fontFamily: 'Inter',
            fontWeight: FontWeight.w600,
            fontSize: 14,
          ),
        ),
      ),
    );
  }
}
4.3 Chat Bubble Widget Blueprint (chat_bubble.dart)
dart
import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:open_filex/open_filex.dart';
class ChatBubble extends StatelessWidget {
  final String? content;
  final String type; // 'TEXT', 'IMAGE', 'FILE', 'SYSTEM'
  final bool isOwn;
  final bool isDeleted;
  final String? senderName;
  final String? timestamp;
  final bool isRead;
  final Map<String, dynamic>? replyTo;
  final List<dynamic> reactions;
  final Function(String emoji)? onReact;
  const ChatBubble({
    super.key,
    required this.content,
    required this.type,
    required this.isOwn,
    required this.isDeleted,
    this.senderName,
    this.timestamp,
    this.isRead = false,
    this.replyTo,
    this.reactions = const [],
    this.onReact,
  });
  @override
  Widget build(BuildContext context) {
    if (type == 'SYSTEM') {
      return Center(
        child: Container(
          margin: const EdgeInsets.symmetric(vertical: 8),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: const Color(0xFFF1F5F9),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(
            content ?? '',
            style: const TextStyle(fontSize: 11, color: Color(0xFF64748B)),
          ),
        ),
      );
    }
    final bubbleBg = isOwn ? const Color(0xFFF35A26) : const Color(0xFFF1F5F9);
    final textColor = isOwn ? Colors.white : const Color(0xFF0F172A);
    return Align(
      alignment: isOwn ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.78),
        child: Column(
          crossAxisAlignment: isOwn ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            // Author Name (for group chats)
            if (!isOwn && senderName != null)
              Padding(
                padding: const EdgeInsets.only(left: 4, bottom: 2),
                child: Text(
                  senderName!,
                  style: const TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF64748B),
                  ),
                ),
              ),
            // Message Bubble Surface
            Container(
              decoration: BoxDecoration(
                color: bubbleBg,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: isOwn ? const Radius.circular(16) : const Radius.circular(4),
                  bottomRight: isOwn ? const Radius.circular(4) : const Radius.circular(16),
                ),
              ),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Reply Quote Preview
                  if (replyTo != null && !isDeleted)
                    _buildReplyQuote(replyTo!),
                  // Body rendering
                  if (isDeleted)
                    const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.delete_outline, size: 14, color: Color(0xFF94A3B8)),
                        SizedBox(width: 4),
                        Text(
                          'This message was deleted',
                          style: TextStyle(
                            fontStyle: FontStyle.italic,
                            fontSize: 12,
                            color: Color(0xFF94A3B8),
                          ),
                        ),
                      ],
                    )
                  else if (type == 'IMAGE')
                    _buildImageMessage()
                  else if (type == 'FILE')
                    _buildFileMessage()
                  else
                    Text(
                      content ?? '',
                      style: TextStyle(fontSize: 14, color: textColor, height: 1.35),
                    ),
                  // Timestamp & Read Receipt Checkmark
                  if (!isDeleted)
                    Padding(
                      padding: const EdgeInsets.only(top: 4),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          if (timestamp != null)
                            Text(
                              timestamp!,
                              style: TextStyle(
                                fontSize: 10,
                                color: isOwn ? Colors.white70 : const Color(0xFF94A3B8),
                              ),
                            ),
                          if (isOwn) ...[
                            const SizedBox(width: 4),
                            Icon(
                              isRead ? Icons.done_all : Icons.done,
                              size: 13,
                              color: isRead ? Colors.white : Colors.white60,
                            ),
                          ],
                        ],
                      ),
                    ),
                ],
              ),
            ),
            // Reaction Badges
            if (reactions.isNotEmpty && !isDeleted)
              Padding(
                padding: const EdgeInsets.only(top: 3),
                child: Wrap(
                  spacing: 4,
                  children: reactions.map((r) {
                    return Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: const Color(0xFFE2E8F0)),
                      ),
                      child: Text(
                        '${r['emoji']} ${r['count']}',
                        style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
                      ),
                    );
                  }).toList(),
                ),
              ),
          ],
        ),
      ),
    );
  }
  Widget _buildReplyQuote(Map<String, dynamic> quote) {
    return Container(
      margin: const EdgeInsets.only(bottom: 6),
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: isOwn ? Colors.black.withOpacity(0.12) : const Color(0xFFE2E8F0),
        border: Border(
          left: BorderSide(
            color: isOwn ? Colors.white70 : const Color(0xFFF35A26),
            width: 2.5,
          ),
        ),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            quote['senderDisplayName'] ?? quote['senderUsername'] ?? 'User',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.bold,
              color: isOwn ? Colors.white : const Color(0xFF0F172A),
            ),
          ),
          Text(
            quote['content'] ?? '',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              fontSize: 11,
              color: isOwn ? Colors.white70 : const Color(0xFF475569),
            ),
          ),
        ],
      ),
    );
  }
  Widget _buildImageMessage() {
    final parts = (content ?? '').split('\n');
    final imageUrl = parts.first;
    final caption = parts.length > 1 ? parts.sublist(1).join('\n') : null;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: CachedNetworkImage(
            imageUrl: imageUrl,
            placeholder: (c, u) => Container(
              height: 180,
              color: Colors.black12,
              child: const Center(child: CircularProgressIndicator(strokeWidth: 2)),
            ),
            errorWidget: (c, u, e) => const Icon(Icons.broken_image, size: 40),
            fit: BoxFit.cover,
          ),
        ),
        if (caption != null && caption.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(top: 6),
            child: Text(
              caption,
              style: TextStyle(fontSize: 13, color: isOwn ? Colors.white : const Color(0xFF0F172A)),
            ),
          ),
      ],
    );
  }
  Widget _buildFileMessage() {
    final parts = (content ?? '').split('\n');
    final fileUrl = parts.first;
    final caption = parts.length > 1 ? parts.sublist(1).join('\n') : null;
    final cleanFileName = Uri.decodeFull(fileUrl.split('?').first.split('/').last);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        InkWell(
          onTap: () => OpenFilex.open(fileUrl),
          child: Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: isOwn ? Colors.white.withOpacity(0.18) : Colors.white,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: const Color(0xFFE2E8F0)),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.insert_drive_file, color: isOwn ? Colors.white : const Color(0xFFF35A26)),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    cleanFileName,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: isOwn ? Colors.white : const Color(0xFF0F172A),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        if (caption != null && caption.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(top: 4),
            child: Text(
              caption,
              style: TextStyle(fontSize: 13, color: isOwn ? Colors.white : const Color(0xFF0F172A)),
            ),
          ),
      ],
    );
  }
}
5. Flutter Project Folder Architecture

A scalable structure matching the web app's feature-driven architecture:

text
lib/
├── app/
│   ├── app.dart                   # MaterialApp router & theme setup
│   └── routes.dart                # GoRouter / Navigator 2.0 configuration
├── core/
│   ├── network/
│   │   ├── dio_client.dart        # Dio singleton + Bearer + 401 refresh interceptor
│   │   └── stomp_service.dart     # STOMP client over SockJS / WebSocket
│   ├── theme/
│   │   ├── colors.dart            # LeoTheme color definitions
│   │   └── theme.dart             # Light & Dark ThemeData
│   └── utils/
│       ├── date_formatter.dart    # relative & timestamp formatting
│       └── mime_helper.dart       # file extension to MIME mapping
├── features/
│   ├── auth/
│   │   ├── data/auth_repository.dart
│   │   ├── models/token_response.dart
│   │   └── presentation/          # Login, Register, Forgot Password screens
│   ├── chat/
│   │   ├── data/chat_repository.dart
│   │   ├── models/conversation.dart, message.dart, reaction.dart
│   │   └── presentation/
│   │       ├── chat_list_screen.dart       # Master conversation list & filter tabs
│   │       ├── conversation_screen.dart    # Detail chat room with bubbles & input bar
│   │       └── widgets/
│   │           ├── chat_bubble.dart        # Custom message bubble
│   │           ├── attachment_sheet.dart   # Internal storage photo & doc picker
│   │           └── typing_indicator.dart   # Bouncing dot indicator
│   ├── media/
│   │   └── data/media_service.dart         # Presign -> S3 upload -> Confirm flow
│   ├── settings/
│   │   └── presentation/screens/           # Profile, Security, Sessions
│   └── admin/                              # Visible only to ROLE_ADMIN
│       └── presentation/screens/           # Dashboard, Users, Reports, Audit Logs
└── main.dart
6. Summary Checklist for Flutter Development

When implementing the Flutter companion app, verify these key criteria against the web app:

 Exact Coral Palette: Use #F35A26 for primary CTAs and sender message bubbles.
 Internal Storage Picking Only: Use image_picker and file_picker to choose attachments from local storage. Do not provide URL text input fields.
 Presign-PUT-Confirm Flow: Ensure images and documents are uploaded directly to the pre-signed S3/Supabase upload URL with matching content-type headers before sending the chat message.
 STOMP Acks & Seq Resolution: Subscribe to /user/queue/acks and /topic/conversations.{id} to reconcile optimistic message IDs with real server sequences.
 Soft-Deleted Message Safety: Guard against content: null for deleted messages ("deleted": true).
 Reply Preview Extraction: Render parent quote previews from the replyTo DTO.
 Admin Tab Gate: Restrict the Admin UI tab strictly to accounts with user.role == 'ADMIN'.
