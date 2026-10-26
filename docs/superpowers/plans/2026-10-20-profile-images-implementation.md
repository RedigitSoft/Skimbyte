# Profile Images (Skimbyte data layer) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fetch the logged-in user's profile (avatar/banner paths) from the auth server's `GET /me` and expose it as app state, with raw bucket paths resolved into displayable CDN URLs. No new UI.

**Architecture:** A new `ProfileRepository`, parallel to the existing `AuthRepository`, observes `AuthRepository.session` and calls a new `AuthApi.me()` whenever a session appears, holding the result in a `StateFlow<Profile?>`. A new `ApiConfig.resolveSpacesUrl` mirrors the existing `resolveImageUrl` pattern but points at the Spaces CDN instead of skimbyte-server's asset base.

**Tech Stack:** Kotlin Multiplatform, Ktor client (existing `authHttpClient`), kotlinx.coroutines (`StateFlow`), Koin, kotlinx.serialization.

**Spec:** `docs/superpowers/specs/2026-10-20-profile-images-design.md`

## Global Constraints

- `avatarUrl`/`bannerUrl` from the auth server are always bare bucket-relative paths — resolve them the same way regardless of origin (Google-seeded or user-uploaded); never special-case the source.
- `SPACES_CDN_BASE_URL` defaults to `https://cdn.example.com` (a real, non-secret, public CDN domain) — unlike `authBaseUrl`/`skimbyteApiBaseUrl`, which default to blank in `local.properties.example` because they're per-developer/per-environment.
- the auth server's `avatarUrl`/`bannerUrl` fields on `/me` may not have shipped yet when this lands: the `me()` fetch must fail soft (catch, leave `profile` at `null`, never crash or surface an error) so the two repos can be built and deployed independently.
- No new screen, no upload UI, no auto-refresh on a timer — data layer only, fetched once per session transition.

## Review Focus

- `/me` returning an older response shape (missing `avatarUrl`/`bannerUrl`, e.g. before the auth server ships) must not crash deserialization — covered in Task 2 (fields are nullable/optional in the DTO) and Task 3 (fetch failure path).
- An already-absolute `avatarUrl` (e.g. a future case where the auth server returns a full URL) must pass through `resolveSpacesUrl` unchanged rather than getting double-prefixed — covered in Task 1.
- Logging out while a profile fetch is in flight must not leave a stale `Profile` in the `StateFlow` after logout completes — covered in Task 3.
- Two rapid session transitions (e.g. login immediately followed by logout, or login → logout → login) must leave `profile` consistent with the *last* transition, not a stale response from an earlier in-flight fetch — covered in Task 3.
- A `null` `avatarUrl`/`bannerUrl` (user never uploaded, no Google picture) must map to a `null` resolved URL, not an empty-string or `"nullpath"`-style concatenation — covered in Task 1 and Task 2.

---

## Task 1: `SPACES_CDN_BASE_URL` config and URL resolution

**Files:**
- Modify: `local.properties.example`
- Modify: `shared/build.gradle.kts`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/network/ApiConfig.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/network/ApiConfigTest.kt` (new)

**Interfaces:**
- Produces: `ApiConfig.SPACES_CDN_BASE_URL: String`; `ApiConfig.resolveSpacesUrl(raw: String): String`. Task 2 uses the latter when mapping the DTO to `Profile`.

- [ ] **Step 1: Write the failing test**

```kotlin
class ApiConfigTest {
    @Test
    fun bare path is prefixed with the Spaces CDN base url() {
        assertEquals(
            "${ApiConfig.SPACES_CDN_BASE_URL}/avatars/u1/abc.jpg",
            ApiConfig.resolveSpacesUrl("avatars/u1/abc.jpg"),
        )
    }

