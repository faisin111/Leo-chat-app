# LeoChat — Kotlin Multiplatform

Compose Multiplatform app (Android + iOS) that is replacing the Flutter client.
The Flutter app in the repository root is the reference for design, flows, and API behavior.

## Modules

- `shared/` — all UI, navigation, DI, and logic (Compose Multiplatform, Ktor, Koin, Coil).
- `androidApp/` — Android host: `LeoChatApplication` starts Koin, `MainActivity` renders `LeoChatApp()`.
- `iosApp/` — SwiftUI host around `MainViewController()`; see `iosApp/README.md`.

## Source layout (`shared/src/commonMain/kotlin/com/leochat`)

```text
app/                      LeoChatApp root, AppModules (Koin), navigation/ (NavHost, transitions)
core/
  cache/                  CacheStore + per-user cache keys
  config/                 AppConfig (API + WebSocket URLs)
  constants/              AppStrings, AppRoutes, StorageKeys
  di/                     coreModule + expect platformModule
  errors/                 AppFailure, Either / AppResult
  network/                Ktor client (Bearer + refresh-on-401), safeApiCall, stomp/
  responsive/             Mobile / tablet / desktop breakpoints
  session/                TokenStore, SessionEvents
  storage/                SecureStorage, KeyValueStorage contracts
  theme/                  LeoColors, LeoTypography (Poppins), LeoFields, LeoTheme
  utils/                  ClientIds
  widgets/common/         buttons/, branding/, inputs/, feedback/, layout/
features/<feature>/
  data/                   datasources/, models/ (DTOs), repositories/ (impl)
  domain/                 entities/, repositories/ (contracts), usecases/
  presentation/           controllers/ (ViewModels), states/, providers/ (Koin), pages/, widgets/
```

Platform code lives in `androidMain` (EncryptedSharedPreferences, SharedPreferences, OkHttp)
and `iosMain` (Keychain, NSUserDefaults, Darwin, `MainViewController`).

## Migration status

| Area | Status |
| --- | --- |
| Core (theme, network, STOMP, storage, cache, DI, widgets) | Done |
| Splash, onboarding, sign in, register, forgot/reset password | Done |
| Chat shell, conversations, messages, groups, shared media | Placeholder `/app` screen |
| Leo AI, calls UI + WebRTC, settings, admin | Not started (call state machine kept) |

## Build and test (Windows)

The JDK needs a short temp dir for its loopback pipe, so set it before running Gradle:

```bash
export TMP='D:\tmp\gradle-tmp' TEMP='D:\tmp\gradle-tmp' GRADLE_OPTS='-Djdk.net.unixdomain.tmpdir=D:\tmp\gradle-tmp'
```

```bash
./gradlew :shared:testDebugUnitTest
```

```bash
./gradlew :androidApp:assembleDebug
```
