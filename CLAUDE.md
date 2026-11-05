  # CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Skimbyte is a Kotlin Multiplatform + Compose Multiplatform project (`io.redigit.skimbyte` package) targeting Android, iOS, Desktop (JVM), and Web (JS and Wasm). It turns developer-news feeds into a daily, skimmable brief.

The app is built out, not a scaffold: onboarding/topic selection, the feed, post detail, bookmarks, account (Google/GitHub OAuth, avatar & banner upload), and settings all ship, with a `designsheet` component catalog alongside. Code lives almost entirely in `shared/commonMain`. A fresh clone runs in **demo mode** on bundled sample data when no backend URL is configured — Koin swaps in `network/DemoNetworkModule.kt` — so every target runs end to end without the (closed-source) backend.

This is an open-source reference project — the bar is "code high-end developers learn from," not "code that just works." Before writing any feature code, read **CONTRIBUTING.md** in full; it is the source of truth for architecture, Compose conventions, testing requirements, and the `designsheet` module. The non-negotiables, repeated here because they apply to every single change:

- Every composable that renders UI needs at least one `@Preview` (light + dark minimum).
- Every composable is stateless where possible (state hoisted to a ViewModel exposing immutable `StateFlow<UiState>`); no hardcoded colors/dimens/text styles — use `MaterialTheme` and shared design tokens.
- Every piece of logic gets a unit test; every interactive/stateful composable gets a Compose UI test; shared design-system components get Roborazzi screenshot tests.
- DI is via Koin, across all targets — don't introduce a platform-specific DI framework.
- Any new or changed shared component/token must be reflected in the `designsheet` module (the component catalog / living style guide) in the same change.
- Always target the latest stable versions pinned in `gradle/libs.versions.toml`; don't add dependencies outside the version catalog.
- Every screen must support foldable devices and resizable/arbitrary window sizes (material3-adaptive + Navigation 3's `ListDetailSceneStrategy`, not hardcoded breakpoints). Never nest a lazy/scrollable composable inside another scrollable in the same orientation — it crashes with an infinite-constraint exception; each piece of content owns its own scrolling.
- Navigation is Navigation 3 (`androidx.navigation3`), not Navigation Compose.

## Modules

- `shared/` — code shared across all platforms, organized by Kotlin source set:
  - `commonMain` — shared Compose UI and business logic (e.g. `App.kt`, `Platform.kt`).
  - `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain` — platform-specific implementations using Kotlin's `expect`/`actual` mechanism (see `Platform.kt` and its `Platform.<platform>.kt` counterparts).
  - Corresponding test source sets: `commonTest`, `androidHostTest`, `iosTest`, `jvmTest`, `webTest`.
- `androidApp/` — Android application entry point (`MainActivity.kt`), depends on `:shared`.
- `desktopApp/` — JVM desktop application entry point (`main.kt`), depends on `:shared`.
- `webApp/` — Web application entry point, builds for both `js` and `wasmJs` targets, depends on `:shared`.
- `designsheet/` — standalone, runnable component catalog / living style guide for the shared design system (tokens + every reusable component), navigable via Navigation 3 with an adaptive list-detail layout. Builds for `jvm` (desktop) and `js`/`wasmJs` (web); depends on `:shared`. See CONTRIBUTING.md for what belongs here.
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
- Design sheet (desktop): `./gradlew :designsheet:run`
- Design sheet (web, Wasm): `./gradlew :designsheet:wasmJsBrowserDevelopmentRun`

### Tests

Shared logic tests live in `shared/src/*Test` and are run per-platform:

- Android: `./gradlew :shared:testAndroidHostTest`
- Desktop (JVM): `./gradlew :shared:jvmTest`
- Web (Wasm): `./gradlew :shared:wasmJsTest`
- Web (JS): `./gradlew :shared:jsTest`
- iOS: `./gradlew :shared:iosSimulatorArm64Test`

To run a single test class/method, append Gradle's `--tests` filter, e.g.:
`./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.SharedLogicDesktopTest"`

Roborazzi screenshot tests run as part of `:shared:testAndroidHostTest`. Golden images live in `shared/src/androidHostTest/roborazzi/` (committed, not in `build/`). Regenerate with `./gradlew :shared:recordRoborazziAndroidHostTest`; check without updating with `./gradlew :shared:verifyRoborazziAndroidHostTest`.

## Dependency management

Dependency versions and library coordinates are centralized in `gradle/libs.versions.toml` (Gradle version catalog) and referenced via `libs.*` accessors in each module's `build.gradle.kts`. Add new dependencies there rather than hardcoding coordinates in individual build files.