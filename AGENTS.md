# LeoChat Engineering Rules

## Architecture

- Use feature-first clean architecture.
- Keep presentation, domain, and data layers separate.
- Keep shared infrastructure in `core`; keep product behavior inside `features`.
- Use this ownership structure:

```text
lib/
  app/                         # App bootstrap, router, provider scope
  core/                        # Shared code only
    config/                    # Environment and API configuration
    cache/                     # Local cache contracts and implementation
    constants/                 # Shared strings, keys, and route names
    di/                        # Riverpod dependency graph
    errors/                    # AppFailure and error mapping
    network/                   # Dio and STOMP infrastructure
    responsive/                # Mobile, tablet, desktop breakpoints
    theme/                     # LeoChat colors and ThemeData
    utils/                     # Shared pure helpers
    widgets/                   # Reusable common widgets, grouped by purpose
      common_widgets/
        buttons/
        branding/
        feedback/
  features/
    auth/
      data/                    # Auth DTOs, data sources, repository
      domain/                  # Auth entities and repository contracts
      presentation/            # Auth providers, pages, widgets
    chat/                      # Conversations, messages, typing, reactions
    media/                     # Presign, upload, confirm flow
    settings/                  # Profile and security screens
    admin/                     # Admin-only dashboard and moderation
  main.dart                   # Flutter entry point only
```

- `data` depends on `domain` and `core`; `presentation` depends on `domain` and providers.
- `domain` must not import Flutter, Dio, Riverpod, or platform plugins.
- `core` must stay feature-agnostic; feature rules belong in the matching feature folder.
- Treat caching as a required feature. Cache conversation summaries and message pages locally, keyed by user and conversation ID.
- Use cache-first rendering with background refresh for lists; show cached data when offline and expose refresh errors without discarding it.
- Invalidate or merge cache entries after sends, edits, reactions, deletions, read receipts, logout, and account changes.
- Keep cache serialization in `core/cache`; repositories decide cache policy through injected cache providers.
- Never cache access or refresh tokens in ordinary app cache; tokens stay in secure storage.
- Widgets must not call Dio or parse JSON.
- Repositories expose domain models and `Either<AppFailure, T>` results.
- Keep DTOs in `data/models`, entities in `domain/entities`, and API sources in `data/datasources`.
- Use Riverpod providers for dependency injection and state management.
- Manage every external dependency through Riverpod providers in `lib/core/di`; do not instantiate Dio, storage, repositories, or services inside widgets.
- Features receive dependencies through provider overrides or `ref.watch`; avoid service locators and global mutable singletons.
- Keep shared user-facing strings, labels, errors, and route names organized under `lib/core/constants`.
- Do not scatter repeated literal strings through widgets; use typed constants or localization keys.
- Build repeated UI as reusable widgets. Check `core/widgets/common_widgets` before creating a new button, logo, input, card, loader, or feedback widget.
- Group common widgets by purpose, such as `buttons`, `branding`, `feedback`, and `inputs`.
- Feature-only widgets stay in that feature's `presentation/widgets`; do not place feature behavior in common widgets.
- Every screen section and repeated item should be its own focused widget when it improves readability or reuse.

## Networking

- Use the shared Dio client from `lib/core/network/providers.dart`.
- Attach Bearer tokens through the Dio interceptor.
- Map Dio failures to `AppFailure`; never expose raw Dio exceptions to widgets.
- Keep API paths and response parsing inside repositories or data sources.
- Preserve `clientMessageId`, `beforeSeq`, `afterSeq`, and `seq` semantics from `FLUTTER_API_GUIDE.md`.

## Realtime chat

- Keep STOMP connection logic in `core/network`.
- Subscribe before publishing.
- Reconnect with backoff and resubscribe after reconnect.
- Merge REST and WebSocket messages by ID, then sort by `seq`.
- Replace messages when edits, reactions, or deletion events arrive.

## UI and responsive behavior

- Follow `LEO_CHAT_FRONTEND_GUIDE.md` for colors, spacing, typography, and surfaces.
- Use Google Fonts `Inter` globally through `LeoTheme`; do not define unrelated font families inside feature widgets.
- Use `LeoColors` and `LeoTheme`; do not add one-off brand colors.
- Use the web design system only for visual language. Build app-native structure: app bars, bottom navigation, touch targets, sheets, drawers, stacked flows, and conversation-first screens.
- Do not copy web marketing layouts, desktop-only navigation, hover interactions, or dense website sections into mobile app flows.
- Design mobile first, then expand to tablet and desktop while preserving app semantics and touch-friendly controls.
- Support mobile, tablet, and desktop layouts through `core/responsive`.
- Mobile uses stacked navigation and full-screen conversations.
- Tablet and desktop use the navigation rail, conversation list, and detail view.

## Quality

- Keep async loading, error, and empty states explicit.
- Prefer small immutable models and widgets.
- Every new or changed feature must include unit tests for domain, repository, and state logic.
- Every new or changed screen or reusable widget must include widget tests for rendering and user interaction.
- Test success, loading, empty, error, responsive, and permission states where applicable.
- Run unit and widget tests with `flutter test` before committing.
- Run `dart format`, `flutter analyze`, and relevant tests before committing.
