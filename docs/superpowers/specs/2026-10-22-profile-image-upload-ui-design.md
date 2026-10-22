# Profile image upload (client UI) — design

## Intent and why

the auth server's `POST /me/image` upload endpoint and `GET /me`'s `avatarUrl`/`bannerUrl`
fields are live and tested end-to-end. Skimbyte's `ProfileRepository` already
fetches and resolves those URLs, but nothing displays them and nothing calls
the upload endpoint — `AccountScreen` has no avatar/banner UI at all. This
closes that gap: a user can see their current avatar/banner on the Account
screen and change either by picking a new image from their device.

## Goals

- Show the user's avatar and banner on `AccountScreen` when logged in.
- Let the user tap the avatar to pick a new image from their device and
  upload it as their avatar. Same mechanism for the banner.
- Support this on all five targets: Android, iOS, Desktop (JVM), Web (JS),
  Web (Wasm) — a real native picker on each, not a stub on some.
- Surface upload progress/failure in the UI (so a failed upload doesn't look
  like nothing happened).

## Non-goals

- No image cropping, rotation, or client-side resizing/compression — upload
  exactly what the platform picker returns (server already rejects >3MB or
  disallowed types with a clear error; client just surfaces that error).
- No gallery/history of past avatars — each upload just replaces what's shown
  (server already keeps old Spaces objects around per its own design; not
  surfaced here).
- No drag-and-drop on Web — a click-to-pick `<input type="file">` is enough
  for this pass.

## Constraints (from CLAUDE.md)

- Every composable needs a `@Preview` (light + dark minimum).
- State hoisted to a ViewModel exposing `StateFlow<UiState>`; composables
  stateless where possible.
- DI via Koin.
- New/changed shared components reflected in the `designsheet` module in the
  same change.
