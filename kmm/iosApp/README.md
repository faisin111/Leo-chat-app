# iOS host

The iOS app is a thin SwiftUI shell around the shared Compose Multiplatform UI.
All screens, navigation, DI, and storage live in `shared/`.

Setup on macOS with Xcode:

1. Build the framework once: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` (or let the Xcode build phase below do it).
2. Create an iOS App target in Xcode and add `LeoChatApp.swift`, `ContentView.swift`, and `Info.plist`.
3. Add a Run Script build phase before "Compile Sources":
   ```sh
   cd "$SRCROOT/.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
4. Under Build Settings set `FRAMEWORK_SEARCH_PATHS` to
   `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)` and add `-framework Shared` to `OTHER_LDFLAGS`.

`ContentView` calls `MainViewControllerKt.MainViewController()`, which starts Koin once and renders `LeoChatApp()`.
Tokens are stored in the Keychain (`IosSecureStorage`); preferences and cache in `NSUserDefaults`.

iOS targets cannot be compiled on Windows; build and verify on a Mac.
