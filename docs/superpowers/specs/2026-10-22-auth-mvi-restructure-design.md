# Auth feature: layer-based restructure + MVI pilot

## Intent and why

The `shared` module is currently organized by feature (`auth/`, `feed/`, `bookmarks/`,
`onboarding/`, `settings/`), with each feature further split into a top-level
package (data/domain) and a nested `ui/` package (screens/ViewModels). In practice
this has broken down worst inside `auth/`: eight unrelated things — an API client,
a Koin module, two repositories, an OAuth redirect bridge, a domain model, a
refresh-race coordinator, and a session-storage abstraction — all sit loose in one
flat package, indistinguishable at a glance from each other or from the screens in
`auth/ui/`. The stated pain is concrete: when something needs to change, it's hard
to tell by filename or location whether a file is "what's on screen" or "where the
behavior/data lives."

The fix has two parts:

1. **Layer-first package structure** (`ui/`, `data/`, `di/` at the top of
   `commonMain`, with feature subfolders underneath `ui/screens/` and
   `data/repositories/`), so the *kind* of file is obvious from its path before you
   even open it.
2. **MVI instead of direct-mutation MVVM** for ViewModels, so a screen's state only
   ever changes through one explicit path (`Intent` in, `State` out, `Effect` for
   one-off events like navigation), instead of being mutated ad hoc from scattered
   public functions.

This project pilots both changes on the `auth` feature only (`AccountScreen` +
`LoginSheet` + the image viewer), plus the generic `network/` infrastructure that
`auth` depends on. The other five features (`feed`, `bookmarks`, `settings`,
`onboarding`, `search`) are **not touched** in this pass — they keep their current
package layout and MVVM style until a follow-up project rolls the same template out
to each of them individually.

## Goals

- Establish the new top-level package skeleton (`ui/screens`, `ui/components`,
  `ui/navigation`, `data/local`, `data/remote`, `data/repositories`,
  `data/models` (incl. `data/models/dto`), `di/modules`) under
  `shared/src/commonMain/kotlin/io/redigit/skimbyte/`.
- Move every `auth/*` and `auth/ui/*` file into that skeleton, split along
  ui/data/di lines.
- Move the parts of `network/*` that `auth`'s DI module actually depends on
  (`ApiConfig`, `Page`, `NetworkModule`'s `createHttpClient`/`configureSkimbyteClient`,
  and their DTOs) into the same skeleton. `DemoNetworkModule.kt` does **not** move
  in this pass — it imports `posts.dto.PostDto` and seeds demo data for the
  `posts`/`feed`/`bookmarks` routes, none of which are in scope here; moving it
  would either pull a `posts`-feature file into the new generic `data/remote/`
  layer or require splitting it along lines that belong to the pass-2 migration,
  not this one. It stays in the old `network/` package untouched until `posts`/
  `feed` get their own pass. Other features (`posts`, `feed`) import
  `ApiConfig`/`Page` too — moving those two is a mechanical import-path fix for
  them, not a behavioral change.
- Regenerate the `BackendConfig.kt` file that `shared/build.gradle.kts`'s
  `generateAuthConfig` Gradle task writes to `io/redigit/skimbyte/auth/` (package
  `io.redigit.skimbyte.auth`) — update that task to write into `data/remote/`
  (package `io.redigit.skimbyte.data.remote`) instead, since `ApiConfig` (which
  imports `BackendConfig`) is moving there, and the old `auth` package is being
  deleted. `LoginSheet.kt` and `androidApp`'s `MainActivity.kt` both import
  `BackendConfig` directly and need their imports updated too.
- Convert `AccountViewModel` and `LoginSheetViewModel` to MVI: each gets an
  `Intent` sealed interface, an immutable `State` data class, a `SideEffect` sealed
  interface for one-off events, a single `fun onIntent(intent: ...)` entry point,
  and a `SharedFlow<SideEffect>` screens collect once for navigation/snackbar
  triggers — replacing today's `onSuccess: () -> Unit` callback parameters threaded
  through from the NavHost.
- Extract `AvatarWithEditBadge` and `BannerImage` into `ui/components/` as reusable
  composables (they're already design-token-driven and used across more than one
  screen), leaving `ui/screens/auth/` with only screen-specific composables
  (`AccountScreen`, `LoginSheet`, `ImageViewerScreen`, their NavHost).
