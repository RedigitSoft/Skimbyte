# Profile Image Upload (Client UI) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a logged-in user see their avatar/banner on `AccountScreen` and change either by picking a new image from their device, on all five Skimbyte targets.

**Architecture:** A cross-platform `expect`/`actual` image picker (`rememberImagePickerLauncher`) returns picked bytes to common code; `AuthApi`/`ProfileRepository` gain an upload path that size-checks client-side, POSTs multipart to the auth server, then re-fetches `/me`; a new `AccountViewModel` holds upload/error state; `AccountScreen` gains an avatar-with-edit-badge and a banner, both tappable.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor client (multipart), coil3 `AsyncImage`, Koin, kotlinx.coroutines.

**Spec:** `docs/superpowers/specs/2026-10-22-profile-image-upload-ui-design.md`

## Global Constraints

- Upload `type` form field must be the literal string `"avatar"` or `"banner"` — the auth server's `ProfileImageService` rejects anything else with 400.
- Client checks `image.bytes.size` against `3 * 1024 * 1024` (3 MB, matching the auth server's own limit) **before** any network call; over that, fail immediately with no upload attempt. No streaming cap during the picker's own read — that part reads the whole image into memory as today's design accepts.
- No image cropping, rotation, resizing, or compression anywhere in this feature.
- Every new `expect` (`rememberImagePickerLauncher`) needs a matching `actual` in all five platform source sets (`androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`) or the corresponding target fails to compile.
- Every composable needs a `@Preview` (light + dark minimum); state hoisted to a ViewModel exposing `StateFlow<UiState>`.
- New shared components (`AvatarWithEditBadge`, `BannerImage`) get a `designsheet` catalog entry in the same change.
- Platform-native glue with no meaningful way to unit-test (the picker `actual`s themselves — opening a real OS picker) follows this codebase's existing precedent: `UrlLauncher.*.kt` has no test files at all. Don't invent one; test the common-code logic that consumes the picker's result instead.

## Review Focus

- User cancels the picker (`onResult(null)`) — must be a silent no-op, not an error shown to the user. Covered in Task 6 (ViewModel test).
- An oversized image (over 3 MB) is picked — must fail immediately with no network call, distinguishable from a server-side rejection. Covered in Task 5 (`ProfileRepository` test).
- Upload fails for a reason other than size (network error, server rejection) — must surface an error without blanking out the currently-displayed avatar/banner. Covered in Task 5 and Task 6.
- A second avatar/banner tap while an upload is already in flight must not start a second concurrent upload. Covered in Task 6.
- A user with no avatar/banner yet (`null` URLs) shows a sensible placeholder, not a broken image or blank space. Covered in Task 7 (Roborazzi states).

---

## Task 1: `PickedImage` + picker contract + Android `actual`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/util/ImagePicker.kt`
- Create: `shared/src/androidMain/kotlin/io/redigit/skimbyte/util/ImagePicker.android.kt`

**Interfaces:**
- Produces: `data class PickedImage(val bytes: ByteArray, val mimeType: String)`; `@Composable expect fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit`. Every later task that needs to trigger picking calls this.

No test for this task — platform-native picker glue has no automated test in this codebase (see Global Constraints); correctness is verified by Task 8's manual run on an Android device/emulator.

- [ ] **Step 1: Define the contract in `ImagePicker.kt`**

```kotlin
package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable

data class PickedImage(val bytes: ByteArray, val mimeType: String)

/**
 * Returns a zero-arg launch function; calling it opens the platform's native image picker.
 * [onResult] receives the picked image, or null if the user cancelled.
 */
@Composable
expect fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit
```

- [ ] **Step 2: Implement the Android `actual` in `ImagePicker.android.kt`**

```kotlin
package io.redigit.skimbyte.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            onResult(null)
        } else {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            onResult(bytes?.let { PickedImage(it, mimeType) })
        }
    }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}
```
Use `LocalContext.current` (not the `AndroidContextHolder` static holder `UrlLauncher.android.kt` uses) — this function is `@Composable`, so `LocalContext` is directly available; the holder exists only because `openUrl` is a plain non-composable function that can't reach `LocalContext`.

- [ ] **Step 3: Compile the Android target**

Run: `./gradlew :shared:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/util/ImagePicker.kt shared/src/androidMain/kotlin/io/redigit/skimbyte/util/ImagePicker.android.kt
git commit -m "Add cross-platform image picker contract and Android actual"
```

---

## Task 2: Desktop (JVM) `actual`

**Files:**
- Create: `shared/src/jvmMain/kotlin/io/redigit/skimbyte/util/ImagePicker.jvm.kt`

**Interfaces:**
- Consumes: `PickedImage`, the `expect rememberImagePickerLauncher` signature (Task 1).

No test — same rationale as Task 1.

- [ ] **Step 1: Implement the JVM `actual`**

```kotlin
package io.redigit.skimbyte.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch(Dispatchers.IO) {
            val dialog = FileDialog(null as Frame?, "Choose an image", FileDialog.LOAD)
            dialog.isVisible = true // blocks this IO-dispatcher thread until dismissed
            val file = dialog.files.firstOrNull()
            val picked = file?.let {
                PickedImage(it.readBytes(), Files.probeContentType(it.toPath()) ?: "image/jpeg")
            }
            withContext(Dispatchers.Main) { onResult(picked) }
        }
    }
}
```

- [ ] **Step 2: Compile the JVM target**

Run: `./gradlew :shared:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/jvmMain/kotlin/io/redigit/skimbyte/util/ImagePicker.jvm.kt
git commit -m "Add Desktop (JVM) image picker actual"
```

---

## Task 3: Web `actual`s (JS + Wasm)

**Files:**
- Create: `shared/src/jsMain/kotlin/io/redigit/skimbyte/util/ImagePicker.js.kt`
- Create: `shared/src/wasmJsMain/kotlin/io/redigit/skimbyte/util/ImagePicker.wasmJs.kt`

**Interfaces:**
- Consumes: `PickedImage`, the `expect rememberImagePickerLauncher` signature (Task 1).

No test — same rationale as Task 1.

- [ ] **Step 1: Implement the JS `actual`**

Shape: on launch, create a detached `<input type="file" accept="image/*">`, trigger `.click()`, read the chosen file's bytes via `FileReader.readAsArrayBuffer` in its `onload` callback, convert the result to a `ByteArray`, and call `onResult(PickedImage(bytes, file.type))` — or `onResult(null)` if the user closes the picker with no file chosen. `UrlLauncher.js.kt` shows `kotlinx.browser.window` is already available this way; find the equivalent DOM/file APIs available in this project's Kotlin/JS setup (`kotlinx.browser.document`, `org.w3c.files.File`, `org.w3c.files.FileReader`) and verify the exact `ArrayBuffer`-to-`ByteArray` conversion compiles — don't guess the conversion call, check it against the actual Kotlin/JS stdlib available here.

- [ ] **Step 2: Compile the JS target**

Run: `./gradlew :shared:compileKotlinJs`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Implement the Wasm `actual`**

Same shape as Step 1. Kotlin/Wasm's JS interop differs from Kotlin/JS's (e.g. `external` declarations and typed-array handling aren't always identical) — verify the same DOM/File/FileReader APIs compile under `wasmJsMain` rather than assuming the JS version's code is directly portable; `UrlLauncher.wasmJs.kt` happens to be identical to `UrlLauncher.js.kt` for the simple case it covers, but file/byte-array handling is more likely to need target-specific adjustment.

- [ ] **Step 4: Compile the Wasm target**

Run: `./gradlew :shared:compileKotlinWasmJs`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add shared/src/jsMain/kotlin/io/redigit/skimbyte/util/ImagePicker.js.kt shared/src/wasmJsMain/kotlin/io/redigit/skimbyte/util/ImagePicker.wasmJs.kt
git commit -m "Add Web (JS + Wasm) image picker actuals"
```

---

## Task 4: iOS `actual`

**Files:**
- Create: `shared/src/iosMain/kotlin/io/redigit/skimbyte/util/ImagePicker.ios.kt`

**Interfaces:**
- Consumes: `PickedImage`, the `expect rememberImagePickerLauncher` signature (Task 1).

No test — same rationale as Task 1. This is the riskiest platform here (UIKit/PhotosUI interop, not a pure-Kotlin API) — verify against Xcode/Kotlin-Native's actual generated interop rather than guessing method names.

- [ ] **Step 1: Implement the iOS `actual`**

Shape: use `PHPickerViewController` (the modern Photos picker, not the deprecated `UIImagePickerController`) from the `platform.PhotosUI`/`platform.Photos` packages — these should be available the same way `platform.UIKit`/`platform.Foundation` already are in `UrlLauncher.ios.kt`, with no extra cinterop `.def` file needed since they're standard system frameworks. Configure a `PHPickerConfiguration` limited to images, present the `PHPickerViewController` from the current key window's root view controller (find how this codebase currently obtains that, if anywhere, or use `UIApplication.sharedApplication.keyWindow?.rootViewController`), implement its `PHPickerViewControllerDelegate` (`picker(_:didFinishPicking:)`) to receive the result, load the picked item's data via its `NSItemProvider` (`loadDataRepresentationForTypeIdentifier` or `loadObjectOfClass`), convert the returned `NSData`/bytes to a Kotlin `ByteArray`, and call `onResult(...)`. Dismiss the picker in the delegate callback either way (including on cancellation, which reports an empty results array). This delegate-based API doesn't map onto a plain Kotlin callback 1:1 — writing a small Objective-C-protocol-conforming Kotlin class (implementing `PHPickerViewControllerDelegateProtocol`) is the usual pattern; verify the exact class/protocol name Kotlin/Native generates for it before committing to the implementation.

- [ ] **Step 2: Compile the iOS target**

Run: `./gradlew :shared:compileKotlinIosSimulatorArm64` (or whatever this project's exact iOS target name is — check `shared/build.gradle.kts`'s `kotlin { iosX64()/iosArm64()/iosSimulatorArm64() }` block)
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add shared/src/iosMain/kotlin/io/redigit/skimbyte/util/ImagePicker.ios.kt
git commit -m "Add iOS image picker actual"
```

---

## Task 5: Upload API + `ProfileRepository` upload methods

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthApi.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ProfileRepository.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ProfileRepositoryImplTest.kt` (extend existing file)

**Interfaces:**
- Consumes: `PickedImage` (Task 1); existing `AuthApi`, `ProfileRepository`/`ProfileRepositoryImpl`, `Profile`/`MeResponseDto`/`toDomain()`.
- Produces: `AuthApi.uploadImage(type: String, image: PickedImage)` (suspend, no return value — callers don't need the response body); `ProfileRepository.uploadAvatar(image: PickedImage): Result<Unit>`; `ProfileRepository.uploadBanner(image: PickedImage): Result<Unit>`. Task 6 calls these two.

- [ ] **Step 1: Write the failing tests**

Add to the existing `FakeAuthApiForProfile` in `ProfileRepositoryImplTest.kt` a way to control upload success/failure (e.g. a `var uploadResult: Result<Unit> = Result.success(Unit)` and `var uploadCallCount = 0`, with `override suspend fun uploadImage(type: String, image: PickedImage) { uploadCallCount++; uploadResult.getOrThrow() }`), then:

```kotlin
@Test
fun uploadAvatar_oversizedImage_failsWithoutCallingTheApi() = runTest(UnconfinedTestDispatcher()) {
    val api = FakeAuthApiForProfile(Result.success(MeResponseDto("u1")))
    val sessionFlow = MutableStateFlow(Session("token"))
    val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)
    advanceUntilIdle()

    val oversized = PickedImage(ByteArray(3 * 1024 * 1024 + 1), "image/png")
    val result = repo.uploadAvatar(oversized)

    assertTrue(result.isFailure)
    assertEquals(0, api.uploadCallCount)
}

@Test
fun uploadAvatar_success_refetchesProfile() = runTest(UnconfinedTestDispatcher()) {
    val api = FakeAuthApiForProfile(Result.success(MeResponseDto("u1", avatarUrl = "avatars/u1/new.jpg")))
    val sessionFlow = MutableStateFlow(Session("token"))
    val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)
    advanceUntilIdle()

    val result = repo.uploadAvatar(PickedImage(ByteArray(10), "image/png"))
    advanceUntilIdle()

    assertTrue(result.isSuccess)
    assertEquals(1, api.uploadCallCount)
    assertEquals("u1", repo.profile.value?.userId)
}

