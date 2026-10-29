# Auth Feature Layer Restructure + MVI Pilot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move the `auth` feature (and the `network/` infrastructure it depends on) out of the current flat per-feature packages into a layer-first `ui/`, `data/`, `di/` package skeleton, and convert `AccountViewModel`/`LoginSheetViewModel` from direct-mutation MVVM to MVI (`Intent` in, `State` out, `SideEffect` for one-off events).

**Architecture:** Each task moves one coherent slice of `auth`/`network` to its new home and fixes every importer in the same commit, so the build stays green throughout. The data/DI layer moves first (Tasks 1–8, pure relocation, no behavior change). Tasks 9–11 convert the UI layer to MVI and relocate it: because Kotlin compiles the whole module together and `AccountNavHost` couples every screen in this feature to every other, each of Tasks 10–11 bundles a ViewModel's MVI conversion together with the screen(s) that construct it, so every task still ends on a fully-compiling, fully-passing commit rather than a partially-converted one. Task 12 is a final verification pass. `feed`, `bookmarks`, `settings`, `onboarding`, `search` are untouched.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin DI, Navigation 3, kotlinx.coroutines (`StateFlow`/`SharedFlow`), Ktor, Roborazzi.

**Spec:** `docs/superpowers/specs/2026-10-22-auth-mvi-restructure-design.md`

## Global Constraints

- Every file that moves must keep compiling on every target: Android, iOS (`iosSimulatorArm64Test`), JVM, JS, WasmJS — not just the targets with fast local test commands.
- No new `expect`/`actual` declarations, no new third-party dependency, no DI framework other than Koin, no navigation library other than Navigation 3.
- No user-visible behavior change on the account or login screens. Where today's code has an existing quirk (see Review Focus), preserve it exactly rather than "fixing" it while already touching that file.
- Every `ViewModel`'s only public mutator is `onIntent(...)`. No public function may mutate state outside it.
- Roborazzi goldens are regenerated under the new qualified class name after any screenshot test's package changes; the old-named PNGs are deleted, not left orphaned.
- `DemoNetworkModule.kt` and `network/dto/*` it still needs do **not** move in this project — only their import lines change where they reference moved symbols.

## Review Focus

- A package-only import fix that compiles on JVM but was missed on `iosMain`/`jsMain`/`wasmJsMain` (no `expect`/`actual` is being added, but a stray platform-specific import could still diverge) — Task 12 compiles every target, not just `jvmTest`/`testAndroidHostTest`.
- Koin wiring is only ever exercised through the real DI graph at app startup, never through the unit tests' fakes — a qualifier or import mismatch introduced while splitting `AuthModule.kt`/`NetworkModule.kt` (Task 8) would compile fine and fail silently or at runtime. Task 12 launches the app once via `:desktopApp:run` to exercise the real Koin graph.
- `AccountRoute`'s existing `LoginSheet(onDismiss = {}, onLoginSuccess = {})` call (both lambdas are already no-ops today) must stay exactly as-is when `AccountScreen.kt` is touched in Task 10 — not "fixed," per the non-goal of no behavior change.
- Roborazzi goldens left orphaned under the old qualified class name after a screenshot test's package changes (Tasks 9 and 11) — each of those tasks' own steps delete the stale PNGs and re-run `verifyRoborazziAndroidHostTest` clean.
- A stale previously-generated `BackendConfig.kt` left in `shared/build/generated/...` at the *old* path after Task 2 changes `generateAuthConfig`'s output directory, causing a duplicate-class or unresolved-reference build error — Task 2's steps run a clean build to catch this.

---

## Task 1: Move domain models and DTOs to `data/models`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/models/Profile.kt` (from `auth/Profile.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/models/dto/{LoginRequestDto,LoginResponseDto,MeResponseDto,RefreshRequestDto,RegisterRequestDto}.kt` (from `auth/dto/*.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/models/dto/{ErrorDto,PageDto}.kt` (from `network/dto/ErrorDto.kt`, `network/dto/PageDto.kt`)
- Delete: `auth/Profile.kt`, `auth/dto/` (whole directory), `network/dto/ErrorDto.kt`, `network/dto/PageDto.kt`
- Modify (package/import only, no logic change): `auth/AuthApi.kt`, `auth/Profile.kt`→moved, `network/NetworkModule.kt`, `network/DemoNetworkModule.kt`, `network/Page.kt`, `posts/PostsApi.kt`, `posts/Post.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/FeedViewModelTest.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/SearchViewModelTest.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/bookmarks/BookmarksViewModelTest.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/AuthRepositoryImplTest.kt`

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.models.Profile`, `io.redigit.skimbyte.data.models.dto.{LoginRequestDto, LoginResponseDto, MeResponseDto, RefreshRequestDto, RegisterRequestDto, ErrorDto, ErrorEnvelopeDto, PageDto}` — same type members as today, only the package changes.

- [ ] **Step 1: Move the 5 auth DTO files and the 2 network DTO files**

`git mv` each file to its new path under `data/models/dto/`, updating each file's `package` declaration to `io.redigit.skimbyte.data.models.dto`. No other content changes.

- [ ] **Step 2: Move `Profile.kt`**

`git mv auth/Profile.kt data/models/Profile.kt`. Update its `package` to `io.redigit.skimbyte.data.models` and its `import io.redigit.skimbyte.auth.dto.MeResponseDto` to `io.redigit.skimbyte.data.models.dto.MeResponseDto`.

- [ ] **Step 3: Fix every importer of the moved DTOs/Profile**

Update imports (old → new package) in: `auth/AuthApi.kt`, `network/NetworkModule.kt` (imports `LoginResponseDto`, `RefreshRequestDto`), `network/DemoNetworkModule.kt` (imports `PageDto`), `network/Page.kt` (imports `PageDto`), `posts/PostsApi.kt` (imports `ApiConfig` — unaffected this task — and `PageDto`), `posts/Post.kt` (imports `ApiConfig`, unaffected this task), `commonTest/feed/FeedViewModelTest.kt` (imports `PageDto`), `commonTest/feed/SearchViewModelTest.kt`, `commonTest/bookmarks/BookmarksViewModelTest.kt` (import `Page`, unaffected this task — verify, no change needed if they only import `Page` not `PageDto`), `commonTest/auth/AuthRepositoryImplTest.kt` (imports `LoginResponseDto`, `MeResponseDto`).

- [ ] **Step 4: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS, no compile errors.

- [ ] **Step 5: Commit**

```bash
git add -A shared/src
git commit -m "refactor: move auth/network DTOs and Profile to data/models"
```

---

## Task 2: Move `ApiConfig` + `BackendConfig` generation to `data/remote`

**Files:**
- Modify: `shared/build.gradle.kts:145-182` (the `generateAuthConfig` task)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/ApiConfig.kt` (from `network/ApiConfig.kt`)
- Delete: `network/ApiConfig.kt`
- Modify (import only): `data/models/Profile.kt`, `network/NetworkModule.kt`, `auth/ui/LoginSheet.kt`, `androidApp/src/main/kotlin/io/redigit/skimbyte/MainActivity.kt`, `posts/PostsApi.kt`, `posts/Post.kt`

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.remote.BackendConfig` (generated object, same members: `AUTH_BASE_URL`, `GOOGLE_REDIRECT_URI`, `API_BASE_URL`, `SPACES_CDN_BASE_URL`, `PRODUCT_ID`), `io.redigit.skimbyte.data.remote.ApiConfig` (same members as today).

- [ ] **Step 1: Update the `generateAuthConfig` Gradle task**

In `shared/build.gradle.kts`, change the generated file path at line 161 from `outputDir.get().file("io/redigit/skimbyte/auth/BackendConfig.kt")` to `outputDir.get().file("io/redigit/skimbyte/data/remote/BackendConfig.kt")`, and change the `package` line in the generated file's text (line 165) from `io.redigit.skimbyte.auth` to `io.redigit.skimbyte.data.remote`.

- [ ] **Step 2: Clean and regenerate**

Run: `./gradlew :shared:clean :shared:generateAuthConfig`
Expected: a fresh `BackendConfig.kt` exists only under `shared/build/generated/.../io/redigit/skimbyte/data/remote/`, and the old `io/redigit/skimbyte/auth/BackendConfig.kt` output path no longer exists anywhere under `shared/build/`.

- [ ] **Step 3: Move `ApiConfig.kt`**

`git mv network/ApiConfig.kt data/remote/ApiConfig.kt`. Update its `package` to `io.redigit.skimbyte.data.remote` and its `import io.redigit.skimbyte.auth.BackendConfig` to `io.redigit.skimbyte.data.remote.BackendConfig` (same package now, so the import can simply be removed).

- [ ] **Step 4: Fix every importer of `ApiConfig`/`BackendConfig`**

Update imports in: `data/models/Profile.kt` (imports `ApiConfig`), `network/NetworkModule.kt` (imports `ApiConfig`), `auth/ui/LoginSheet.kt` (imports `BackendConfig` and `ApiConfig`), `androidApp/.../MainActivity.kt` (imports `BackendConfig`), `posts/PostsApi.kt` (imports `ApiConfig`), `posts/Post.kt` (imports `ApiConfig`).

- [ ] **Step 5: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest :androidApp:assembleDebug`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add -A shared/src shared/build.gradle.kts androidApp/src
git commit -m "refactor: move ApiConfig/BackendConfig generation to data/remote"
```

---

## Task 3: Move `SessionStorage` to `data/local`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/local/SessionStorage.kt` (from `auth/SessionStorage.kt`)
- Delete: `auth/SessionStorage.kt`
- Modify (import only): `auth/AuthRepositoryImpl.kt`, `auth/RefreshCoordinator.kt`, `auth/AuthModule.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/testing/FakeSessionStorage.kt`, `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/AuthRepositoryImplTest.kt` (its local `private class FakeSessionStorage`)

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.local.SessionStorage` (interface, unchanged members), `io.redigit.skimbyte.data.local.SettingsSessionStorage` (impl, unchanged).

- [ ] **Step 1: Move the file**

`git mv auth/SessionStorage.kt data/local/SessionStorage.kt`. Update its `package` to `io.redigit.skimbyte.data.local`.

- [ ] **Step 2: Fix every importer**

Add `import io.redigit.skimbyte.data.local.SessionStorage` (and `SettingsSessionStorage` where constructed) to: `auth/AuthRepositoryImpl.kt`, `auth/RefreshCoordinator.kt`, `auth/AuthModule.kt`, `commonTest/testing/FakeSessionStorage.kt`, `commonTest/auth/AuthRepositoryImplTest.kt`.

- [ ] **Step 3: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add -A shared/src
git commit -m "refactor: move SessionStorage to data/local"
```