- Keep every existing test passing (after relocation + MVI-driven rewrites) and
  every CONTRIBUTING.md requirement intact: unit test per ViewModel, Compose UI
  test per screen, Roborazzi screenshot tests for the extracted `ui/components`
  (regenerated under their new fully-qualified class names — Roborazzi keys
  goldens by qualified test class name, so moving a screenshot test's package
  requires `recordRoborazziAndroidHostTest`, not a file rename).
- Apply SOLID where it's directly applicable: single-responsibility is the main
  driver of the ui/data split itself; dependency inversion shows up in ViewModels
  depending on repository *interfaces* (already true today, preserved); the others
  (open/closed, Liskov, interface segregation) aren't expected to require new
  abstractions here — call out in review if a specific change needs one, rather
  than adding interfaces speculatively.

## Non-goals

- Converting `feed`, `bookmarks`, `settings`, `onboarding`, or `search` to the new
  structure or to MVI. They stay exactly as they are.
- Fixing the logout → re-login → stuck navigation bug reported earlier. That bug
  hasn't been root-caused yet, and this restructure is not guaranteed to fix it —
  though replacing `LoginSheetViewModel`'s `onSuccess` callback params with an
  explicit `SideEffect` the NavHost reacts to may make that class of bug easier to
  reason about. The bug gets its own investigation, separately, after this lands.
- Introducing a new DI framework, a different navigation library, or any new
  third-party dependency. Koin, Navigation 3, and the existing `libs.versions.toml`
  entries are unchanged.
- Changing any user-visible behavior of the account or login screens. This is a
  structural/architectural change; screenshots and interaction tests should assert
  the same behavior as before, just through the new state-shape.

## Anti-goals

- Ending up with a `data/` or `ui/` folder that's just as undifferentiated as the
  old flat `auth/` package, just bigger (e.g. dumping every feature's repositories
  into one unsorted `data/repositories/` with no sub-grouping as more features
  migrate in later passes). For this pass, with only `auth`'s repositories moving
  in, this isn't yet a problem, but the follow-up passes should keep files grouped
  by feature within each layer folder (e.g. `data/repositories/auth/`,
  `data/repositories/feed/` if/when `data/repositories/` gets crowded) rather than
  flattening indefinitely.
- An MVI implementation that's MVI in name only — e.g. a `ViewModel` that still
  exposes public mutator functions alongside `onIntent`. Every state change must
  go through `onIntent`.

## Constraints

- KMP `expect`/`actual` rules still apply: nothing here introduces a new `expect`
  declaration, but any file that moves must keep compiling on every target
  (Android, iOS, JVM, JS, WasmJS).
- Koin DI must still work across all targets — only the *location* of `module { }`
  blocks changes (into `di/modules/`), not the DI mechanism.
- CONTRIBUTING.md's non-negotiables remain in force: every composable keeps its
  `@Preview` (light + dark), stateless composables stay stateless, every piece of
  logic keeps a unit test, every interactive composable keeps a Compose UI test,
  moved design-system components keep Roborazzi coverage and stay reflected in
  `designsheet` if `designsheet` references them.
- Navigation 3 (`androidx.navigation3`) stays the navigation mechanism; this
  project does not touch `ListDetailSceneStrategy` usage or introduce Navigation
  Compose.

## Decided structure