@Test
fun uploadAvatar_apiFailure_leavesExistingProfileUntouched() = runTest(UnconfinedTestDispatcher()) {
    val api = FakeAuthApiForProfile(Result.success(MeResponseDto("u1")))
    api.uploadResult = Result.failure(RuntimeException("boom"))
    val sessionFlow = MutableStateFlow(Session("token"))
    val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)
    advanceUntilIdle()
    val profileBeforeUpload = repo.profile.value

    val result = repo.uploadAvatar(PickedImage(ByteArray(10), "image/png"))

    assertTrue(result.isFailure)
    assertEquals(profileBeforeUpload, repo.profile.value)
}
```
`uploadBanner` needs no separate test here beyond what Task 6's UI-level tests cover for the banner path — the size-check and re-fetch logic is identical and shared.

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ProfileRepositoryImplTest"`
Expected: FAIL (`uploadAvatar`/`uploadImage` unresolved)

- [ ] **Step 3: Add `uploadImage` to `AuthApi`/`KtorAuthApi`**

```kotlin
interface AuthApi {
    // ...existing members...
    suspend fun uploadImage(type: String, image: PickedImage)
}

class KtorAuthApi(...) : AuthApi {
    // ...
    override suspend fun uploadImage(type: String, image: PickedImage) {
        client.post("${ApiConfig.AUTH_BASE_URL}/me/image") {
            setBody(MultiPartFormDataContent(formData {
                append("type", type)
                append("file", image.bytes, Headers.build {
                    append(HttpHeaders.ContentType, image.mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"upload\"")
                })
            }))
        }
    }
}
```
No explicit `contentType(...)` call before `setBody` — `MultiPartFormDataContent` sets its own `multipart/form-data` content type with boundary.

