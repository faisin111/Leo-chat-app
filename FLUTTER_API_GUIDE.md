# Flutter Backend API Guide

## WebSocket client

```dart
callback: (frame) => onMessage(jsonDecode(frame.body!)),
);
client.subscribe(
  destination: '/topic/conversations.$conversationId.typing',
  callback: (frame) => onTyping(jsonDecode(frame.body!)),
);
client.subscribe(
  destination: '/user/queue/errors',
  callback: (frame) => onApiError(jsonDecode(frame.body!)),
);
},
),
);
client.activate();
```

For Bearer WebSocket clients, pass the token in STOMP `connectHeaders`. For cookie clients, use the cookie-aware transport. Subscribe before sending messages. Reconnect with backoff and resubscribe after reconnect.

## Message response

```json
{
  "id": "uuid",
  "conversationId": "uuid",
  "senderId": "uuid",
  "senderUsername": "alice",
  "senderDisplayName": "Alice",
  "seq": 12,
  "clientMessageId": "mobile-123",
  "type": "TEXT",
  "content": "Hello",
  "replyToId": null,
  "replyTo": null,
  "reactions": [
    {
      "emoji": "👍",
      "count": 2,
      "users": [
        { "id": "uuid", "username": "alice", "displayName": "Alice" }
      ]
    }
  ],
  "createdAt": "2026-10-17T10:20:30Z",
  "editedAt": null,
  "deletedAt": null,
  "deleted": false
}
```

Deleted messages keep their ID and sequence, set `deleted` to `true`, and set `content` to `null`. A reply preview has `id`, `senderId`, `senderUsername`, `senderDisplayName`, `content`, and `deleted`.

## Flutter data-layer rules

- Use repositories: `AuthRepository`, `UserRepository`, `ConversationRepository`, `MessageRepository`, `MediaRepository`, and `AdminRepository`.
- Keep DTOs separate from widgets. Parse UUIDs as strings or `UuidValue`; parse timestamps with `DateTime.parse`.
- Use `clientMessageId` for retry-safe sends. Do not create duplicate messages when a request times out; retry the same ID.
- Load older messages with `beforeSeq`; sync new messages with `afterSeq`.
- Merge REST history and WebSocket messages by message ID, then order by `seq`.
- Treat WebSocket events as updates: replace the message when reactions, edits, or deletion change.
- URL-encode reaction emoji in `DELETE /messages/{id}/reactions/{emoji}`.

## Media upload flow

1. Call `POST /media/presign` with MIME type and byte size.
2. Upload bytes with `PUT` to the returned `uploadUrl`, using exactly the returned `Content-Type`.
3. Call `POST /media/confirm` with `storageKey`.
4. Save the returned URL only where the API expects a profile or attachment URL. Presigned URLs expire; request a new URL when needed.

The maximum file size is 10 MiB. Unsupported types return `422 BUSINESS_RULE`.

## Environment and deployment

Production requires HTTPS/WSS, configured CORS for the Flutter web origin if using Flutter Web, Supabase storage credentials for media, and a TURN server for WebRTC calls. Render must deploy the latest Git commit. The backend listens on the `PORT` environment variable.

## Response shapes

Empty success responses have no JSON body. Do not call `response.json()` for `204` or for action routes documented as empty.

## Admin response details

Admin routes use the same error contract. They require a user whose role is `ROLE_ADMIN`.

`GET /admin/stats/overview` returns:

```json
{ "totalUsers": 42, "totalConversations": 18, "totalReports": 3 }
```

`GET /admin/users`, `GET /admin/conversations`, `GET /admin/reports`, and `GET /admin/audit-logs` return the cursor page shape. User items contain `id`, `username`, `email`, `displayName`, `role`, and `status`. Conversation items contain `id`, `type`, `title`, `status`, `lastSeq`, `lastMessageAt`, and `createdAt`. Report items contain `id`, `reporterId`, `targetType`, `targetId`, `reason`, `details`, `status`, `createdAt`, and optional resolution fields. Audit items contain the actor, action, target, metadata, and `createdAt`.

Admin mutations use these request and response forms:

```http
PATCH /api/v1/admin/users/{id}/status
{ "status": "DISABLED", "reason": "Policy violation" }
```

```http
PATCH /api/v1/admin/conversations/{id}/status
{ "status": "ARCHIVED", "reason": "Moderation complete" }
```

```http
PATCH /api/v1/admin/reports/{id}
{ "resolution": "Confirmed", "action": "DELETE_MESSAGE" }
```

```http
DELETE /api/v1/admin/messages/{id}?reason=Spam%20content
```

Each returns `200` with `{ "message": "..." }`. `POST /admin/users/{id}/force-logout` returns `422` because JWTs are fully stateless. `POST /admin/transfer-ownership` accepts `{ "targetUserId": "uuid", "password": "current-admin-password" }` and returns a success message.

Admin status values: user `ACTIVE|DISABLED|BANNED`; conversation `ACTIVE|ARCHIVED|DELETED`; report `OPEN|INVESTIGATING|RESOLVED|DISMISSED`.