```
shared/src/commonMain/kotlin/io/redigit/skimbyte/
  ui/
    screens/
      auth/
        AccountScreen.kt
        AccountNavHost.kt
        AccountState.kt        (was AccountUiState.kt)
        AccountIntent.kt       (new)
        AccountEffect.kt       (new)
        AccountViewModel.kt
        LoginSheet.kt
        LoginSheetState.kt     (was LoginSheetUiState.kt)
        LoginSheetIntent.kt    (new)
        LoginSheetEffect.kt    (new)
        LoginSheetViewModel.kt
        ImageViewerScreen.kt
    components/
      AvatarWithEditBadge.kt
      BannerImage.kt
    navigation/
      (top-level nav graphs live here once they move; auth has none today —
       AccountNavHost is screen-local and stays under ui/screens/auth)
  data/
    local/
      SessionStorage.kt        (interface + SettingsSessionStorage impl)
    remote/
      AuthApi.kt
      RefreshCoordinator.kt
      GoogleAuthCallback.kt
      SkimbyteHttpClient.kt    (createHttpClient/configureSkimbyteClient,
                                 extracted out of NetworkModule.kt)
      ApiConfig.kt
      Page.kt
      BackendConfig.kt         (generated by generateAuthConfig; see Goals)
    repositories/
      AuthRepository.kt
      AuthRepositoryImpl.kt
      ProfileRepository.kt
    models/
      Profile.kt
      dto/
        LoginRequestDto.kt
        LoginResponseDto.kt
        MeResponseDto.kt
        RefreshRequestDto.kt
        RegisterRequestDto.kt
        ErrorDto.kt
        PageDto.kt
  di/
    modules/
      AuthModule.kt            (Koin module { } block only)
      NetworkModule.kt         (Koin module { } block only)
```

Old `auth/` is deleted entirely, and the `auth`-coupled parts of `network/`
(`ApiConfig.kt`, `Page.kt`, `NetworkModule.kt`, `BackendConfig.kt`, and
`network/dto/PageDto.kt` and `ErrorDto.kt`) are deleted from `network/` once every
reference is updated — no compatibility shims, no re-exports. `network/`
survives this pass as a smaller package containing only `DemoNetworkModule.kt`,
which stays in place (it's `posts`-feature-coupled, not `auth`-coupled — see
Goals) but needs two import lines updated: `createHttpClient` now resolves from
`data/remote`, and `PageDto` now resolves from `data/models/dto`. `network/` is
fully absorbed into the new skeleton in pass 2, when `posts`/`feed` migrate.

### MVI contract shape (applies to both AccountViewModel and LoginSheetViewModel)

```kotlin
sealed interface AccountIntent {
    data object Logout : AccountIntent
    data class AvatarPicked(val image: PickedImage) : AccountIntent
    data class BannerPicked(val image: PickedImage) : AccountIntent
}

sealed interface AccountEffect {
    // populated as needed; Account has none identified yet — Logout is fully
    // observed via AuthRepository.session elsewhere, no navigation effect needed.
}

data class AccountState(
    val isLoggedIn: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val avatarUploadInProgress: Boolean = false,
    val bannerUploadInProgress: Boolean = false,
    val avatarError: String? = null,
    val bannerError: String? = null,
)

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AccountEffect>()
    val effects: SharedFlow<AccountEffect> = _effects.asSharedFlow()

    fun onIntent(intent: AccountIntent) { /* single dispatch point */ }
}
```

`LoginSheetIntent` replaces today's four public mutators — `onSubmit(onSuccess)`,
`onGoogleRedirect(uri, onSuccess)`, `onGoogleSignInStarted()`, and
`resetForDismiss()` — with `Submit`, `GoogleRedirect(uri)`, `GoogleSignInStarted`,
and `Dismissed`, respectively, plus `EmailChanged(value)`, `PasswordChanged(value)`,
and `ModeToggled` for the three remaining `onXChanged`/`onToggleMode` mutators.
`LoginSheetEffect.LoginSucceeded` is what the NavHost collects once instead of the
`onSuccess` lambda threaded in from the call site. Every one of these must go
through `onIntent` — none stay as separate public functions (see Anti-goals).

### Screen composable signature

Screen-level composables take exactly `state` and `onIntent`, nothing else —
textbook MVI, and it keeps the composable fully decoupled from any particular
ViewModel:

```kotlin
@Composable
fun AccountScreen(
    state: AccountState,
    onIntent: (AccountIntent) -> Unit,
    modifier: Modifier = Modifier,
    // navigation-only params (onOpenSettings, etc.) stay as-is if AccountScreen
    // doesn't own that behavior as state
) { /* ... */ }
```

This changes every call site: `AccountNavHost`'s `AccountScreen(...)` call, every
`@Preview`, both designsheet catalogs (`AccountScreenCatalog`, `LoginSheetCatalog`,
`ProfileImagesCatalog`), and the JVM UI tests (`AccountScreenContentTest`,
`LoginSheetContentTest`) — all of these currently pass granular lambdas
(`onLogout = {}`, `onEmailChanged = {}`, etc.) and need to pass
`onIntent = { capturedIntent = it }` (or similar) instead.