---

## Task 4: Move `RefreshCoordinator` and `GoogleAuthCallback` to `data/remote`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/RefreshCoordinator.kt` (from `auth/RefreshCoordinator.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/GoogleAuthCallback.kt` (from `auth/GoogleAuthCallback.kt`)
- Delete: `auth/RefreshCoordinator.kt`, `auth/GoogleAuthCallback.kt`
- Move + update: `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/RefreshCoordinatorTest.kt` → `shared/src/commonTest/kotlin/io/redigit/skimbyte/data/remote/RefreshCoordinatorTest.kt`
- Modify (import only): `network/NetworkModule.kt`, `auth/AuthModule.kt`, `auth/ui/LoginSheet.kt` (GoogleAuthCallback), `androidApp/.../MainActivity.kt` (GoogleAuthCallback)

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.remote.RefreshCoordinator` (unchanged `refresh(...)` signature), `io.redigit.skimbyte.data.remote.GoogleAuthCallback` (unchanged `redirects`/`onRedirect`).

- [ ] **Step 1: Move the two files**

`git mv` each to `data/remote/`, update `package` to `io.redigit.skimbyte.data.remote`. `RefreshCoordinator.kt` additionally needs `import io.redigit.skimbyte.data.local.SessionStorage` added (cross-package now).

- [ ] **Step 2: Move and update the test**

`git mv shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/RefreshCoordinatorTest.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/data/remote/RefreshCoordinatorTest.kt`. Update its `package` to `io.redigit.skimbyte.data.remote` (drops the now-redundant `import io.redigit.skimbyte.testing.FakeSessionStorage` — keep it, `testing` package is unaffected).

- [ ] **Step 3: Fix every importer**

Update imports in: `network/NetworkModule.kt` (imports `RefreshCoordinator`, `SessionStorage` — note `SessionStorage` import was already fixed in Task 3), `auth/AuthModule.kt` (constructs `RefreshCoordinator(get())`), `auth/ui/LoginSheet.kt` (imports `GoogleAuthCallback`), `androidApp/.../MainActivity.kt` (imports `GoogleAuthCallback`).

- [ ] **Step 4: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -A shared/src androidApp/src
git commit -m "refactor: move RefreshCoordinator and GoogleAuthCallback to data/remote"
```

---

## Task 5: Split `NetworkModule.kt` into `SkimbyteHttpClient` (data/remote) + Koin registration; move `Page.kt`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/SkimbyteHttpClient.kt` (the `createHttpClient`/`configureSkimbyteClient` functions from `network/NetworkModule.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/modules/NetworkModule.kt` (just the `networkModule` Koin `module { }` block)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/Page.kt` (from `network/Page.kt`)
- Delete: `network/NetworkModule.kt`, `network/Page.kt`
- Move + update: `commonTest/network/TokenRefreshTest.kt` → `commonTest/data/remote/TokenRefreshTest.kt`; `commonTest/network/ConcurrentClientRefreshTest.kt` → `commonTest/data/remote/ConcurrentClientRefreshTest.kt`
- Modify (import only): `auth/AuthModule.kt` (calls `createHttpClient`), `network/DemoNetworkModule.kt` (calls `createHttpClient`, imports `PageDto` — already fixed in Task 1), `posts/PostsRepository.kt`, `posts/PostsRepositoryImpl.kt` (import `Page`, `toDomain`), `commonTest/feed/SearchViewModelTest.kt`, `commonTest/bookmarks/BookmarksViewModelTest.kt`, `commonTest/feed/FeedViewModelTest.kt` (import `Page`)

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.remote.createHttpClient(sessionStorage: SessionStorage, refreshCoordinator: RefreshCoordinator): HttpClient`, the `internal fun createHttpClient(sessionStorage, refreshCoordinator, engine: HttpClientEngine): HttpClient` test entry point, `io.redigit.skimbyte.data.remote.Page<T>` + `Page<T>.toDomain(...)`, `io.redigit.skimbyte.di.modules.networkModule` (Koin `Module`).

- [ ] **Step 1: Create `SkimbyteHttpClient.kt`**

Move `createHttpClient(sessionStorage, refreshCoordinator)`, the test-only `createHttpClient(sessionStorage, refreshCoordinator, engine)`, and `private fun HttpClientConfig<*>.configureSkimbyteClient(...)` from `network/NetworkModule.kt` into this new file verbatim, package `io.redigit.skimbyte.data.remote`.

- [ ] **Step 2: Create `di/modules/NetworkModule.kt`**

```kotlin
package io.redigit.skimbyte.di.modules

