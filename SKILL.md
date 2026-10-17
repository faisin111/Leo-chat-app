# LeoChat Project Skill

Use this file as the implementation contract for future LeoChat work.

## Build style

Build a modern, smooth realtime chat client. Favor predictable state transitions, small rebuild scopes, optimistic interaction with server reconciliation, and clear loading/error/empty states.

Every implementation change must ship with tests. Add unit tests for models, parsers, repositories, failure mapping, and Riverpod state. Add widget tests for every changed screen or reusable widget, including user interaction and responsive layouts. Cover success, loading, empty, error, and permission states when they apply. Run `flutter test` before committing.

Use meaningful commit messages. Each message must clearly state the complete change in imperative form, such as `Add username login screen`, `Add registration flow`, or `Fix Android Java 21 build compatibility`. Avoid vague messages like `changes`, `update`, or `fix stuff`.

## Required stack

- Flutter Material 3.
- Riverpod for state management and dependency injection.
- Dio for HTTP and interceptors.
- `fpdart` for typed `Either<AppFailure, T>` results.
- `go_router` for navigation.
- `flutter_secure_storage` for access and refresh tokens.
- `google_fonts` with Poppins for the global LeoChat type system.

Dependency injection is mandatory. Register Dio, secure storage, WebSocket services, repositories, and feature controllers through Riverpod providers in `lib/core/di`. Widgets and repositories must consume injected dependencies; they must not construct infrastructure objects directly.

## Folder rules

```text
lib/
  app/
  core/
    cache/
    config/
    constants/
    di/
    errors/
    network/
    responsive/
    theme/
    widgets/
      common_widgets/
        buttons/
        branding/
        feedback/
  features/
    auth/
    chat/
    media/
    settings/
    admin/
```

Each feature may contain `data`, `domain`, and `presentation`. Use `domain/usecases` for application actions, `domain/repositories` for contracts, `data/repositories` for implementations, `data/models` for DTOs, `data/datasources` for transport, and `presentation/controllers`, `presentation/providers`, and `presentation/states` for Riverpod-driven UI state. Presentation reads state from providers. Data sources own Dio calls. Repositories translate DTOs into domain entities.

Architecture boundaries are strict: `main.dart` only boots the app; `app/` owns routing and composition; `core/` owns reusable infrastructure; `features/` own business behavior. Data sources handle transport, repositories handle translation and orchestration, domain contains framework-free contracts and entities, and presentation contains Riverpod controllers, pages, and widgets.

Caching is part of the app architecture. Use a local cache for conversation lists and message pages. Read cached data immediately, refresh from the API in the background, merge WebSocket updates into cache, and preserve cached data when refresh fails. Cache keys must include the authenticated user and conversation ID. Clear user-scoped cache on logout.

Keep shared strings in `lib/core/constants`. Use `AppStrings` or localization keys for repeated labels, messages, errors, and route names. Do not hardcode repeated user-facing strings inside feature widgets.

Keep visual tokens organized under `lib/core/theme`: `colors.dart` for color tokens, `typography.dart` for the Google Fonts Poppins text scale, and `fields.dart` for shared input decoration and field geometry. Refactoring theme organization must preserve the existing LeoChat appearance; do not introduce one-off colors, text styles, or field shapes in feature widgets.

Use bundled assets for LeoChat branding and core icons. Keep the app identity local under `assets/icons`; do not load the logo or core app icons from a network URL.

All scrollable pages use the shared smooth scroll behavior and all app routes use the shared animated transition. New scroll views and routes must follow these defaults instead of adding one-off physics or transitions.

Keep startup routing auth-aware: always show the three-second splash, then skip onboarding and login when a secure access token exists.

Reusable widgets are mandatory. Before adding a widget, check `lib/core/widgets/common_widgets`. Shared buttons, branding, inputs, cards, loaders, and feedback components belong there and must be reused across modules. Feature-specific widgets remain inside their feature module. Do not duplicate equivalent widgets with different names.

Keep assets organized under `assets/images`, `assets/icons`, `assets/illustrations`, `assets/avatars`, and `assets/fonts`. Register each folder in `pubspec.yaml` and reuse assets through shared constants or helpers.

## Responsive rules

- `< 600dp`: mobile list and full-screen conversation routes.
- `600–1023dp`: tablet navigation rail plus narrower conversation sidebar.
- `>= 1024dp`: desktop navigation rail, 340dp conversation sidebar, flexible chat detail, optional profile drawer.

## LeoChat visual contract

Use coral `#F35A26`, slate neutrals, Poppins typography, white surfaces, rounded 12–16px controls, 24px sheets, and the message bubble geometry defined in `LEO_CHAT_FRONTEND_GUIDE.md`.

The visual system comes from the web reference, but the product structure must feel like a polished native app. Use app bars, bottom navigation, stacked mobile flows, modal sheets, drawers, large touch targets, and conversation-first navigation. Avoid treating the Flutter client as a website or copying web-only hover and marketing patterns.

## Realtime contract

Use optimistic `clientMessageId` values, server ACK reconciliation, sequence ordering, typing events, read receipts, reactions, reply previews, soft deletion, and reconnect/resubscribe behavior from the API guides.