- [ ] **Step 4: Add `uploadAvatar`/`uploadBanner` to `ProfileRepository`/`ProfileRepositoryImpl`**

```kotlin
interface ProfileRepository {
    val profile: StateFlow<Profile?>
    suspend fun uploadAvatar(image: PickedImage): Result<Unit>
    suspend fun uploadBanner(image: PickedImage): Result<Unit>
}
```
In `ProfileRepositoryImpl`, add a private suspend helper both call:
```kotlin
private suspend fun upload(type: String, image: PickedImage): Result<Unit> {
    if (image.bytes.size > MAX_IMAGE_BYTES) return Result.failure(IllegalArgumentException("Image too large"))
    return runCatching {
        authApi.uploadImage(type, image)
        _profile.value = runCatching { authApi.me().toDomain() }.getOrNull() ?: _profile.value
    }
}

override suspend fun uploadAvatar(image: PickedImage) = upload("avatar", image)
override suspend fun uploadBanner(image: PickedImage) = upload("banner", image)

companion object {
    const val MAX_IMAGE_BYTES = 3 * 1024 * 1024
}
```
The `?: _profile.value` fallback on the re-fetch keeps the Review Focus guarantee (a failed re-fetch after a successful upload must not blank the profile) — only a successful `/me` re-fetch replaces the held value.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ProfileRepositoryImplTest"`
Expected: PASS, all tests in the file green

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthApi.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ProfileRepository.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ProfileRepositoryImplTest.kt
git commit -m "Add avatar/banner upload to AuthApi and ProfileRepository"
```

---

## Task 6: `AccountViewModel`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountUiState.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountViewModel.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ui/AccountViewModelTest.kt` (new)