import io.ktor.client.HttpClient
import io.redigit.skimbyte.data.remote.createHttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { createHttpClient(get(), get()) }
}
```

- [ ] **Step 3: Delete old `network/NetworkModule.kt`, move `Page.kt`**

`git mv network/Page.kt data/remote/Page.kt`, update its `package` and its `import io.redigit.skimbyte.network.dto.PageDto` to `io.redigit.skimbyte.data.models.dto.PageDto`.

- [ ] **Step 4: Move the two `createHttpClient`-testing files**

`git mv` both to `commonTest/data/remote/`, update each `package` to `io.redigit.skimbyte.data.remote`; their `import io.redigit.skimbyte.auth.RefreshCoordinator` becomes `io.redigit.skimbyte.data.remote.RefreshCoordinator` (now same-package — remove the import), `import io.redigit.skimbyte.testing.FakeSessionStorage` is unchanged.

- [ ] **Step 5: Fix every importer**

Update imports in: `auth/AuthModule.kt`, `network/DemoNetworkModule.kt` (`createHttpClient`), `posts/PostsRepository.kt`, `posts/PostsRepositoryImpl.kt` (`Page`, `toDomain`), `commonTest/feed/SearchViewModelTest.kt`, `commonTest/bookmarks/BookmarksViewModelTest.kt`, `commonTest/feed/FeedViewModelTest.kt` (`Page`). Update `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt`'s `import io.redigit.skimbyte.network.networkModule` to `import io.redigit.skimbyte.di.modules.networkModule`.

- [ ] **Step 6: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add -A shared/src
git commit -m "refactor: split NetworkModule into SkimbyteHttpClient + Koin registration, move Page"
```

---

## Task 6: Move `AuthApi` to `data/remote`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/remote/AuthApi.kt` (from `auth/AuthApi.kt`)
- Delete: `auth/AuthApi.kt`
- Modify (import only): `auth/AuthModule.kt`, `auth/AuthRepositoryImpl.kt`, `auth/ProfileRepository.kt`, `commonTest/auth/AuthRepositoryImplTest.kt` (local `FakeAuthApi : AuthApi`)

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.remote.AuthApi` (interface, unchanged members), `io.redigit.skimbyte.data.remote.KtorAuthApi` (impl, unchanged).

- [ ] **Step 1: Move the file**

`git mv auth/AuthApi.kt data/remote/AuthApi.kt`. Update `package` to `io.redigit.skimbyte.data.remote`; its `import io.redigit.skimbyte.network.ApiConfig` becomes same-package (remove), its dto imports already point at `io.redigit.skimbyte.data.models.dto.*` from Task 1.

- [ ] **Step 2: Fix every importer**

Add `import io.redigit.skimbyte.data.remote.AuthApi` (and `KtorAuthApi` where constructed) to: `auth/AuthModule.kt`, `auth/AuthRepositoryImpl.kt`, `auth/ProfileRepository.kt`, `commonTest/auth/AuthRepositoryImplTest.kt`.

- [ ] **Step 3: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add -A shared/src
git commit -m "refactor: move AuthApi to data/remote"
```

---

## Task 7: Move `AuthRepository`, `ProfileRepository` to `data/repositories`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/repositories/AuthRepository.kt` (interface + `Session` data class, from `auth/AuthRepository.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/repositories/AuthRepositoryImpl.kt` (from `auth/AuthRepositoryImpl.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/data/repositories/ProfileRepository.kt` (interface + impl, from `auth/ProfileRepository.kt`)
- Delete: `auth/AuthRepository.kt`, `auth/AuthRepositoryImpl.kt`, `auth/ProfileRepository.kt`
- Move + update: `commonTest/auth/AuthRepositoryImplTest.kt` → `commonTest/data/repositories/AuthRepositoryImplTest.kt`
- Modify (import only): `auth/AuthModule.kt`, `auth/ui/AccountViewModel.kt`, `auth/ui/LoginSheetViewModel.kt`, `commonTest/auth/ui/AccountViewModelTest.kt`, `commonTest/auth/ui/LoginSheetViewModelTest.kt`

**Interfaces:**
- Produces: `io.redigit.skimbyte.data.repositories.{AuthRepository, Session, AuthRepositoryImpl, ProfileRepository, ProfileRepositoryImpl}` — all members unchanged from today.

- [ ] **Step 1: Move the three production files**

`git mv` each to `data/repositories/`, update `package` to `io.redigit.skimbyte.data.repositories`. `AuthRepositoryImpl.kt` needs `import io.redigit.skimbyte.data.remote.AuthApi` and `import io.redigit.skimbyte.data.local.SessionStorage` added (cross-package now). `ProfileRepository.kt` needs `import io.redigit.skimbyte.data.remote.AuthApi` and `import io.redigit.skimbyte.data.models.Profile` added.

- [ ] **Step 2: Move and update the test**

`git mv shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/AuthRepositoryImplTest.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/data/repositories/AuthRepositoryImplTest.kt`. Update `package` to `io.redigit.skimbyte.data.repositories`; add imports for `SessionStorage` (`data.local`), `AuthApi` (`data.remote`), dto types (`data.models.dto`, already correct from Task 1).

- [ ] **Step 3: Fix every importer**

Update imports in: `auth/AuthModule.kt` (constructs `AuthRepositoryImpl`, `ProfileRepositoryImpl`), `auth/ui/AccountViewModel.kt` (imports `AuthRepository`, `ProfileRepository`), `auth/ui/LoginSheetViewModel.kt` (imports `AuthRepository`), `commonTest/auth/ui/AccountViewModelTest.kt` (imports `AuthRepository`, `Profile`, `ProfileRepository`, `Session`), `commonTest/auth/ui/LoginSheetViewModelTest.kt` (imports `AuthRepository`, `Session`).

