  # CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Skimbyte is a Kotlin Multiplatform + Compose Multiplatform project targeting Android, iOS, Desktop (JVM), and Web (JS and Wasm). It is currently a freshly scaffolded template (`io.redigit.skimbyte` package) with no app-specific features implemented yet.

This is an open-source reference project — the bar is "code high-end developers learn from," not "code that just works." Before writing any feature code, read **CONTRIBUTING.md** in full; it is the source of truth for architecture, Compose conventions, testing requirements, and the `designsheet` module. The non-negotiables, repeated here because they apply to every single change:

- Every composable that renders UI needs at least one `@Preview` (light + dark minimum).
- Every composable is stateless where possible (state hoisted to a ViewModel exposing immutable `StateFlow<UiState>`); no hardcoded colors/dimens/text styles — use `MaterialTheme` and shared design tokens.
- Every piece of logic gets a unit test; every interactive/stateful composable gets a Compose UI test; shared design-system components get Roborazzi screenshot tests.
- DI is via Koin, across all targets — don't introduce a platform-specific DI framework.
- Any new or changed shared component/token must be reflected in the `designsheet` module (the component catalog / living style guide) in the same change.
- Always target the latest stable versions pinned in `gradle/libs.versions.toml`; don't add dependencies outside the version catalog.

## Modules

- `shared/` — code shared across all platforms, organized by Kotlin source set:
  - `commonMain` — shared Compose UI and business logic (e.g. `App.kt`, `Platform.kt`).
  - `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain` — platform-specific implementations using Kotlin's `expect`/`actual` mechanism (see `Platform.kt` and its `Platform.<platform>.kt` counterparts).
  - Corresponding test source sets: `commonTest`, `androidHostTest`, `iosTest`, `jvmTest`, `webTest`.
- `androidApp/` — Android application entry point (`MainActivity.kt`), depends on `:shared`.
- `desktopApp/` — JVM desktop application entry point (`main.kt`), depends on `:shared`.
- `webApp/` — Web application entry point, builds for both `js` and `wasmJs` targets, depends on `:shared`.
- `iosApp/` — Xcode project; the actual iOS entry point. Even though UI is shared via Compose Multiplatform, this project is required to build/run on iOS and is where SwiftUI code would go. Must be opened/run in Xcode, not Gradle.

When adding a new `expect` declaration in `commonMain`, you must add a matching `actual` implementation in every platform source set (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`) or the corresponding target will fail to compile.

## Common commands

Run all commands from the repo root using the Gradle wrapper (`./gradlew`).

### Build / run apps

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app (standard): `./gradlew :desktopApp:run`
- Desktop app (hot reload): `./gradlew :desktopApp:hotRun --auto`
- Web app, Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- Web app, JS target (slower, supports older browsers): `./gradlew :webApp:jsBrowserDevelopmentRun`
- iOS app: open `iosApp/` in Xcode and run from there (not buildable via Gradle alone).

### Tests

Shared logic tests live in `shared/src/*Test` and are run per-platform:

- Android: `./gradlew :shared:testAndroidHostTest`
- Desktop (JVM): `./gradlew :shared:jvmTest`
- Web (Wasm): `./gradlew :shared:wasmJsTest`
- Web (JS): `./gradlew :shared:jsTest`
- iOS: `./gradlew :shared:iosSimulatorArm64Test`

To run a single test class/method, append Gradle's `--tests` filter, e.g.:
`./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.SharedLogicDesktopTest"`

## Dependency management

Dependency versions and library coordinates are centralized in `gradle/libs.versions.toml` (Gradle version catalog) and referenced via `libs.*` accessors in each module's `build.gradle.kts`. Add new dependencies there rather than hardcoding coordinates in individual build files.