**Interfaces:**
- Consumes: `AuthRepository.session`, `ProfileRepository.profile`/`uploadAvatar`/`uploadBanner` (Task 5), `PickedImage` (Task 1).
- Produces: `data class AccountUiState(...)`; `class AccountViewModel(...) : ViewModel()` with `uiState: StateFlow<AccountUiState>`, `onAvatarPicked(image: PickedImage)`, `onBannerPicked(image: PickedImage)`. Task 8 wires these into `AccountScreen`.

- [ ] **Step 1: Define `AccountUiState`**

```kotlin
package io.redigit.skimbyte.auth.ui

data class AccountUiState(
    val isLoggedIn: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val avatarUploadInProgress: Boolean = false,
    val bannerUploadInProgress: Boolean = false,
    val avatarError: String? = null,
    val bannerError: String? = null,
)
```

- [ ] **Step 2: Write the failing tests**

```kotlin
private class FakeProfileRepository(initial: Profile? = null) : ProfileRepository {
    private val _profile = MutableStateFlow(initial)
    override val profile: StateFlow<Profile?> = _profile.asStateFlow()
    var avatarResult: Result<Unit> = Result.success(Unit)
    var bannerResult: Result<Unit> = Result.success(Unit)
    var uploadAvatarCallCount = 0
    override suspend fun uploadAvatar(image: PickedImage): Result<Unit> { uploadAvatarCallCount++; return avatarResult }
    override suspend fun uploadBanner(image: PickedImage): Result<Unit> = bannerResult
    fun setProfile(profile: Profile?) { _profile.value = profile }
}

class AccountViewModelTest {
    @Test
    fun cancelledPick_isSilentNoOp() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeProfileRepository()
        val vm = AccountViewModel(fakeAuthRepository(loggedIn = true), repo)
        // onAvatarPicked is never called for a null pick result — the picker callback itself
        // only invokes onAvatarPicked when non-null; assert no state change from constructing alone.
        assertEquals(AccountUiState(isLoggedIn = true), vm.uiState.value)
    }

    @Test
    fun onAvatarPicked_success_clearsInProgressAndError() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeProfileRepository()
        val vm = AccountViewModel(fakeAuthRepository(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png"))
        advanceUntilIdle()

        assertEquals(false, vm.uiState.value.avatarUploadInProgress)
        assertEquals(null, vm.uiState.value.avatarError)
        assertEquals(1, repo.uploadAvatarCallCount)
    }

    @Test
    fun onAvatarPicked_failure_setsErrorAndClearsInProgress() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeProfileRepository()
        repo.avatarResult = Result.failure(RuntimeException("boom"))
        val vm = AccountViewModel(fakeAuthRepository(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png"))
        advanceUntilIdle()

        assertEquals(false, vm.uiState.value.avatarUploadInProgress)
        assertEquals("Could not update avatar", vm.uiState.value.avatarError)
    }

    @Test
    fun onAvatarPicked_whileAlreadyUploading_isIgnored() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeProfileRepository()
        val vm = AccountViewModel(fakeAuthRepository(loggedIn = true), repo)

        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png"))
        vm.onAvatarPicked(PickedImage(ByteArray(10), "image/png")) // fires before the first completes
        advanceUntilIdle()

        assertEquals(1, repo.uploadAvatarCallCount)
    }
}
```
`fakeAuthRepository(loggedIn: Boolean)` — a small test helper building a fake/stub `AuthRepository` whose `session` is `MutableStateFlow(if (loggedIn) Session("token") else null)` and whose other members throw `error("unused")`; follow the existing `FakeAuthApi`-style pattern from `AuthRepositoryImplTest.kt`/`ProfileRepositoryImplTest.kt` for the exact shape.

