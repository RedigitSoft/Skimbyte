# Architecture

## Modules

```
shared/       all app code: commonMain + android/ios/jvm/js/wasmJs actuals, all test source sets
androidApp/   Android entry point
iosApp/       Xcode project (iOS entry point)
desktopApp/   Desktop (JVM) entry point
webApp/       Web entry point (JS + Wasm)
designsheet/  runnable component catalog / living style guide (desktop + web)
```

Platform-specific behavior uses Kotlin's `expect`/`actual` mechanism — every `expect` declaration
in `commonMain` has an `actual` in each of the five platform source sets (for example `openUrl`,
the image picker, and `authRedirectUri`).

## State: MVI-flavored ViewModels

Every screen is driven by a ViewModel exposing an immutable `StateFlow<UiState>`; composables are
stateless and receive `(state, onIntent)`. Intents are sealed interfaces (`FeedIntent`,
`AccountIntent`, …); one-shot events use a separate effects `SharedFlow`.

## Navigation

[Navigation 3](https://developer.android.com/guide/navigation/navigation-3) (`androidx.navigation3`),
not Navigation Compose. Screens adapt to window size with material3-adaptive and
`ListDetailSceneStrategy` rather than hardcoded breakpoints, so foldables and arbitrary desktop
window sizes work everywhere.

## Dependency injection

Koin, across all five targets — a single `appModule` plus per-concern modules. When no backend is
configured, the network module is swapped for a demo module at startup
(`ApiConfig.DEMO_MODE`), which is what makes a fresh clone runnable.

## Networking & data

- Ktor client + kotlinx.serialization for the posts/bookmarks API.
- A separate authenticated client for the auth server (token refresh is single-flighted through
  `RefreshCoordinator`, because refresh tokens are single-use and rotation-detected server-side).
- Coil 3 for images, with an explicitly registered Ktor network fetcher.

## Auth

End-user sessions are issued by a separate closed-source auth server and carried as bearer JWTs;
the client stores an access/refresh token pair. OAuth sign-in (Google, GitHub) is server-driven:
the client opens the server's `/start` URL in a browser and receives tokens back on a
platform-appropriate redirect — a custom-scheme deep link on Android/iOS, an RFC 8252 loopback
listener on desktop.

## Configuration

Endpoints live in gitignored `local.properties` and are generated into `BackendConfig.kt` at
build time — committed source contains no environment URLs. See `local.properties.example` for
every key.