- [ ] **Step 4: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -A shared/src
git commit -m "refactor: move AuthRepository and ProfileRepository to data/repositories"
```

---

## Task 8: Split `AuthModule.kt` into `di/modules/AuthModule.kt`; update `AppModule.kt`

At this point every symbol `AuthModule.kt` references (`SessionStorage`, `RefreshCoordinator`, `createHttpClient`, `AuthApi`, `AuthRepository`, `ProfileRepository`) already lives in its final `data/*` home. Only the two ViewModel registrations (`AccountViewModel`, `LoginSheetViewModel`) still point at the old `auth.ui` package — that's correct for now; Tasks 10–11 move them and this file's `viewModel { }` lines get their imports fixed again then.

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/modules/AuthModule.kt` (the `authModule` Koin block, from `auth/AuthModule.kt`)
- Delete: `auth/AuthModule.kt` — **this empties the old `auth/` package down to just its `ui/` subfolder**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt`

**Interfaces:**
- Produces: `io.redigit.skimbyte.di.modules.authModule` (Koin `Module`, unchanged registrations).

- [ ] **Step 1: Move `AuthModule.kt`**

`git mv auth/AuthModule.kt di/modules/AuthModule.kt`. Update `package` to `io.redigit.skimbyte.di.modules`. Update its imports: `Settings` (unchanged, `com.russhwolf.settings`), `SessionStorage`/`SettingsSessionStorage` → `io.redigit.skimbyte.data.local`, `RefreshCoordinator` → `io.redigit.skimbyte.data.remote`, `createHttpClient` → `io.redigit.skimbyte.data.remote`, `AuthApi`/`KtorAuthApi` → `io.redigit.skimbyte.data.remote`, `AuthRepository`/`AuthRepositoryImpl`/`ProfileRepository`/`ProfileRepositoryImpl` → `io.redigit.skimbyte.data.repositories`. `AccountViewModel`/`LoginSheetViewModel` imports stay `io.redigit.skimbyte.auth.ui.*` for now.

- [ ] **Step 2: Update `AppModule.kt`**

Change `import io.redigit.skimbyte.auth.authModule` to `import io.redigit.skimbyte.di.modules.authModule` (the `import io.redigit.skimbyte.network.networkModule` → `io.redigit.skimbyte.di.modules.networkModule` line was already fixed in Task 5).

- [ ] **Step 3: Build and run tests**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 4: Verify old `auth/` package contents**

Run: `find shared/src/commonMain/kotlin/io/redigit/skimbyte/auth -maxdepth 1 -type f`
Expected: empty output (only the `ui/` subdirectory remains under `auth/`).

- [ ] **Step 5: Commit**

```bash
git add -A shared/src
git commit -m "refactor: split AuthModule into di/modules/AuthModule"
```

---

## Task 9: Move `AvatarWithEditBadge` and `BannerImage` to `ui/components`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/components/AvatarWithEditBadge.kt` (from `auth/ui/AvatarWithEditBadge.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/components/BannerImage.kt` (from `auth/ui/BannerImage.kt`)
- Delete: `auth/ui/AvatarWithEditBadge.kt`, `auth/ui/BannerImage.kt`
- Move: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadgeScreenshotTest.kt` → `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/ui/components/AvatarWithEditBadgeScreenshotTest.kt`
- Delete: `shared/src/androidHostTest/roborazzi/io.redigit.skimbyte.auth.ui.AvatarWithEditBadgeScreenshotTest.{noAvatar,uploading}.png`
- Modify (import only): `auth/ui/AccountScreen.kt`, `designsheet/.../components/ProfileImagesCatalog.kt`

**Interfaces:**
- Produces: `io.redigit.skimbyte.ui.components.{AvatarWithEditBadge, BannerImage}` — identical signatures to today (`avatarUrl`/`bannerUrl`, `isUploading`, `onAvatarClick`/`onBannerClick`, `onEditClick`, `modifier`, `sharedTransition`). No MVI conversion — these are reusable presentational components, not screens.

- [ ] **Step 1: Move the two component files**

`git mv` each to `ui/components/`, update `package` to `io.redigit.skimbyte.ui.components`. No other content change.

- [ ] **Step 2: Move the screenshot test**

`git mv shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadgeScreenshotTest.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/ui/components/AvatarWithEditBadgeScreenshotTest.kt`. Update `package` to `io.redigit.skimbyte.ui.components`.

- [ ] **Step 3: Fix importers**

Update imports in `auth/ui/AccountScreen.kt` and `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/ProfileImagesCatalog.kt`.

- [ ] **Step 4: Delete stale goldens and regenerate**

Run: `rm shared/src/androidHostTest/roborazzi/io.redigit.skimbyte.auth.ui.AvatarWithEditBadgeScreenshotTest.*.png`
Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "io.redigit.skimbyte.ui.components.AvatarWithEditBadgeScreenshotTest"`
Expected: two new PNGs appear under `io.redigit.skimbyte.ui.components.AvatarWithEditBadgeScreenshotTest.{noAvatar,uploading}.png`.

- [ ] **Step 5: Verify clean**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest --tests "io.redigit.skimbyte.ui.components.AvatarWithEditBadgeScreenshotTest"`
Expected: PASS.

- [ ] **Step 6: Build and run full test suite**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add -A shared/src designsheet/src
git commit -m "refactor: move AvatarWithEditBadge and BannerImage to ui/components"
```

---

## Task 10: Convert the Account feature to MVI (ViewModel + Screen + Route), relocate to `ui/screens/auth`

`AccountViewModel`'s conversion and `AccountScreen`/`AccountRoute`'s signature change are bundled into one task: once `AccountUiState`/the old `AccountViewModel` are deleted, `AccountScreen.kt` (which constructs them) won't compile until it's converted too — splitting them into separate commits would mean one of the two commits leaves the module in a broken state. `AccountNavHost.kt` also constructs `AccountViewModel` directly; it stays at its current location (`auth/ui/`) for this task, with just its `AccountViewModel`-related calls updated to keep it compiling — it fully relocates in Task 11, once `LoginSheet`/`ImageViewerScreen` (which it also wires) have moved too.

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountIntent.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountEffect.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountState.kt` (renamed from `AccountUiState.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountViewModel.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountScreen.kt`
- Delete: `auth/ui/AccountUiState.kt`, `auth/ui/AccountViewModel.kt`, `auth/ui/AccountScreen.kt`
- Move + rewrite: `commonTest/auth/ui/AccountViewModelTest.kt` → `commonTest/ui/screens/auth/AccountViewModelTest.kt`; `jvmTest/auth/ui/AccountScreenContentTest.kt` → `jvmTest/ui/screens/auth/AccountScreenContentTest.kt`
- Modify: `di/modules/AuthModule.kt` (viewModel registration import), `auth/ui/AccountNavHost.kt` (import + call-site fixes only, not relocated yet), `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/AccountScreenCatalog.kt`

**Interfaces:**
- Consumes: `io.redigit.skimbyte.data.repositories.{AuthRepository, ProfileRepository}` (Task 7), `io.redigit.skimbyte.util.PickedImage` (unchanged location), `io.redigit.skimbyte.ui.components.{AvatarWithEditBadge, BannerImage}` (Task 9), `io.redigit.skimbyte.feed.ui.SharedElementContext` (unchanged), `io.redigit.skimbyte.util.rememberImagePickerLauncher` (unchanged).
- Produces:
  - `AccountIntent` (sealed interface): `Logout`, `AvatarPicked(image: PickedImage)`, `BannerPicked(image: PickedImage)`.
  - `AccountEffect` (sealed interface, no cases yet — `Logout` is fully observed via `AuthRepository.session` elsewhere).
  - `AccountState` (data class, same 7 fields as today's `AccountUiState`: `isLoggedIn`, `avatarUrl`, `bannerUrl`, `avatarUploadInProgress`, `bannerUploadInProgress`, `avatarError`, `bannerError`).
  - `AccountViewModel(authRepository: AuthRepository, profileRepository: ProfileRepository) : ViewModel()` exposing `val state: StateFlow<AccountState>`, `val effects: SharedFlow<AccountEffect>`, and the single mutator `fun onIntent(intent: AccountIntent)`.
  - `AccountScreen(state: AccountState, onIntent: (AccountIntent) -> Unit, onSettingsClick: () -> Unit, onAvatarClick: () -> Unit, onAvatarEditClick: () -> Unit, onBannerClick: () -> Unit, onBannerEditClick: () -> Unit, modifier: Modifier = Modifier, avatarSharedTransition: SharedElementContext? = null, bannerSharedTransition: SharedElementContext? = null)` — only `onLogout` is removed (replaced by `onIntent(AccountIntent.Logout)` on the confirm button); every other param is unchanged (navigation/picker-launch triggers, not state-owned).
  - `AccountRoute(onSettingsClick: () -> Unit, onAvatarClick: () -> Unit, onBannerClick: () -> Unit, viewModel: AccountViewModel = koinViewModel(), modifier: Modifier = Modifier, avatarSharedTransition: SharedElementContext? = null, bannerSharedTransition: SharedElementContext? = null)` — unchanged signature.

- [ ] **Step 1: Write the failing test for `AccountViewModel`**

Rewrite `commonTest/ui/screens/auth/AccountViewModelTest.kt`, replacing every `vm.uiState.value` with `vm.state.value`, every `AccountUiState(...)` with `AccountState(...)`, and every direct mutator call with `onIntent`:

```kotlin
@Test
fun initialState_reflectsSessionAndProfile() = runTest {
    val repo = FakeProfileRepository(Profile("u1", avatarUrl = "https://cdn/a.jpg", bannerUrl = null))
    val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

    assertEquals(AccountState(isLoggedIn = true, avatarUrl = "https://cdn/a.jpg", bannerUrl = null), vm.state.value)
}

@Test
fun onAvatarPicked_success_clearsInProgressAndError() = runTest {
    val repo = FakeProfileRepository()
    val vm = AccountViewModel(FakeAuthRepositoryForAccount(loggedIn = true), repo)

    vm.onIntent(AccountIntent.AvatarPicked(PickedImage(ByteArray(10), "image/png")))

    assertEquals(false, vm.state.value.avatarUploadInProgress)
    assertNull(vm.state.value.avatarError)
    assertEquals(1, repo.uploadAvatarCallCount)
}
```

Apply the same `onIntent(AccountIntent.AvatarPicked(...))` / `onIntent(AccountIntent.BannerPicked(...))` substitution to the remaining 3 existing test methods (`onAvatarPicked_failure_setsErrorAndClearsInProgress`, `onAvatarPicked_whileAlreadyUploading_isIgnored`, `onBannerPicked_success_clearsInProgressAndError`), keeping each test's existing assertions and fakes (`FakeAuthRepositoryForAccount`, `FakeProfileRepository`) unchanged.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.ui.screens.auth.AccountViewModelTest"`
Expected: FAIL (compile error — `AccountIntent`/`AccountState`/`AccountViewModel` don't exist at the new package yet).

- [ ] **Step 3: Implement `AccountIntent.kt`**

```kotlin
package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.util.PickedImage

sealed interface AccountIntent {
    data object Logout : AccountIntent
    data class AvatarPicked(val image: PickedImage) : AccountIntent
    data class BannerPicked(val image: PickedImage) : AccountIntent
}
```

- [ ] **Step 4: Implement `AccountEffect.kt`**

```kotlin
package io.redigit.skimbyte.ui.screens.auth

sealed interface AccountEffect
```

- [ ] **Step 5: Implement `AccountState.kt`**

Same fields as today's `AccountUiState`, renamed class, same package `io.redigit.skimbyte.ui.screens.auth`.

- [ ] **Step 6: Implement `AccountViewModel.kt`**

Same `init` block as today (combine `authRepository.session` + `profileRepository.profile`, writing into `_state` instead of `_uiState`). Replace the three public mutators (`onLogout`, `onAvatarPicked`, `onBannerPicked`) with private functions of the same bodies, dispatched from a single public `onIntent`:

```kotlin
fun onIntent(intent: AccountIntent) {
    when (intent) {
        AccountIntent.Logout -> authRepository.logout()
        is AccountIntent.AvatarPicked -> onAvatarPicked(intent.image)
        is AccountIntent.BannerPicked -> onBannerPicked(intent.image)
    }
}
```

`_effects` is declared (`MutableSharedFlow<AccountEffect>()` / `val effects: SharedFlow<AccountEffect> = _effects.asSharedFlow()`) but nothing emits into it yet — no case needs it today.

- [ ] **Step 7: Run the test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.ui.screens.auth.AccountViewModelTest"`
Expected: PASS.

- [ ] **Step 8: Move `AccountScreen.kt` and rewrite its signature and body**

`git mv auth/ui/AccountScreen.kt ui/screens/auth/AccountScreen.kt`, update `package` to `io.redigit.skimbyte.ui.screens.auth`. Update imports: drop `AccountUiState`, add `AccountIntent`/`AccountState`; `AvatarWithEditBadge`/`BannerImage` → `io.redigit.skimbyte.ui.components`.

Replace `uiState: AccountUiState` param with `state: AccountState`; replace `onLogout: () -> Unit` param with `onIntent: (AccountIntent) -> Unit`. Every other reference to `uiState.X` becomes `state.X`. The `AlertDialog`'s confirm button changes from `onClick = { showLogoutConfirm = false; onLogout() }` to `onClick = { showLogoutConfirm = false; onIntent(AccountIntent.Logout) }`.

- [ ] **Step 9: Rewrite `AccountRoute`'s body**

```kotlin
val state by viewModel.state.collectAsState()
val pickAvatar = rememberImagePickerLauncher { image -> image?.let { viewModel.onIntent(AccountIntent.AvatarPicked(it)) } }
val pickBanner = rememberImagePickerLauncher { image -> image?.let { viewModel.onIntent(AccountIntent.BannerPicked(it)) } }
if (!state.isLoggedIn) {
    LoginSheet(onDismiss = {}, onLoginSuccess = {})
}
AccountScreen(
    state = state,
    onIntent = viewModel::onIntent,
    onSettingsClick = onSettingsClick,
    onAvatarClick = onAvatarClick,
    onAvatarEditClick = pickAvatar,
    onBannerClick = onBannerClick,
    onBannerEditClick = pickBanner,
    modifier = modifier,
    avatarSharedTransition = avatarSharedTransition,
    bannerSharedTransition = bannerSharedTransition,
)
```

Leave the `LoginSheet(onDismiss = {}, onLoginSuccess = {})` call exactly as-is — both lambdas are already no-ops today; preserving that quirk, not fixing it, is the point (see Review Focus). `LoginSheet` hasn't moved yet (it moves in Task 11), so this reference is still to `io.redigit.skimbyte.auth.ui.LoginSheet` — add that import explicitly since `AccountScreen.kt` is no longer in the same package as `LoginSheet.kt`.

- [ ] **Step 10: Rewrite all 8 `@Preview` functions**

Each changes `uiState = AccountUiState(...)` to `state = AccountState(...)` and `onLogout = {}` to `onIntent = {}`.

- [ ] **Step 11: Move and rewrite `AccountScreenContentTest.kt`**

`git mv jvmTest/auth/ui/AccountScreenContentTest.kt jvmTest/ui/screens/auth/AccountScreenContentTest.kt`, update `package`. Every test replaces `uiState = AccountUiState(...)` with `state = AccountState(...)` and `onLogout = { ... }` with `onIntent = { intent -> ... }`. The two logout-specific tests change their assertion target:

```kotlin
@Test
fun confirmingLogoutDialog_invokesOnIntentWithLogout() = runComposeUiTest {
    var capturedIntent: AccountIntent? = null
    setContent {
        SkimbyteTheme {
            AccountScreen(
                state = AccountState(isLoggedIn = true),
                onIntent = { capturedIntent = it },
                onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
            )
        }
    }
    onNodeWithTag("logoutRow").performClick()
    waitForIdle()
    onNodeWithTag("confirmLogout").performClick()
    waitForIdle()
    assertEquals(AccountIntent.Logout, capturedIntent)
}

@Test
fun dismissingLogoutDialog_doesNotInvokeOnIntent() = runComposeUiTest {
    var capturedIntent: AccountIntent? = null
    // same setContent as above with onIntent = { capturedIntent = it }
    onNodeWithTag("logoutRow").performClick()
    waitForIdle()
    onNodeWithText("Cancel").performClick()
    waitForIdle()
    onNodeWithText("Log out?").assertDoesNotExist()
    assertNull(capturedIntent)
}
```

The other 8 tests (`tappingAvatar_invokesOnAvatarClick`, etc.) keep their existing params/assertions, only swapping `uiState=`→`state=` and adding `onIntent = {}`.

- [ ] **Step 12: Fix the `AuthModule.kt` registration**

Update `di/modules/AuthModule.kt`'s `import io.redigit.skimbyte.auth.ui.AccountViewModel` to `io.redigit.skimbyte.ui.screens.auth.AccountViewModel`. (`viewModel { AccountViewModel(get(), get()) }` call itself is unchanged.)

- [ ] **Step 13: Patch `AccountNavHost.kt` just enough to keep compiling (not relocated yet)**

`AccountNavHost.kt` stays at `auth/ui/AccountNavHost.kt` for now. Add `import io.redigit.skimbyte.ui.screens.auth.{AccountViewModel, AccountIntent, AccountRoute}`. Change `accountViewModel.uiState.collectAsState()` to `accountViewModel.state.collectAsState()`, and the two `rememberImagePickerLauncher` calls from `accountViewModel::onAvatarPicked`/`accountViewModel::onBannerPicked` to `{ image -> accountViewModel.onIntent(AccountIntent.AvatarPicked(image)) }`/`{ image -> accountViewModel.onIntent(AccountIntent.BannerPicked(image)) }`.

- [ ] **Step 14: Update `AccountScreenCatalog.kt`**

Change `AccountUiState(isLoggedIn = true)` → `AccountState(isLoggedIn = true)`, `onLogout = {}` → `onIntent = {}`, update imports to `io.redigit.skimbyte.ui.screens.auth.{AccountScreen, AccountState, AccountIntent}`.

- [ ] **Step 15: Delete old files, build, run full test suite**

Delete `auth/ui/AccountUiState.kt`, old `commonTest/auth/ui/AccountViewModelTest.kt`, old `jvmTest/auth/ui/AccountScreenContentTest.kt`.
Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest :androidApp:assembleDebug`
Expected: PASS — the module compiles fully; `auth/ui/LoginSheet.kt` is untouched and still compiles on its own (its signature isn't changing until Task 11).

- [ ] **Step 16: Commit**

```bash
git add -A shared/src designsheet/src
git commit -m "refactor: convert Account feature to MVI, move to ui/screens/auth"
```

---

## Task 11: Convert the LoginSheet feature to MVI, relocate `ImageViewerScreen`, finish relocating `AccountNavHost`

Same reasoning as Task 10: `LoginSheetViewModel`'s conversion and `LoginSheetContent`/`LoginSheet`'s signature change must land together. Once that's done, every file `AccountNavHost.kt` wires (`AccountViewModel`, `AccountScreen`/`AccountRoute`, `LoginSheet`, `ImageViewerScreen`) lives in `ui/screens/auth/`, so this task also fully relocates `AccountNavHost.kt` there and updates `App.kt` — completing the removal of the old `auth/` package.

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/LoginSheetIntent.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/LoginSheetEffect.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/LoginSheetState.kt` (renamed from `LoginSheetUiState.kt`)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/LoginSheetViewModel.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/LoginSheet.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/ImageViewerScreen.kt` (pure move, no signature change)
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/ui/screens/auth/AccountNavHost.kt`
- Delete: `auth/ui/LoginSheetUiState.kt`, `auth/ui/LoginSheetViewModel.kt`, `auth/ui/LoginSheet.kt`, `auth/ui/ImageViewerScreen.kt`, `auth/ui/AccountNavHost.kt` — **`auth/` is now fully gone**
- Move + rewrite: `commonTest/auth/ui/LoginSheetViewModelTest.kt` → `commonTest/ui/screens/auth/LoginSheetViewModelTest.kt`; `jvmTest/auth/ui/LoginSheetContentTest.kt` → `jvmTest/ui/screens/auth/LoginSheetContentTest.kt`; `androidHostTest/auth/ui/LoginSheetScreenshotTest.kt` → `androidHostTest/ui/screens/auth/LoginSheetScreenshotTest.kt`
- Delete: `shared/src/androidHostTest/roborazzi/io.redigit.skimbyte.auth.ui.LoginSheetScreenshotTest.{emptyState,errorState}.png`
- Modify: `di/modules/AuthModule.kt` (viewModel registration import), `App.kt` (`AccountNavHost` import), `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/LoginSheetCatalog.kt`

**Interfaces:**
- Consumes: `io.redigit.skimbyte.data.repositories.AuthRepository` (Task 7), `io.redigit.skimbyte.data.remote.{BackendConfig, GoogleAuthCallback, ApiConfig}` (Tasks 2, 4), `AccountViewModel`, `AccountIntent`, `AccountRoute` (Task 10), `io.redigit.skimbyte.settings.{SettingsViewModel, ui.SettingsScreen}` and `io.redigit.skimbyte.onboarding.{TopicsViewModel, ui.TopicsScreen}` — **unchanged, untouched, still MVVM, still imported from their current unmoved packages.**
- Produces:
  - `LoginSheetIntent` (sealed interface): `EmailChanged(value: String)`, `PasswordChanged(value: String)`, `ModeToggled`, `Submit`, `GoogleSignInStarted`, `GoogleRedirect(redirectUri: String)`, `Dismissed`.
  - `LoginSheetEffect` (sealed interface): `LoginSucceeded`.
  - `LoginSheetState` (data class, same 5 fields as today's `LoginSheetUiState`: `email`, `password`, `isRegisterMode`, `isSubmitting`, `error`).
  - `LoginSheetViewModel(authRepository: AuthRepository) : ViewModel()` exposing `val state: StateFlow<LoginSheetState>`, `val effects: SharedFlow<LoginSheetEffect>`, and the single mutator `fun onIntent(intent: LoginSheetIntent)`.
  - `LoginSheetContent(state: LoginSheetState, onIntent: (LoginSheetIntent) -> Unit, onGoogleSignInClick: () -> Unit, modifier: Modifier = Modifier)` — the 5 granular callback params collapse into `onIntent`; `onGoogleSignInClick` stays separate (it also triggers `openUrl(...)`, a platform action `onIntent` alone can't express).
  - `LoginSheet(onDismiss: () -> Unit, onLoginSuccess: () -> Unit, viewModel: LoginSheetViewModel = koinViewModel())` — unchanged signature.
  - `ImageViewerScreen(...)` — unchanged signature.
  - `AccountNavHost(onRootChanged: (Boolean) -> Unit)` — unchanged signature.

- [ ] **Step 1: Write the failing tests for `LoginSheetViewModel`**

Rewrite `commonTest/ui/screens/auth/LoginSheetViewModelTest.kt`. Field-mutation tests become direct `onIntent` calls with state assertions unchanged in substance:

```kotlin
@Test
fun onSubmit_blankFields_isNoOp() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))

    viewModel.onIntent(LoginSheetIntent.Submit)

    assertEquals(false, viewModel.state.value.isSubmitting)
}

@Test
fun onToggleMode_switchesToRegisterMode() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))

    viewModel.onIntent(LoginSheetIntent.ModeToggled)

    assertTrue(viewModel.state.value.isRegisterMode)
}
```

Tests that previously asserted on the `onSuccess`/`onSuccess()` callback must instead collect `effects`:

```kotlin
@Test
fun onSubmit_success_resetsState_andEmitsLoginSucceeded() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
    viewModel.onIntent(LoginSheetIntent.EmailChanged("demo@skimbyte.app"))
    viewModel.onIntent(LoginSheetIntent.PasswordChanged("demo"))
    val effects = mutableListOf<LoginSheetEffect>()
    val collectJob = launch { viewModel.effects.toList(effects) }

    viewModel.onIntent(LoginSheetIntent.Submit)

    assertEquals(listOf(LoginSheetEffect.LoginSucceeded), effects)
    assertEquals(LoginSheetState(), viewModel.state.value)
    collectJob.cancel()
}

@Test
fun onSubmit_failure_setsError_andStopsSubmitting() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException("invalid"))))
    viewModel.onIntent(LoginSheetIntent.EmailChanged("wrong@skimbyte.app"))
    viewModel.onIntent(LoginSheetIntent.PasswordChanged("wrong"))

    viewModel.onIntent(LoginSheetIntent.Submit)

    assertEquals(false, viewModel.state.value.isSubmitting)
    assertTrue(viewModel.state.value.error != null)
}

@Test
fun onSubmit_inRegisterMode_callsRegister_notLogin() = runTest {
    var registerCalled = false
    val repository = object : AuthRepository { /* same body as today, registerCalled = true */ }
    val viewModel = LoginSheetViewModel(repository)
    viewModel.onIntent(LoginSheetIntent.ModeToggled)
    viewModel.onIntent(LoginSheetIntent.EmailChanged("new@skimbyte.app"))
    viewModel.onIntent(LoginSheetIntent.PasswordChanged("newpass"))

    viewModel.onIntent(LoginSheetIntent.Submit)

    assertTrue(registerCalled)
}