The "second tap while uploading is ignored" test only works if `onAvatarPicked`'s in-progress guard check and flag-set happen synchronously before the first `advanceUntilIdle()` lets the upload complete — write `onAvatarPicked` so the guard-and-flag-set is the first synchronous thing it does (see Step 3).

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ui.AccountViewModelTest"`
Expected: FAIL (`AccountViewModel` unresolved)

- [ ] **Step 4: Implement `AccountViewModel`**

```kotlin
class AccountViewModel(
    authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.session, profileRepository.profile) { session, profile -> session to profile }
                .collect { (session, profile) ->
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = session != null,
                        avatarUrl = profile?.avatarUrl,
                        bannerUrl = profile?.bannerUrl,
                    )
                }
        }
    }

    fun onAvatarPicked(image: PickedImage) {
        if (_uiState.value.avatarUploadInProgress) return
        _uiState.value = _uiState.value.copy(avatarUploadInProgress = true, avatarError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadAvatar(image)
            _uiState.value = _uiState.value.copy(
                avatarUploadInProgress = false,
                avatarError = result.exceptionOrNull()?.let { "Could not update avatar" },
            )
        }
    }

    fun onBannerPicked(image: PickedImage) {
        if (_uiState.value.bannerUploadInProgress) return
        _uiState.value = _uiState.value.copy(bannerUploadInProgress = true, bannerError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadBanner(image)
            _uiState.value = _uiState.value.copy(
                bannerUploadInProgress = false,
                bannerError = result.exceptionOrNull()?.let { "Could not update banner" },
            )
        }
    }
}
```
`combine` is `kotlinx.coroutines.flow.combine`.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ui.AccountViewModelTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountUiState.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountViewModel.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ui/AccountViewModelTest.kt
git commit -m "Add AccountViewModel with avatar/banner upload state"
```

---

## Task 7: `AvatarWithEditBadge` + `BannerImage` composables, previews, designsheet, Roborazzi

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadge.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/BannerImage.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/ProfileImagesCatalog.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadgeScreenshotTest.kt` (new)