## File-placement defaults (the builder's calls, stated so they can be checked)

- DTOs live under `data/models/dto/`, not next to the API client that produces
  them — kept separate from domain models (`data/models/*.kt`) but under the same
  parent, per direction received.
- `RefreshCoordinator` and `GoogleAuthCallback` go to `data/remote/`: both are
  thin orchestration/bridge layers around a remote concern (token refresh calls;
  the OAuth redirect from the platform entry point), even though
  `RefreshCoordinator` reads `SessionStorage` (local).
- `NetworkModule.kt` and `DemoNetworkModule.kt` each split in two: the Koin
  `module { }` block stays under that filename in `di/modules/`; the actual
  `HttpClient` construction logic (`createHttpClient`, `configureSkimbyteClient`,
  `demoMockEngine()` + seed data) moves to new files under `data/remote/`.
- `AccountNavHost` stays under `ui/screens/auth/` (it's screen-local nested
  navigation), not `ui/navigation/` (reserved for app-wide top-level graphs, which
  `auth` doesn't have any of today).
- `AccountNavHost` also wires in `SettingsScreen`/`SettingsViewModel` and
  `TopicsScreen`/`TopicsViewModel` (both out of scope). That wiring is untouched —
  it keeps constructing/calling those ViewModels exactly as it does today, still
  MVVM, still importing from their current (unmoved) packages. Only the parts of
  `AccountNavHost` that touch `AccountViewModel`/`LoginSheetViewModel` change, to
  `onIntent(...)` calls.
- `ApiConfig` importing `BackendConfig` is an existing dependency, not a new one
  introduced by this move — both land in `data/remote/`, so the dependency
  direction question resolves itself; no action needed beyond the import-path
  update already covered above.

## What's left to the builder

- Exact reducer/handler function names and internal structure inside `onIntent`
  (e.g. one `when` block vs. private per-intent handlers) — either is fine as long
  as `onIntent` is the only public mutator.
- Whether `AccountEffect`/`LoginSheetEffect` need any cases beyond
  `LoginSucceeded` — re-derive from what the NavHost/call sites actually need once
  touching that code, rather than guessing now.
- Migration order (e.g. move files first with a mechanical commit, then convert to
  MVI in a second commit, vs. doing both at once per file) — pick whatever keeps
  the diff reviewable; this does not need to be a single commit.
- Regenerating Roborazzi goldens (`recordRoborazziAndroidHostTest`) after the
  package move, deleting the stale goldens under the old qualified class names
  (`io.redigit.skimbyte.auth.ui.AvatarWithEditBadgeScreenshotTest.*.png`,
  `io.redigit.skimbyte.auth.ui.LoginSheetScreenshotTest.*.png`), and confirming
  `verifyRoborazziAndroidHostTest` passes clean afterward (no orphaned PNGs).
- Test files that move along with their production counterparts:
  `commonTest/.../auth/AuthRepositoryImplTest.kt`,
  `commonTest/.../auth/ui/AccountViewModelTest.kt`,
  `commonTest/.../auth/ui/LoginSheetViewModelTest.kt`,
  `jvmTest/.../auth/ui/AccountScreenContentTest.kt`,
  `jvmTest/.../auth/ui/LoginSheetContentTest.kt`,
  `androidHostTest/.../auth/ui/LoginSheetScreenshotTest.kt`, and the
  `AvatarWithEditBadgeScreenshotTest` (package/location to confirm when touching
  it, since `AvatarWithEditBadge` itself moves to `ui/components/`).
- Updating every import across the codebase that references moved
  `auth.*`/`network.*` symbols: Koin module registration wherever the full
  `KoinApplication` is assembled (`di/AppModule.kt` or equivalent), `App.kt`,
  `MainActivity.kt` (`BackendConfig` import), the three designsheet catalogs
  (`AccountScreenCatalog`, `LoginSheetCatalog`, `ProfileImagesCatalog`), and other
  features importing `ApiConfig`/`Page` (`posts`, `feed`).
- `shared/build.gradle.kts`'s `generateAuthConfig` task: update its output path
  and generated package declaration to match `BackendConfig`'s new home.