@Test
fun onGoogleRedirect_success_resetsState_andEmitsLoginSucceeded() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
    val effects = mutableListOf<LoginSheetEffect>()
    val collectJob = launch { viewModel.effects.toList(effects) }

    viewModel.onIntent(LoginSheetIntent.GoogleRedirect("skimbyte://auth/callback?access_token=a&refresh_token=b"))

    assertEquals(listOf(LoginSheetEffect.LoginSucceeded), effects)
    assertEquals(LoginSheetState(), viewModel.state.value)
    collectJob.cancel()
}

@Test
fun onEmailChanged_clearsExistingError() = runTest {
    val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException())))
    viewModel.onIntent(LoginSheetIntent.EmailChanged("a"))
    viewModel.onIntent(LoginSheetIntent.PasswordChanged("b"))
    viewModel.onIntent(LoginSheetIntent.Submit)
    assertTrue(viewModel.state.value.error != null)

    viewModel.onIntent(LoginSheetIntent.EmailChanged("new@skimbyte.app"))

    assertNull(viewModel.state.value.error)
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.ui.screens.auth.LoginSheetViewModelTest"`
Expected: FAIL (compile error).

- [ ] **Step 3: Implement `LoginSheetIntent.kt`**

```kotlin
package io.redigit.skimbyte.ui.screens.auth

sealed interface LoginSheetIntent {
    data class EmailChanged(val value: String) : LoginSheetIntent
    data class PasswordChanged(val value: String) : LoginSheetIntent
    data object ModeToggled : LoginSheetIntent
    data object Submit : LoginSheetIntent
    data object GoogleSignInStarted : LoginSheetIntent
    data class GoogleRedirect(val redirectUri: String) : LoginSheetIntent
    data object Dismissed : LoginSheetIntent
}
```