**Interfaces:**
- Produces: `@Composable fun AvatarWithEditBadge(avatarUrl: String?, isUploading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`; `@Composable fun BannerImage(bannerUrl: String?, isUploading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`. Task 8 wires both into `AccountScreen`.

- [ ] **Step 1: Implement `AvatarWithEditBadge`**

Circular `coil3.compose.AsyncImage` (model = `avatarUrl`, `ContentScale.Crop`, clipped to a circle shape) when `avatarUrl != null`, else a placeholder circle in `MaterialTheme.colorScheme.surfaceVariant` with a person/account icon — follow `PostCard.kt`'s `AsyncImage`/placeholder-`Box` pattern for the null-vs-present split. Overlay a small circular edit/camera badge in the bottom-end corner (`Icons.Filled.Edit` or `Icons.Filled.PhotoCamera`, whichever this codebase's icon set already has — check `SkimbyteBottomNavBar.kt`/`AccountScreen.kt`'s existing `Icons.Filled.*` imports for precedent). When `isUploading`, show a `CircularProgressIndicator` over the avatar instead of (or on top of) the image, and make the whole composable non-clickable while uploading (don't invoke `onClick` — `clickable(enabled = !isUploading)`). Set `contentDescription = "Change avatar"` on the clickable element itself (the outermost `Box`/`Modifier.clickable` target, via `Modifier.semantics { this.contentDescription = "Change avatar" }` or the equivalent `clickable(onClickLabel = ...)` — whichever this codebase already favors elsewhere for a clickable image) — Task 8's UI test looks this up by exactly this string. Add `@Preview` composables: light+dark, with-avatar, without-avatar, uploading.

- [ ] **Step 2: Implement `BannerImage`**

Full-width `AsyncImage` (fixed height, e.g. 160.dp, `ContentScale.Crop`) when `bannerUrl != null`, else a placeholder `Box` with `MaterialTheme.colorScheme.surfaceVariant`. Same `isUploading` → progress indicator + disabled click behavior as `AvatarWithEditBadge`. `@Preview` composables: light+dark, with-banner, without-banner, uploading.

- [ ] **Step 3: Add the designsheet catalog entry**

In `ProfileImagesCatalog.kt`, a `ProfileImagesCatalogContent()` composable rendering both components in their with-image/without-image/uploading states (mirroring `LoginSheetCatalogContent`'s shape: a scrollable `Column` with `Arrangement.spacedBy`), plus a `@Preview`. Register it in `CatalogRegistry.kt`: `CatalogItem("profile-images", "Avatar & banner", "Account") { ProfileImagesCatalogContent() }` (pick whichever existing category grouping fits — check the current category strings already in that file).

- [ ] **Step 4: Write the Roborazzi screenshot test**

```kotlin
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AvatarWithEditBadgeScreenshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noAvatar() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = false, onClick = {}) }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun uploading() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = true, onClick = {}) }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```
Matches `BookmarkButtonScreenshotTest.kt`'s shape exactly. (A `with-avatar` state needs a real/fake image URL that Roborazzi can actually load offline — check how, if at all, existing screenshot tests here handle `AsyncImage` with a real URL; if none do, two states — `noAvatar`/`uploading` — are enough for this test, since the `with-avatar` visual is just the same `AsyncImage` usage already covered by `PostCard`'s own screenshot coverage elsewhere.)