    @Test
    fun already absolute url is passed through unchanged() {
        assertEquals("https://other.cdn/x.jpg", ApiConfig.resolveSpacesUrl("https://other.cdn/x.jpg"))
    }
}
```

- [ ] **Step 2: Run it, confirm it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.network.ApiConfigTest"`
Expected: FAIL (`resolveSpacesUrl` doesn't exist).

- [ ] **Step 3: Add `spacesCdnBaseUrl` to the generated config**

In `local.properties.example`, add a commented line near the other backend keys:
```
# DigitalOcean Spaces CDN domain for avatar/banner images — defaults to the real domain below;
# override only if pointing at a different CDN (e.g. staging).
#spacesCdnBaseUrl=""
```
In `shared/build.gradle.kts`'s `generateAuthConfig` task, add `val spacesCdnBaseUrl = props.getProperty("spacesCdnBaseUrl", "https://cdn.example.com")` and add `const val SPACES_CDN_BASE_URL = "$spacesCdnBaseUrl"` to the generated `BackendConfig` object, alongside `API_BASE_URL`.

- [ ] **Step 4: Add the constant and resolver to `ApiConfig.kt`**

```kotlin
const val SPACES_CDN_BASE_URL = BackendConfig.SPACES_CDN_BASE_URL

fun resolveSpacesUrl(raw: String): String =
    if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "$SPACES_CDN_BASE_URL/$raw"
```
Note the `/` separator: `SPACES_CDN_BASE_URL` has no trailing slash and the auth server's stored keys (e.g. `avatars/u1/abc.jpg`) have no leading slash, matching the test in Step 1.

- [ ] **Step 5: Run it, confirm it passes**

Same command as Step 2; expect PASS.

- [ ] **Step 6: Commit**

```bash
git add local.properties.example shared/build.gradle.kts shared/src/commonMain/kotlin/io/redigit/skimbyte/network/ApiConfig.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/network/ApiConfigTest.kt
git commit -m "Add SPACES_CDN_BASE_URL config and resolveSpacesUrl"
```

---

## Task 2: `/me` DTO, API call, and domain model

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/dto/MeResponseDto.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthApi.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/Profile.kt`

**Interfaces:**
- Consumes: `ApiConfig.resolveSpacesUrl` (Task 1).
- Produces: `AuthApi.me(): MeResponseDto`; `data class Profile(val userId: String, val avatarUrl: String?, val bannerUrl: String?)`; `fun MeResponseDto.toDomain(): Profile`. Task 3 calls `authApi.me().toDomain()`.

- [ ] **Step 1: Add `MeResponseDto`**

```kotlin
@Serializable
data class MeResponseDto(
    val userId: String,
    val tenantId: String? = null,
    val sessionId: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
)
```
Mirrors the auth server's `MeResponse` (`routes/Dtos.kt`); defaults make it tolerant of an older server response missing the new fields (Global Constraint, Review Focus item 1).

- [ ] **Step 2: Add `me()` to `AuthApi`/`KtorAuthApi`**

```kotlin
interface AuthApi {
    ...
    suspend fun me(): MeResponseDto
}

class KtorAuthApi(...) : AuthApi {
    ...
    override suspend fun me(): MeResponseDto =
        client.get("${ApiConfig.AUTH_BASE_URL}/me").body()
}
```
Uses the same injected `authHttpClient` as `login`/`register`/`refresh` — its `Auth` plugin already attaches the bearer token.

- [ ] **Step 3: Add `Profile` and its mapper**

```kotlin
data class Profile(val userId: String, val avatarUrl: String?, val bannerUrl: String?)

fun MeResponseDto.toDomain(): Profile = Profile(
    userId = userId,
    avatarUrl = avatarUrl?.let(ApiConfig::resolveSpacesUrl),
    bannerUrl = bannerUrl?.let(ApiConfig::resolveSpacesUrl),
)
```
`?.let(...)` keeps `null` as `null` (Review Focus item 5) rather than resolving a missing path.

- [ ] **Step 4: Commit**

No new tests in this task — `MeResponseDto`/`Profile`/`toDomain()` are exercised through Task 3's repository tests, matching how `Post.kt`'s `toDomain()` has no dedicated test of its own in this codebase.

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/dto/MeResponseDto.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthApi.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/Profile.kt
git commit -m "Add GET /me DTO, API call, and Profile domain model"
```

---

## Task 3: `ProfileRepository`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ProfileRepository.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthModule.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ProfileRepositoryImplTest.kt` (new)

**Interfaces:**
- Consumes: `AuthApi.me()` (Task 2), `AuthRepository.session: StateFlow<Session?>` (existing).
- Produces: `interface ProfileRepository { val profile: StateFlow<Profile?> }`. Nothing downstream yet (no UI wiring in this plan).

- [ ] **Step 1: Write the failing tests**

```kotlin
private class FakeAuthApiForProfile(private val result: Result<MeResponseDto>) : AuthApi {
    override suspend fun login(email: String, password: String) = error("unused")
    override suspend fun register(email: String, password: String) = error("unused")
    override suspend fun refresh(refreshToken: String) = error("unused")
    override suspend fun me(): MeResponseDto = result.getOrThrow()
}

class ProfileRepositoryImplTest {
    @Test
    fun session_becoming_nonNull_fetches_and_populates_profile() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.success(MeResponseDto("u1", avatarUrl = "avatars/u1/a.jpg"))),
            sessionFlow,
            backgroundScope, // kotlinx-coroutines-test's TestScope.backgroundScope
        )
        sessionFlow.value = Session("token")
        advanceUntilIdle()
        assertEquals("u1", repo.profile.value?.userId)
    }

    @Test
    fun session_becoming_null_clears_profile() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(Session("token"))
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.success(MeResponseDto("u1"))),
            sessionFlow,
            backgroundScope,
        )
        advanceUntilIdle()
        sessionFlow.value = null
        advanceUntilIdle()
        assertNull(repo.profile.value)
    }

    @Test
    fun fetch_failure_leaves_profile_null_and_does_not_throw() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.failure(RuntimeException("404"))),
            sessionFlow,
            backgroundScope,
        )
        sessionFlow.value = Session("token")
        advanceUntilIdle()
        assertNull(repo.profile.value)
    }

    @Test
    fun second_login_after_logout_reflects_the_latest_session_profile() = runTest {
        var call = 0
        val api = object : AuthApi {
            override suspend fun login(email: String, password: String) = error("unused")
            override suspend fun register(email: String, password: String) = error("unused")
            override suspend fun refresh(refreshToken: String) = error("unused")
            override suspend fun me(): MeResponseDto {
                call++
                return MeResponseDto("u$call")
            }
        }
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)

        sessionFlow.value = Session("token-1")
        advanceUntilIdle()
        sessionFlow.value = null
        advanceUntilIdle()
        sessionFlow.value = Session("token-2")
        advanceUntilIdle()

        assertEquals("u2", repo.profile.value?.userId)
    }
}
```

- [ ] **Step 2: Run it, confirm it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.auth.ProfileRepositoryImplTest"`
Expected: FAIL (`ProfileRepositoryImpl` doesn't exist).

- [ ] **Step 3: Implement `ProfileRepository`/`ProfileRepositoryImpl`**

```kotlin
interface ProfileRepository {
    val profile: StateFlow<Profile?>
}

class ProfileRepositoryImpl(
    private val authApi: AuthApi,
    session: StateFlow<Session?>,
    scope: CoroutineScope,
) : ProfileRepository {
    private val _profile = MutableStateFlow<Profile?>(null)
    override val profile: StateFlow<Profile?> = _profile.asStateFlow()

    init {
        scope.launch {
            session.collectLatest { current ->
                if (current == null) {
                    _profile.value = null
                } else {
                    _profile.value = runCatching { authApi.me().toDomain() }.getOrNull()
                }
            }
        }
    }
}
```
`collectLatest` (not `collect`) is what makes Review Focus item 4 (second login after logout) correct: a new session value cancels any in-flight fetch for the previous one before starting its own.

- [ ] **Step 4: Run it, confirm it passes**

Same command as Step 2; expect PASS.

- [ ] **Step 5: Wire into Koin**

In `AuthModule.kt`, add:
```kotlin
single<ProfileRepository> { ProfileRepositoryImpl(get(), get<AuthRepository>().session, CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
```
placed after the existing `single<AuthRepository>` registration (it resolves `AuthRepository` from the same Koin graph).

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ProfileRepository.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/AuthModule.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/auth/ProfileRepositoryImplTest.kt
git commit -m "Add ProfileRepository, fetched on session change"
```