- [ ] **Step 4: Implement `LoginSheetEffect.kt`**

```kotlin
package io.redigit.skimbyte.ui.screens.auth

sealed interface LoginSheetEffect {
    data object LoginSucceeded : LoginSheetEffect
}
```

- [ ] **Step 5: Implement `LoginSheetState.kt`**

Same 5 fields as today's `LoginSheetUiState`, renamed class, package `io.redigit.skimbyte.ui.screens.auth`.

- [ ] **Step 6: Implement `LoginSheetViewModel.kt`**

```kotlin
fun onIntent(intent: LoginSheetIntent) {
    when (intent) {
        is LoginSheetIntent.EmailChanged -> _state.value = _state.value.copy(email = intent.value, error = null)
        is LoginSheetIntent.PasswordChanged -> _state.value = _state.value.copy(password = intent.value, error = null)
        LoginSheetIntent.ModeToggled -> _state.value = _state.value.copy(isRegisterMode = !_state.value.isRegisterMode, error = null)
        LoginSheetIntent.Submit -> submit()
        LoginSheetIntent.GoogleSignInStarted -> _state.value = _state.value.copy(isSubmitting = true, error = null)
        is LoginSheetIntent.GoogleRedirect -> googleRedirect(intent.redirectUri)
        LoginSheetIntent.Dismissed -> _state.value = LoginSheetState()
    }
}
```