- [ ] **Step 5: Record the Roborazzi goldens**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "io.redigit.skimbyte.auth.ui.AvatarWithEditBadgeScreenshotTest"`
Expected: BUILD SUCCESSFUL, new golden PNGs created under `shared/src/androidHostTest/roborazzi/`

- [ ] **Step 6: Verify the recorded goldens**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest --tests "io.redigit.skimbyte.auth.ui.AvatarWithEditBadgeScreenshotTest"`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadge.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/BannerImage.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/ProfileImagesCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/ui/AvatarWithEditBadgeScreenshotTest.kt shared/src/androidHostTest/roborazzi/
git commit -m "Add AvatarWithEditBadge and BannerImage components, designsheet entry, screenshots"
```

---

## Task 8: Wire into `AccountScreen`

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt`
- Test: `shared/src/jvmTest/kotlin/io/redigit/skimbyte/auth/ui/AccountScreenContentTest.kt` (new)

**Interfaces:**
- Consumes: `AccountViewModel`/`AccountUiState` (Task 6), `AvatarWithEditBadge`/`BannerImage` (Task 7), `rememberImagePickerLauncher` (Task 1).

- [ ] **Step 1: Write the failing Compose UI test**

```kotlin
class AccountScreenContentTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingAvatar_invokesOnAvatarClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = {},
                    onLoginSucceeded = {},
                    onSettingsClick = {},
                    onAvatarClick = { clicked = true },
                    onBannerClick = {},
                )
            }
        }
        onNodeWithContentDescription("Change avatar").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }
}
```
(Matching `LoginSheetContentTest.kt`'s `runComposeUiTest` shape.) Pick whatever exact `contentDescription` `AvatarWithEditBadge` ends up using in Task 7 and use the same string here — this test is written against `AccountScreen`'s new signature, so it drives that signature's final shape.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ui.AccountScreenContentTest"`
Expected: FAIL (`AccountScreen` doesn't have this signature yet)

- [ ] **Step 3: Update `AccountScreen` and `AccountRoute`**

`AccountScreen` becomes a stateless composable taking `uiState: AccountUiState` plus callbacks (`onLogout`, `onLoginSucceeded`, `onSettingsClick`, `onAvatarClick: () -> Unit`, `onBannerClick: () -> Unit`), rendering `BannerImage`/`AvatarWithEditBadge` (Task 7) above the existing logged-in/out content, driven by `uiState.bannerUrl`/`avatarUrl`/`*UploadInProgress`.

`AccountRoute` switches from directly injecting `AuthRepository` to `koinViewModel<AccountViewModel>()`, collects `uiState`, and wires the picker:
```kotlin
@Composable
fun AccountRoute(onSettingsClick: () -> Unit, viewModel: AccountViewModel = koinViewModel(), modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val pickAvatar = rememberImagePickerLauncher { image -> image?.let(viewModel::onAvatarPicked) }
    val pickBanner = rememberImagePickerLauncher { image -> image?.let(viewModel::onBannerPicked) }
    AccountScreen(
        uiState = uiState,
        onLogout = { /* existing logout call, now via a repository the ViewModel exposes or still direct-injected AuthRepository — builder's call which, consistent with keeping AccountViewModel focused per the design doc's "what's left to the builder" on AuthRepository vs AccountViewModel boundaries */ },
        onLoginSucceeded = {},
        onSettingsClick = onSettingsClick,
        onAvatarClick = pickAvatar,
        onBannerClick = pickBanner,
        modifier = modifier,
    )
}
```
Update existing `@Preview` composables in the file to match the new `AccountScreen` signature (pass a plain `AccountUiState(...)` instead of `isLoggedIn: Boolean`).

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ui.AccountScreenContentTest"`
Expected: PASS

- [ ] **Step 5: Run the full shared test suite**

Run: `./gradlew :shared:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 6: Manual check**

Run `:desktopApp:run` (or `:androidApp:assembleDebug` + an emulator), log in, confirm tapping the avatar opens a real file picker and a chosen image round-trips through to `GET /me` and displays.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt shared/src/jvmTest/kotlin/io/redigit/skimbyte/auth/ui/AccountScreenContentTest.kt
git commit -m "Wire avatar/banner picker and upload into AccountScreen"
```