- Adaptive layout (material3-adaptive / Navigation 3's `ListDetailSceneStrategy`),
  no hardcoded breakpoints, no nested scrollables in the same orientation.
- Every new `expect` needs a matching `actual` in every platform source set
  (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`) or the
  corresponding target fails to compile.
- Unit test for logic, Compose UI test for the interactive composable,
  Roborazzi screenshot test for the shared design-system component (the
  avatar-with-edit-badge piece).

## Decided design

**Picker shape.** Picking an image is inherently asynchronous and, on
Android and iOS, must be launched from a `@Composable` scope (Android's
`ActivityResultContracts.PickVisualMedia` is only usable via
`rememberLauncherForActivityResult`, which is lifecycle-bound; iOS's
`PHPickerViewController` is presented via a UIViewController and reports back
through a delegate). Desktop and Web could get away with a plain suspend
function, but the `expect`/`actual` signature must be identical across all
five targets, so the shape that works everywhere wins:

```kotlin
data class PickedImage(val bytes: ByteArray, val mimeType: String)

@Composable
expect fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit
```

Mirrors Android's own `rememberLauncherForActivityResult` naming — returns a
zero-arg `launch()` lambda; `onResult` receives the picked bytes/mime type, or
`null` on cancellation. Lives at
`shared/src/commonMain/kotlin/io/redigit/skimbyte/util/ImagePicker.kt`,
alongside the existing `UrlLauncher.kt` (same cross-platform-native-action
pattern already used for `openUrl`).

**Upload API.** New method on `AuthApi`:
```kotlin
suspend fun uploadImage(type: String, image: PickedImage): MeResponseDto
```
POSTs multipart (`type` form field + `file` part) to
`${ApiConfig.AUTH_BASE_URL}/me/image`, using the same `authHttpClient` (bearer
token already attached). `type` must be the literal string `"avatar"` or
`"banner"` — confirmed directly against the auth server's `ProfileImageService`
(`io.redigit.auth.profile.ProfileImageService.upload`), which throws
`AuthException.BadRequest` for anything else. Multipart itself needs no new
dependency: Skimbyte's Ktor client engines (OkHttp on Android, Darwin on iOS,
CIO on JVM/Desktop, the Js engine on both JS and Wasm targets) all support
`MultiPartFormDataContent`/`formData {}` out of the box — this is core
`ktor-client-core` functionality, not an engine-specific add-on. the auth server's response is `ImageUploadResponse
(avatarUrl, bannerUrl)` — only one is non-null per the brief's exact
contract — but `ProfileRepository` needs the *other* field preserved too
(e.g. uploading a new avatar must not blank out the existing banner in
local state), so the repository method re-fetches `/me` after a successful
upload rather than trying to merge the partial `ImageUploadResponse` into
the existing `Profile`. Simpler and guaranteed consistent with the server's
actual stored state.

**`ProfileRepository` addition:**
```kotlin
suspend fun uploadAvatar(image: PickedImage): Result<Unit>
suspend fun uploadBanner(image: PickedImage): Result<Unit>
```
Each first checks `image.bytes.size` against a `MAX_IMAGE_BYTES` constant
(3 MB, matching the auth server's own limit exactly) and returns `Result.failure`
immediately — no network call — if it's over, so an oversized image (e.g. a
48MP phone photo) never gets uploaded just to be rejected; the picker
`actual`s still read the full image into memory first (no streaming size
cap during the read itself — that's unchanged), but at least no network
round-trip happens for an image that's already known to be too big.
Otherwise calls `authApi.uploadImage(...)`, then re-fetches `/me` and
updates the held `Profile` on success; returns `Result.failure` on error
without touching the current `Profile` (so a failed upload doesn't wipe out
a previously-good avatar from the UI).

**UI.** `AccountScreen` gains:
- An avatar (circular image, `AsyncImage`-equivalent already in use elsewhere
  in this codebase for `imageUrl` — reuse that mechanism) with a small
  edit/camera badge overlaid in the bottom-right corner. Tapping anywhere on
  the avatar triggers the picker (`rememberImagePickerLauncher`), then calls
  `uploadAvatar`.
- A banner strip above/behind the avatar (simple full-width image), tappable
  the same way, calling `uploadBanner`.
- A small inline loading indicator over whichever image is mid-upload, and a
  transient error (snackbar or inline text) on failure — exact placement is
  left to the builder, matching existing error-surfacing conventions
  elsewhere in the app (check how `LoginSheetViewModel` surfaces its errors
  and mirror that).
- New `AccountUiState` (or extend whatever state shape `AccountScreen`'s
  ViewModel already needs) carrying `profile: Profile?`,
  `avatarUploadInProgress: Boolean`, `bannerUploadInProgress: Boolean`, and an
  error message slot for each.

**Designsheet.** Add the avatar-with-edit-badge component to the catalog as
its own entry (it's the one genuinely new shared visual piece here), with
light/dark previews and states (idle / uploading / with-image / no-image
placeholder).

## What's left to the builder

- Exact platform API calls per `actual`:
  - Android: `ActivityResultContracts.PickVisualMedia()` (API 33+ Photo
    Picker, with the existing media-store fallback Android already handles
    automatically via this contract) + reading bytes from the returned `Uri`
    via `ContentResolver`.
  - iOS: `PHPickerViewController` (modern `PHPicker`, not the deprecated
    `UIImagePickerController`), presented via whatever UIViewController
    interop pattern this codebase already uses for other native UI, bytes
    read via `PHPickerResult`'s `NSItemProvider`.
  - Desktop (JVM): AWT `FileDialog` or Compose Desktop's file-chooser API
    (whichever is already available in this project's dependencies —
    check before adding a new one), run off the main thread.
  - Web (JS/Wasm): a hidden `<input type="file" accept="image/*">`,
    triggered programmatically, bytes read via `FileReader`; JS and WasmJS
    targets may need separate `actual`s if the DOM API surface differs
    between them in this project's Kotlin/JS vs Kotlin/Wasm setup — check
    existing `UrlLauncher.js.kt` / `UrlLauncher.wasmJs.kt` for whether they
    already share code or are fully separate.
- Exact `mimeType` detection per platform (Android `ContentResolver.getType`,
  iOS `UTType`, Desktop file extension sniffing, Web `File.type`).
- Confirmed: `AccountScreen` currently has no ViewModel at all — it's a
  stateless composable taking `isLoggedIn: Boolean` plus callbacks, and
  `AccountRoute` just injects `AuthRepository` directly via `koinInject()`
  for the logged-in check. A fresh `AccountViewModel` needs introducing
  (exposing `AccountUiState` as a `StateFlow`), following the same
  shape/conventions as `LoginSheetViewModel`.
- Test coverage specifics: unit tests for `ProfileRepository.uploadAvatar`/
  `uploadBanner` (success, failure-leaves-profile-unchanged, re-fetch
  behavior) using the existing fake-`AuthApi` pattern; Compose UI test for
  tap-to-trigger-picker on the avatar composable; Roborazzi screenshots for
  the avatar-with-badge component's states.