`submit()` and `googleRedirect(redirectUri: String)` are `private` functions with the same bodies as today's `onSubmit`/`onGoogleRedirect`, except: on success, instead of calling the `onSuccess` lambda parameter, launch `_effects.emit(LoginSheetEffect.LoginSucceeded)` inside the same `viewModelScope.launch` coroutine (use `emit`, not `tryEmit` — a collector set up via `launch { effects.collect { ... } }` before the intent fires will rendezvous correctly under `UnconfinedTestDispatcher`; `tryEmit` can silently drop the effect if no collector is actively suspended).

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.ui.screens.auth.LoginSheetViewModelTest"`
Expected: PASS.

- [ ] **Step 8: Move `ImageViewerScreen.kt`**

`git mv auth/ui/ImageViewerScreen.kt ui/screens/auth/ImageViewerScreen.kt`, update `package` only. No signature or body change.

- [ ] **Step 9: Move `LoginSheet.kt` and rewrite `LoginSheetContent`**

`git mv auth/ui/LoginSheet.kt ui/screens/auth/LoginSheet.kt`, update `package`. Update imports: `BackendConfig`, `GoogleAuthCallback`, `ApiConfig` → `io.redigit.skimbyte.data.remote`; `LoginSheetUiState` → `LoginSheetState`, add `LoginSheetIntent`, `LoginSheetEffect`.

Rewrite `LoginSheetContent`'s signature to `(state: LoginSheetState, onIntent: (LoginSheetIntent) -> Unit, onGoogleSignInClick: () -> Unit, modifier: Modifier = Modifier)`. Field callbacks become: email `onValueChange = { onIntent(LoginSheetIntent.EmailChanged(it)) }`, password `onValueChange = { onIntent(LoginSheetIntent.PasswordChanged(it)) }`, toggle button `onClick = { onIntent(LoginSheetIntent.ModeToggled) }`, submit button `onClick = { onIntent(LoginSheetIntent.Submit) }`, Google button keeps `onClick = onGoogleSignInClick`.

- [ ] **Step 10: Rewrite `LoginSheet`'s body**

```kotlin
@Composable
fun LoginSheet(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginSheetViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    LaunchedEffect(Unit) {
        GoogleAuthCallback.redirects.collect { uri -> viewModel.onIntent(LoginSheetIntent.GoogleRedirect(uri)) }
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginSheetEffect.LoginSucceeded -> onLoginSuccess()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(LoginSheetIntent.Dismissed)
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        LoginSheetContent(
            state = state,
            onIntent = viewModel::onIntent,
            onGoogleSignInClick = {
                viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)
                openUrl(googleSignInStartUrl())
            },
        )
    }
}
```

- [ ] **Step 11: Rewrite the 3 `@Preview` functions in `LoginSheet.kt`**

Each changes `state = LoginSheetUiState(...)` to `state = LoginSheetState(...)`, and the 5 callback params to `onIntent = {}, onGoogleSignInClick = {}`.

- [ ] **Step 12: Move and rewrite `LoginSheetContentTest.kt`**

`git mv jvmTest/auth/ui/LoginSheetContentTest.kt jvmTest/ui/screens/auth/LoginSheetContentTest.kt`, update `package`. Each test replaces the 5 callback params with `onIntent = { ... }, onGoogleSignInClick = {}`:

```kotlin
@Test
fun filledFields_submitInvokesOnIntentWithSubmit() = runComposeUiTest {
    var submittedIntent: LoginSheetIntent? = null
    setContent {
        SkimbyteTheme {
            LoginSheetContent(
                state = LoginSheetState(email = "demo@skimbyte.app", password = "demo"),
                onIntent = { submittedIntent = it },
                onGoogleSignInClick = {},
            )
        }
    }
    onNodeWithText("Log in").performClick()
    waitForIdle()
    assertEquals(LoginSheetIntent.Submit, submittedIntent)
}
```

Apply the same pattern to `emptyFields_submitIsDisabled` (asserts `submittedIntent` stays `null`) and `errorState_showsErrorMessage` (no intent assertion needed, just `onIntent = {}`).

- [ ] **Step 13: Move and rewrite `LoginSheetScreenshotTest.kt`**

`git mv androidHostTest/auth/ui/LoginSheetScreenshotTest.kt androidHostTest/ui/screens/auth/LoginSheetScreenshotTest.kt`, update `package`. Both `emptyState()`/`errorState()` tests change `state = LoginSheetUiState(...)` to `state = LoginSheetState(...)` and the 5 callbacks to `onIntent = {}, onGoogleSignInClick = {}`.

- [ ] **Step 14: Update `LoginSheetCatalog.kt`**

Same substitution (`LoginSheetUiState` → `LoginSheetState`, 5 callbacks → `onIntent = {}, onGoogleSignInClick = {}`) across its 3 `LoginSheetContent(...)` calls; update imports.

- [ ] **Step 15: Fix the `AuthModule.kt` registration**

Update `di/modules/AuthModule.kt`'s `import io.redigit.skimbyte.auth.ui.LoginSheetViewModel` to `io.redigit.skimbyte.ui.screens.auth.LoginSheetViewModel`.

- [ ] **Step 16: Move and finish rewiring `AccountNavHost.kt`**

`git mv auth/ui/AccountNavHost.kt ui/screens/auth/AccountNavHost.kt`, update `package` to `io.redigit.skimbyte.ui.screens.auth`. `AccountViewModel`, `AccountRoute`, `AccountIntent`, `ImageViewerScreen`, `AVATAR_SHARED_ELEMENT_KEY`, `BANNER_SHARED_ELEMENT_KEY`, `LoginSheet` are now all same-package — drop the now-unnecessary cross-package imports added in Task 10 Step 13. Leave the `io.redigit.skimbyte.settings.*`, `io.redigit.skimbyte.onboarding.*`, `io.redigit.skimbyte.nav.*` imports completely untouched — this file's wiring of `SettingsScreen`/`SettingsViewModel` and `TopicsScreen`/`TopicsViewModel` does not change.

- [ ] **Step 17: Update `App.kt`**

Change `import io.redigit.skimbyte.auth.ui.AccountNavHost` to `import io.redigit.skimbyte.ui.screens.auth.AccountNavHost`.

- [ ] **Step 18: Delete stale goldens and regenerate**

Run: `rm shared/src/androidHostTest/roborazzi/io.redigit.skimbyte.auth.ui.LoginSheetScreenshotTest.*.png`
Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "io.redigit.skimbyte.ui.screens.auth.LoginSheetScreenshotTest"`
Expected: two new PNGs under `io.redigit.skimbyte.ui.screens.auth.LoginSheetScreenshotTest.{emptyState,errorState}.png`.

- [ ] **Step 19: Verify clean**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest --tests "io.redigit.skimbyte.ui.screens.auth.LoginSheetScreenshotTest"`
Expected: PASS.

- [ ] **Step 20: Delete old files, build, run full test suite**

Delete `auth/ui/LoginSheetUiState.kt`, old `commonTest/auth/ui/LoginSheetViewModelTest.kt`, old `jvmTest/auth/ui/LoginSheetContentTest.kt`. Confirm `auth/ui/` and `auth/` no longer exist (`find shared/src/commonMain/kotlin/io/redigit/skimbyte/auth` should report no such file or directory; remove the empty directory if `git mv` left one behind).
Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest :androidApp:assembleDebug`
Expected: PASS — the module compiles fully and `auth/` is gone.

- [ ] **Step 21: Commit**

```bash
git add -A shared/src designsheet/src
git commit -m "refactor: convert LoginSheet feature to MVI, finish moving auth to ui/screens/auth"
```

---

## Task 12: Final cleanup and full verification

**Files:** none created; verification only.

- [ ] **Step 1: Confirm `network/` contains only the demo module**

Run: `ls shared/src/commonMain/kotlin/io/redigit/skimbyte/network/`
Expected: only `DemoNetworkModule.kt` (no `dto/` subdirectory — both `ErrorDto.kt` and `PageDto.kt` moved in Task 1).

- [ ] **Step 2: Grep for any remaining stale references**

Run: `grep -rn "io\.redigit\.skimbyte\.auth\.\|io\.redigit\.skimbyte\.network\.ApiConfig\|io\.redigit\.skimbyte\.network\.Page\b\|io\.redigit\.skimbyte\.network\.dto\.PageDto\|io\.redigit\.skimbyte\.network\.dto\.ErrorDto" --include=*.kt shared/src androidApp/src designsheet/src`
Expected: no matches (the only legitimate remaining `io.redigit.skimbyte.network.*` references are to `DemoNetworkModule`/`demoNetworkModule`).

- [ ] **Step 3: Compile every target, not just JVM/Android**

Run: `./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs :shared:iosSimulatorArm64Test :shared:jvmTest :shared:testAndroidHostTest`
Expected: PASS on every target.

- [ ] **Step 4: Verify Roborazzi is clean**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest`
Expected: PASS, and `git status shared/src/androidHostTest/roborazzi/` shows no untracked/orphaned PNGs under old `io.redigit.skimbyte.auth.ui.*` qualified names.

- [ ] **Step 5: Exercise the real Koin graph**

Run: `./gradlew :desktopApp:run`, navigate to the account tab, confirm the screen renders (logged-out state is fine — no live backend needed), then stop the process.
Expected: no Koin `NoDefinitionFoundException`/`IllegalStateException` at startup or when opening the account tab (this is the only check in the whole project that exercises `authModule`/`networkModule`/`AccountViewModel`/`LoginSheetViewModel` through the real DI graph rather than test fakes — see Review Focus).

- [ ] **Step 6: Full test suite one more time**

Run: `./gradlew :shared:testAndroidHostTest :shared:jvmTest :shared:jsTest :shared:wasmJsTest`
Expected: PASS.

- [ ] **Step 7: Commit (only if Steps 1–6 required any fixes)**

```bash
git add -A
git commit -m "chore: final cleanup after auth MVI restructure"
```
