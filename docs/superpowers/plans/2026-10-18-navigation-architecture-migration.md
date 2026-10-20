# Navigation Architecture Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate Skimbyte's existing, organically-grown navigation (3 tabs, two incompatible "detail" screens, a non-adaptive nav bar, no onboarding, no theme choice) onto the target graph in the design doc: 4 independent top-level destinations (Home/Feed, Bookmarks, Search, Account), a single shared `PostDetail` screen reached with a shared-element transition from anywhere, a horizontal swipeable deck with a list-view alternative, first-run topic onboarding with later editing, and a persisted theme switcher.

**Architecture:** This is a migration, not a greenfield build — each task modifies or replaces an existing Navigation 3 screen/nav-host rather than inventing new infrastructure. New local-storage-backed preference stores follow the existing `SessionStorage`/`multiplatform-settings` pattern. The nav shell keeps the project's existing Material 3 Expressive `HorizontalFloatingToolbar` for compact width and adds a `NavigationRail` for medium/expanded, switched by `currentWindowAdaptiveInfo()` — not `NavigationSuiteScaffold`, which has no slot for the floating-toolbar shape already in use.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.12.1, Navigation 3 (`androidx.navigation3` 1.1.1, `material3-adaptive-navigation3` 1.3.0-beta02 for `ListDetailSceneStrategy`), Koin DI, `multiplatform-settings` for local persistence.

**Spec:** `docs/superpowers/specs/2026-10-18-navigation-architecture-design.md`

## Global Constraints

- Navigation is Navigation 3 (`androidx.navigation3`), not Navigation Compose.
- Every screen must support foldables/arbitrary window sizes via `material3-adaptive` + `ListDetailSceneStrategy`; no hardcoded dp breakpoints anywhere — use `currentWindowAdaptiveInfo()`'s window size class.
- No nested scrollables in the same orientation — each of the deck, the list, and the detail page owns its own scrolling.
- DI via Koin only; new dependencies only from `gradle/libs.versions.toml` (no new entries are needed — `multiplatformSettings`, `material3-adaptive*`, `navigation3-ui`, `compose.materialIconsExtended` already cover this work).
- Every new/changed shared composable needs a matching `designsheet` catalog entry in the same change, plus light+dark `@Preview`s.
- Swipe gestures on the deck are purely positional (left = next, right = previous) — no action is ever inferred from swipe direction.
- State is hoisted to a ViewModel exposing immutable `StateFlow<UiState>`, per CLAUDE.md — the plan below adds ViewModels for Topics and Settings rather than local `remember` state, even though their logic is simple.

## Review Focus

- **Topic onboarding boundary counts**: selecting exactly 2 topics must keep Confirm disabled; selecting exactly 3 must enable it (the spec's "more than 2 and less than 5" — covered in Task 5).
- **`ThemeMode.System` tracks the OS live**, not just at launch — changing the system setting while the app is open must recompose the theme (Task 2).
- **Tab-switch state retention**: leaving a tab (e.g. Home → Bookmarks) and returning must preserve that tab's Navigation 3 backstack and scroll/deck position, not reset it — this is a real regression risk from the existing plain `when` switch, which doesn't wrap destinations in a `SaveableStateHolder` (Task 11).
- **`PostDetail`'s bookmark action diverges by origin**: reached from Bookmarks, unbookmarking must remove the post from the list and pop back; reached from Home or Search, it must toggle in place and stay on the detail screen (Tasks 6, 7, 8).
- **Editing topic interests to drop the feed's active filter**: if the user removes the topic Home is currently filtered on, Home must fall back to a remaining selected topic instead of showing an orphaned filter chip (Task 5/11 boundary).

---

## File Structure

**New files:**
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeMode.kt` — `enum class ThemeMode { Light, Dark, System }`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModeStore.kt` — interface + `Settings`-backed impl, mirrors `SessionStorage`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModule.kt` — Koin module
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicPreferencesStore.kt` — interface + `Settings`-backed impl
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/OnboardingModule.kt` — Koin module
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicsUiState.kt`, `TopicsViewModel.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/ui/TopicsScreen.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/ui/SplashScreen.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostDetailScreen.kt` — the unified single-post detail (replaces `SourceDetailScreen` and `BookmarkDetailScreen`)
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchNavHost.kt` — Search's own Navigation 3 host (extracted out of `FeedNavHost`)
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/SearchViewModel.kt`, `SearchUiState.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewMode.kt` — `enum class FeedViewMode { Stack, List }`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/SettingsUiState.kt`, `SettingsViewModel.kt`, `SettingsModule.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/ui/SettingsScreen.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountNavHost.kt` — Account's own Navigation 3 host (new; Account currently has no host at all)
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteNavigationRail.kt`
- `designsheet/.../components/PostDetailScreenCatalog.kt`, `TopicsScreenCatalog.kt`, `SettingsScreenCatalog.kt`, `SkimbyteNavigationRailCatalog.kt`

**Modified files:**
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/SkimbyteNavKeys.kt` — add `Search` to `TopLevelDestination`; add `PostDetail`, `AccountHome`, `Settings`, `Topics`; remove `SourceDetail`, `BookmarkDetail`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteBottomNavBar.kt` — drop `onSearchClick` (Search is now a plain `TopLevelDestination` entry)
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt` — add a `ThemeMode`-based overload of `SkimbyteTheme` alongside the existing `Boolean`-based one
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedUiState.kt`, `FeedViewModel.kt` — add `viewMode: FeedViewMode`, inject `TopicPreferencesStore`, default `selectedTopic` to a stored interest instead of `Topic.All`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedScreen.kt` — horizontal orientation, view-mode toggle, card-wide tap target
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedNavHost.kt` — `PostDetail` instead of `SourceDetail`; Search extracted out
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostCard.kt` — replace `onSourceClick` with whole-card `onCardClick`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/TopicFilterRow.kt` — take the topic list as a parameter instead of hardcoding `Topic.entries`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarksNavHost.kt` — `PostDetail` instead of `BookmarkDetail`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt` — add `onSettingsClick`, becomes `AccountHome`'s content
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/App.kt` — full rewrite: Splash → onboarding gate → adaptive 4-destination shell with `SaveableStateHolder`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt` — include `themeModule`, `onboardingModule`, `settingsModule`
- `designsheet/.../catalog/CatalogRegistry.kt` — swap `source-detail`/`bottom-nav-bar` entries, add new ones

**Deleted files:**
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SourceDetailScreen.kt`
- `shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarkDetailScreen.kt`
- `designsheet/.../components/SourceDetailScreenCatalog.kt`
- Any `*Test.kt` / Roborazzi goldens referencing the two deleted screens

---

## Task 1: `ThemeMode` and `ThemeModeStore`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeMode.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModeStore.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModule.kt`
- Create: `shared/src/commonTest/kotlin/io/redigit/skimbyte/testing/FakeSettings.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/theme/ThemeModeStoreTest.kt`

**Interfaces:**
- Produces: `enum class ThemeMode { Light, Dark, System }`; `interface ThemeModeStore { fun get(): ThemeMode; fun set(mode: ThemeMode) }`; `class SettingsThemeModeStore(private val settings: Settings) : ThemeModeStore`; `val themeModule: Module` registering `single<ThemeModeStore> { SettingsThemeModeStore(get()) }` (reuses the `Settings` singleton already provided by `authModule`). Also produces `class FakeSettings(initial: Map<String, Any> = emptyMap()) : Settings` — the `com.russhwolf.settings:multiplatform-settings` 1.3.0 artifact this project depends on has **no bundled in-memory test double**; `MapSettings`/`MockSettings` ship only in a separate `multiplatform-settings-test` artifact this project does not pull in. `FakeSettings` is a minimal, map-backed implementation of the full `Settings` interface (every `put`/`get`/`getOrNull` for `Int`/`Long`/`String`/`Float`/`Double`/`Boolean`, plus `keys`/`size`/`clear`/`remove`/`hasKey` — **not** `ObservableSettings`'s listener methods, which `Settings` itself does not declare). This task creates it in `commonTest` because Task 1 is the first to need it; Task 3 reuses it rather than duplicating it.

- [ ] **Step 1: Write `FakeSettings`**

```kotlin
package io.redigit.skimbyte.testing

import com.russhwolf.settings.Settings

class FakeSettings(initial: Map<String, Any> = emptyMap()) : Settings {
    private val map = initial.toMutableMap()
    override val keys: Set<String> get() = map.keys
    override val size: Int get() = map.size
    override fun clear() { map.clear() }
    override fun remove(key: String) { map.remove(key) }
    override fun hasKey(key: String): Boolean = map.containsKey(key)
    override fun putInt(key: String, value: Int) { map[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = map[key] as? Int ?: defaultValue
    override fun getIntOrNull(key: String): Int? = map[key] as? Int
    override fun putLong(key: String, value: Long) { map[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = map[key] as? Long ?: defaultValue
    override fun getLongOrNull(key: String): Long? = map[key] as? Long
    override fun putString(key: String, value: String) { map[key] = value }
    override fun getString(key: String, defaultValue: String): String = map[key] as? String ?: defaultValue
    override fun getStringOrNull(key: String): String? = map[key] as? String
    override fun putFloat(key: String, value: Float) { map[key] = value }
    override fun getFloat(key: String, defaultValue: Float): Float = map[key] as? Float ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = map[key] as? Float
    override fun putDouble(key: String, value: Double) { map[key] = value }
    override fun getDouble(key: String, defaultValue: Double): Double = map[key] as? Double ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = map[key] as? Double
    override fun putBoolean(key: String, value: Boolean) { map[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as? Boolean ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = map[key] as? Boolean
}
```

- [ ] **Step 2: Write the failing `ThemeModeStoreTest`**

```kotlin
class ThemeModeStoreTest {
    @Test fun `defaults to System when nothing stored`() {
        val store = SettingsThemeModeStore(FakeSettings())
        assertEquals(ThemeMode.System, store.get())
    }
    @Test fun `set then get round-trips`() {
        val store = SettingsThemeModeStore(FakeSettings())
        store.set(ThemeMode.Light)
        assertEquals(ThemeMode.Light, store.get())
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.theme.ThemeModeStoreTest"`
Expected: FAIL (classes don't exist yet)

- [ ] **Step 4: Implement `ThemeMode`, `ThemeModeStore`, `SettingsThemeModeStore`**

Store as a string key (`"theme_mode"`) via `settings.getStringOrNull`/`putString`, using `ThemeMode.name`; unknown/missing values resolve to `ThemeMode.System` (defensive default at the storage boundary, matching `Topic`'s existing "best-guess" comment style).

- [ ] **Step 5: Implement `themeModule`**

```kotlin
val themeModule = module {
    single<ThemeModeStore> { SettingsThemeModeStore(get()) }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.theme.ThemeModeStoreTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeMode.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModeStore.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/ThemeModule.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/testing/FakeSettings.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/theme/ThemeModeStoreTest.kt
git commit -m "Add persisted ThemeMode store"
```

---

## Task 2: Wire `ThemeMode` into `SkimbyteTheme` and `AppModule`

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt`

**Interfaces:**
- Consumes: `ThemeMode` from Task 1.
- Produces: a second `SkimbyteTheme(themeMode: ThemeMode, content: @Composable () -> Unit)` overload. The **existing** `SkimbyteTheme(darkTheme: Boolean = true, content)` is left untouched — do not change its signature or touch any of its ~15 existing `@Preview` call sites; this task only adds a new overload that the real app root (Task 11) will call.

- [ ] **Step 1: Add the overload in `Theme.kt`**

```kotlin
@Composable
fun SkimbyteTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    SkimbyteTheme(darkTheme = dark, content = content)
}
```

Also delete the old "always dark per the product spec" comment above the existing function — that product decision is reversed per the design doc's "Settings scope" section.

- [ ] **Step 2: Register `themeModule` in `AppModule.kt`**

Add `themeModule` to the `includes(...)` list alongside the existing modules.

- [ ] **Step 3: Compile-check**

Run: `./gradlew :shared:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Manually verify `ThemeMode.System` tracks the OS live**

This is the Review Focus item for live OS-theme tracking; `isSystemInDarkTheme()` is itself a Compose state read, so no extra wiring is needed, but it's worth confirming once rather than assuming: run the app (any target) with `ThemeMode.System` active, flip the OS/emulator's dark-mode setting while the app is in the foreground, and confirm the UI recomposes to match without restarting the app. Not automatable as a `jvmTest`/`androidHostTest` case — record the manual check in the task's PR description instead of adding a test for it.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt
git commit -m "Add ThemeMode-based SkimbyteTheme overload"
```

---

## Task 3: `TopicPreferencesStore`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicPreferencesStore.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/OnboardingModule.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/onboarding/TopicPreferencesStoreTest.kt`

**Interfaces:**
- Consumes: `io.redigit.skimbyte.posts.Topic`; `io.redigit.skimbyte.testing.FakeSettings` (Task 1 — the test file imports it rather than redefining it; this task does not create a `FakeSettings.kt`).
- Produces:
```kotlin
interface TopicPreferencesStore {
    fun selectedTopics(): Set<Topic>
    fun setSelectedTopics(topics: Set<Topic>)
    fun hasCompletedOnboarding(): Boolean
    fun setOnboardingCompleted()
}
class SettingsTopicPreferencesStore(private val settings: Settings) : TopicPreferencesStore
val onboardingModule: Module
```

- [ ] **Step 1: Write the failing tests**

```kotlin
class TopicPreferencesStoreTest {
    @Test fun `no topics selected and onboarding incomplete by default`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        assertEquals(emptySet<Topic>(), store.selectedTopics())
        assertFalse(store.hasCompletedOnboarding())
    }
    @Test fun `set then get round-trips a topic set`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setSelectedTopics(setOf(Topic.Kotlin, Topic.Android))
        assertEquals(setOf(Topic.Kotlin, Topic.Android), store.selectedTopics())
    }
    @Test fun `setOnboardingCompleted persists`() {
        val store = SettingsTopicPreferencesStore(FakeSettings())
        store.setOnboardingCompleted()
        assertTrue(store.hasCompletedOnboarding())
    }
    @Test fun `unknown stored topic name is ignored, not thrown`() {
        val settings = FakeSettings(mapOf("selected_topics" to "Kotlin,NotARealTopic"))
        val store = SettingsTopicPreferencesStore(settings)
        assertEquals(setOf(Topic.Kotlin), store.selectedTopics())
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.onboarding.TopicPreferencesStoreTest"`
Expected: FAIL (classes don't exist yet)

- [ ] **Step 3: Implement the store**

Encode the topic set as a comma-joined list of `Topic.name` strings under one settings key; decode by splitting, mapping through `Topic.entries.associateBy { it.name }`, and dropping unrecognized names rather than throwing (defensive parsing at the storage boundary). `hasCompletedOnboarding`/`setOnboardingCompleted` use a separate boolean key.

- [ ] **Step 4: Implement `onboardingModule`**

```kotlin
val onboardingModule = module {
    single<TopicPreferencesStore> { SettingsTopicPreferencesStore(get()) }
}
```

- [ ] **Step 5: Register in `AppModule.kt`**

Add `onboardingModule` to `includes(...)`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.onboarding.TopicPreferencesStoreTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicPreferencesStore.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/OnboardingModule.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/onboarding/TopicPreferencesStoreTest.kt
git commit -m "Add persisted topic preferences store"
```

---

## Task 4: Nav key and `TopLevelDestination` migration

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/SkimbyteNavKeys.kt`

**Interfaces:**
- Produces:
```kotlin
enum class TopLevelDestination { Feed, Bookmarks, Search, Account }

@Serializable data object BookmarksList : NavKey
@Serializable data object FeedHome : NavKey
@Serializable data object SearchHome : NavKey
@Serializable data class PostDetail(val postId: String) : NavKey
@Serializable data object AccountHome : NavKey
@Serializable data object Settings : NavKey
@Serializable data object Topics : NavKey
```
`SourceDetail` and `BookmarkDetail` are removed. Every later task that referenced them (Tasks 6, 7, 8) consumes `PostDetail` instead.

- [ ] **Step 1: Make the edit**

Add `Search` to `TopLevelDestination`; add `PostDetail`, `AccountHome`, `Settings`, `Topics`; delete `SourceDetail` and `BookmarkDetail`.

- [ ] **Step 2: Confirm the expected breakage**

Run: `./gradlew :shared:compileCommonMainKotlinMetadata`
Expected: FAILS — `FeedNavHost.kt`, `SourceDetailScreen.kt`, `BookmarksNavHost.kt`, and `BookmarkDetailScreen.kt` all reference the removed `SourceDetail`/`BookmarkDetail` symbols, and `App.kt` references the old 3-value `TopLevelDestination`. This is expected; Tasks 6, 8, 9, 11 fix each in turn. Do not attempt to make this compile within this task.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/SkimbyteNavKeys.kt
git commit -m "Migrate nav keys to unified PostDetail and 4-destination top level"
```

---

## Task 5: `TopicsScreen` (multi-select onboarding/editing) and `SplashScreen`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicsUiState.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/TopicsViewModel.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/ui/TopicsScreen.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/ui/SplashScreen.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding/OnboardingModule.kt` (register `TopicsViewModel`, added in Task 3 for `TopicPreferencesStore` only)
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/TopicsScreenCatalog.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/onboarding/TopicsViewModelTest.kt`

**Interfaces:**
- Consumes: `TopicPreferencesStore` (Task 3), `Topic` (existing — `Topic.entries - Topic.All` are the selectable options).
- Produces:
```kotlin
data class TopicsUiState(
    val available: List<Topic> = Topic.entries.filter { it != Topic.All },
    val selected: Set<Topic> = emptySet(),
    val isEditing: Boolean = false,
) {
    val canConfirm: Boolean get() = selected.size in 3..4
}
class TopicsViewModel(private val store: TopicPreferencesStore, isEditing: Boolean) : ViewModel() {
    val uiState: StateFlow<TopicsUiState>
    fun onTopicToggled(topic: Topic)
    fun onConfirm()
}
```
`TopicsScreen(uiState: TopicsUiState, onTopicToggled: (Topic) -> Unit, onConfirm: () -> Unit, modifier: Modifier = Modifier)` — stateless. `SplashScreen(modifier: Modifier = Modifier)` — stateless, no logic, just a branded loading visual (no `ViewModel`, no artificial delay — Task 11 controls how long it's shown, which is exactly as long as the synchronous `hasCompletedOnboarding()` read takes).

- [ ] **Step 1: Write the failing `TopicsViewModel` tests**

```kotlin
class TopicsViewModelTest {
    @Test fun `confirm disabled below 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        vm.onTopicToggled(Topic.Kotlin)
        vm.onTopicToggled(Topic.Android)
        assertFalse(vm.uiState.value.canConfirm)
    }
    @Test fun `confirm enabled at exactly 3 selections`() {
        val vm = TopicsViewModel(FakeTopicPreferencesStore(), isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach(vm::onTopicToggled)
        assertTrue(vm.uiState.value.canConfirm)
    }
    @Test fun `onConfirm persists selection and onboarding flag`() {
        val store = FakeTopicPreferencesStore()
        val vm = TopicsViewModel(store, isEditing = false)
        listOf(Topic.Kotlin, Topic.Android, Topic.Kmp).forEach(vm::onTopicToggled)
        vm.onConfirm()
        assertEquals(setOf(Topic.Kotlin, Topic.Android, Topic.Kmp), store.selectedTopics())
        assertTrue(store.hasCompletedOnboarding())
    }
    @Test fun `editing mode pre-populates from the store`() {
        val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Tooling)) }
        val vm = TopicsViewModel(store, isEditing = true)
        assertEquals(setOf(Topic.Tooling), vm.uiState.value.selected)
        assertTrue(vm.uiState.value.isEditing)
    }
}
```
(`FakeTopicPreferencesStore` is a simple in-memory implementation of the Task 3 interface, local to the test file.)

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.onboarding.TopicsViewModelTest"`
Expected: FAIL

- [ ] **Step 3: Implement `TopicsUiState` and `TopicsViewModel`**

`onTopicToggled` adds/removes from `selected` (no cap enforcement needed beyond `canConfirm` gating the button — with only 4 non-`All` topics total, selecting all 4 is valid). `onConfirm` calls `store.setSelectedTopics(selected)` then `store.setOnboardingCompleted()` only when `!isEditing` (re-confirming during an edit must not re-trigger onboarding completion logic it's already past, though setting it again is harmless — simplest correct behavior is to always call it, since it's idempotent).

- [ ] **Step 4: Register `TopicsViewModel` in `OnboardingModule.kt`**

`TopicsViewModel` takes a runtime `isEditing: Boolean` that differs per call site (Task 10's edit flow passes `true`, Task 11's onboarding flow passes `false`), so it needs Koin's parameterized `viewModel` factory, not a plain single-arg one:

```kotlin
viewModel { (isEditing: Boolean) -> TopicsViewModel(get(), isEditing) }
```

Add this to the existing `onboardingModule` block alongside the `TopicPreferencesStore` registration from Task 3. Without this, `koinViewModel<TopicsViewModel> { parametersOf(...) }` in Tasks 10 and 11 has nothing to resolve against.

- [ ] **Step 5: Implement `TopicsScreen`**

A `LazyVerticalGrid` or `Column` of toggleable topic chips (reuse the existing selected/unselected chip visual style from `TopicFilterRow`'s `Surface`/`Text` pattern) plus a `Button` labelled `"Save"` when `isEditing` else `"Get started"`, `enabled = uiState.canConfirm`, calling `onConfirm`. Needs light + dark `@Preview`s for both `isEditing = false` and `isEditing = true`.

- [ ] **Step 6: Implement `SplashScreen`**

A centered `Text`/logo mark inside a `Box.fillMaxSize()`, themed via `SkimbyteTheme`. Needs light + dark `@Preview`s.

- [ ] **Step 7: Add the designsheet catalog entry**

Add `TopicsScreenCatalogContent()` to `TopicsScreenCatalog.kt` and register it as `CatalogItem("topics-screen", "Topics", "Onboarding")` in `CatalogRegistry.kt`.

- [ ] **Step 8: Run tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.onboarding.TopicsViewModelTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/onboarding designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/TopicsScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/onboarding/TopicsViewModelTest.kt
git commit -m "Add topic onboarding/editing screen and splash screen"
```

---

## Task 6: `PostDetailScreen` and `PostCard` card-wide tap

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostDetailScreen.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostCard.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/PostDetailScreenCatalog.kt`
- Delete: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SourceDetailScreen.kt`
- Delete: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SourceDetailScreenCatalog.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/ui/PostCardTest.kt` (modify existing)

**Interfaces:**
- Consumes: `Post` (existing).
- Produces:
```kotlin
fun PostDetailScreen(
    post: Post,
    isBookmarkLoading: Boolean,
    onBookmarkClick: () -> Unit,
    onReadSourceClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
)
fun PostDetailPlaceholder(modifier: Modifier = Modifier)

@ExperimentalSharedTransitionApi
data class SharedElementContext(
    val scope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)
```
`PostCard` gains the same optional `sharedTransition: SharedElementContext? = null` parameter. `onSourceClick: (String) -> Unit` is removed; `onCardClick: (String) -> Unit` is added. Both composables apply `Modifier.sharedElement(scope.rememberSharedContentState(key = "post-image-${post.id}"), animatedVisibilityScope)` to their image element only when `sharedTransition != null`, else a plain `Modifier` — so every existing `@Preview`/designsheet call site (which has no `SharedTransitionLayout`/`AnimatedVisibilityScope` available) keeps compiling unchanged by simply omitting the parameter. `PostDetailScreen` does **not** reuse `PostCard` internally (that would make the card inside the detail page clickable into itself) — it is its own full-bleed layout: image header (the shared element), title, un-capped markdown body (unlike `PostCard`'s `heightIn(max = 240.dp)` — the whole point of a full page is the content isn't truncated), a "View source" button, and a bookmark toggle, with a back button in a `TopAppBar`.

Tasks 8 and 9 are responsible for actually constructing a `SharedElementContext` (wrapping each `NavHost`'s `NavDisplay` in a `SharedTransitionLayout` and sourcing an `AnimatedVisibilityScope` from it) and passing it down to both `PostCard` and `PostDetailScreen`. **Navigation 3's exact API for exposing an `AnimatedVisibilityScope` per entry is version-sensitive and must be confirmed against the installed `navigation3-ui`/`material3-adaptive-navigation3` 1.1.1/1.3.0-beta02 artifacts while implementing Task 8** — if no such scope is exposed by `NavDisplay`/`entryProvider` at this version, fall back to wrapping the two states in a single `AnimatedContent` inside the `SharedTransitionLayout` driven by "is `PostDetail` topmost", and note the simplification in that task's commit message rather than blocking on it.

- [ ] **Step 1: Confirm `SharedTransitionLayout`/`SharedTransitionScope` are on the classpath**

`androidx.compose.animation.SharedTransitionLayout` ships in the `org.jetbrains.compose.animation:animation` artifact. Check whether it's already pulled in transitively (via `compose.foundation`/`compose.material3`) by referencing it in a scratch file; if unresolved, add `compose-animation = { module = "org.jetbrains.compose.animation:animation", version.ref = "composeMultiplatform" }` to `gradle/libs.versions.toml` and `implementation(libs.compose.animation)` to `shared/build.gradle.kts`'s `commonMain.dependencies`.

- [ ] **Step 2: Modify `PostCard`**

Replace the `onSourceClick: (String) -> Unit` parameter with `onCardClick: (String) -> Unit`; add the `sharedTransition: SharedElementContext? = null` parameter and apply it to the image `Box`. Remove the `Modifier.clickable { onSourceClick(post.sourceName) }` on the source-name `Text`, leaving it plain. Wrap the outer `Card(...)` itself with `.clickable { onCardClick(post.id) }`. Update every existing preview call site in `PostCard.kt` to pass `onCardClick = {}` instead of `onSourceClick = {}` (they can omit `sharedTransition` entirely, relying on its default).

- [ ] **Step 3: Update the existing `PostCardTest.kt` UI test**

Find the test that currently taps the source-name text and asserts `onSourceClick` fires; change it to tap the card body and assert `onCardClick` fires with the post's id.

- [ ] **Step 4: Run the test to verify it fails against the old code, then passes against the new**

Run: `./gradlew :shared:testAndroidHostTest --tests "io.redigit.skimbyte.feed.ui.PostCardTest"`
Expected: PASS after Step 2's change is in place.

- [ ] **Step 5: Implement `PostDetailScreen` and `PostDetailPlaceholder`**

Apply the same `sharedTransition`-gated `Modifier.sharedElement(..., key = "post-image-${post.id}")` to the image header that `PostCard` applies to its image — matching keys are what make the transition continuous between the two screens. Needs light + dark `@Preview`s, plus a preview with a long markdown body to confirm it isn't height-capped (unlike `PostCard`'s preview set). Previews omit `sharedTransition`.

- [ ] **Step 6: Delete `SourceDetailScreen.kt` and its catalog file**

Remove both files. `SourceDetailViewMode`, `SourceDetailScreen`, and `SourceDetailPlaceholder` no longer exist anywhere.

- [ ] **Step 7: Update `CatalogRegistry.kt`**

Remove the `"source-detail"` `CatalogItem`; add `CatalogItem("post-detail", "Post detail", "Feed") { PostDetailScreenCatalogContent() }`.

- [ ] **Step 8: Confirm compile state**

Run: `./gradlew :shared:compileCommonMainKotlinMetadata`
Expected: Still fails at this point — `FeedScreen.kt`, `FeedNavHost.kt`, `SearchScreen.kt` still reference the old `PostCard` signature and `SourceDetail`. This is expected; fixed in Tasks 7–8.

- [ ] **Step 9: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostDetailScreen.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostCard.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SourceDetailScreen.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/PostDetailScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SourceDetailScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/ui/PostCardTest.kt
git commit -m "Replace SourceDetail with unified PostDetailScreen; make whole card tappable"
```

---

## Task 7: `FeedScreen`/`FeedViewModel` — horizontal deck, list toggle, topic defaults from preferences

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedUiState.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedScreen.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/TopicFilterRow.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedModule.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewMode.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/FeedViewModelTest.kt` (modify existing)

**Interfaces:**
- Consumes: `TopicPreferencesStore` (Task 3).
- Produces:
```kotlin
enum class FeedViewMode { Stack, List }
data class FeedUiState(
    val selectedTopic: Topic = Topic.All, // default overridden in init, see Step 3
    val viewMode: FeedViewMode = FeedViewMode.Stack,
    val availableTopics: List<Topic> = emptyList(), // the user's selected interests
    /* ...existing fields unchanged... */
)
```
`FeedViewModel` constructor gains a fourth parameter: `private val topicPreferencesStore: TopicPreferencesStore`. New method: `fun onViewModeToggled()`. `TopicFilterRow` signature changes: `TopicFilterRow(topics: List<Topic>, selectedTopic: Topic, onTopicSelected: (Topic) -> Unit, modifier: Modifier = Modifier)` — no longer hardcodes `Topic.entries`. `FeedScreen` gains a `sharedTransition: SharedElementContext? = null` parameter (Task 6's type) that it forwards unchanged to every `PostCard` it renders, in both Stack and List view modes; its own `@Preview`s omit it.

- [ ] **Step 1: Write/modify the failing `FeedViewModelTest` cases**

```kotlin
@Test fun `initial selected topic comes from stored interests, not Topic All`() {
    val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Kotlin, Topic.Android)) }
    val vm = FeedViewModel(fakePostsRepository, fakeAuthRepository, fakeBookmarkIdsStore, store)
    assertEquals(Topic.Kotlin, vm.uiState.value.selectedTopic) // first of the stored set
    assertEquals(listOf(Topic.Kotlin, Topic.Android), vm.uiState.value.availableTopics)
}
@Test fun `onViewModeToggled flips between Stack and List`() {
    val vm = FeedViewModel(fakePostsRepository, fakeAuthRepository, fakeBookmarkIdsStore, store)
    vm.onViewModeToggled()
    assertEquals(FeedViewMode.List, vm.uiState.value.viewMode)
    vm.onViewModeToggled()
    assertEquals(FeedViewMode.Stack, vm.uiState.value.viewMode)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.FeedViewModelTest"`
Expected: FAIL

- [ ] **Step 3: Implement the `FeedViewModel`/`FeedUiState` changes**

In `init`, read `topicPreferencesStore.selectedTopics()`; if non-empty, seed `_uiState` with `availableTopics = that set as a list` and `selectedTopic = that set's first element`, before calling `loadInitial()`. `onViewModeToggled` flips `viewMode` via `.copy()`, matching the existing `onTopicSelected`/`onPageChanged` style.

- [ ] **Step 4: Update `FeedModule.kt`**

`viewModel { FeedViewModel(get(), get(), get(), get()) }` — the new fourth `get()` resolves `TopicPreferencesStore`.

- [ ] **Step 5: Update `TopicFilterRow`**

Replace `Topic.entries.forEach` with `topics.forEach`. Update its own `@Preview`s to pass an explicit `topics` list.

- [ ] **Step 6: Update `FeedScreen`**

Switch `StackedCardBrowser`'s `orientation` to `StackedCardOrientation.Horizontal`. Pass `uiState.availableTopics` into `TopicFilterRow`. Add a view-mode toggle button in the top-right (reuse the existing `Surface`+`Icon` toggle-button visual pattern that was in the now-deleted `SourceDetailScreen` — move that small composable here, e.g. as a private `FeedViewModeToggle` in `FeedScreen.kt`, using `Icons.Filled.ViewAgenda`/`Icons.AutoMirrored.Filled.ViewList` or similar already-available icons from `compose.materialIconsExtended`). When `viewMode == FeedViewMode.List`, render a `LazyColumn` of `PostCard`s (`Modifier.fillMaxWidth()`, not `fillMaxSize()`) instead of `StackedCardBrowser`. Replace `onSourceClick` wiring with `onCardClick` throughout.

- [ ] **Step 7: Run tests to verify they pass**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.FeedViewModelTest"`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedUiState.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewModel.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewMode.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedModule.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedScreen.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/TopicFilterRow.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/FeedViewModelTest.kt
git commit -m "Switch feed deck to horizontal swipe, add list view toggle, seed topic from preferences"
```

---

## Task 8: `FeedNavHost` → `PostDetail`; extract `SearchNavHost`

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedNavHost.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchNavHost.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/SearchUiState.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/SearchViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchScreen.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteBottomNavBar.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/SearchViewModelTest.kt`

**Interfaces:**
- Consumes: `PostsRepository`, `BookmarkIdsStore`-aware repository (same pattern as `FeedViewModel`); `PostDetail` nav key (Task 4); `PostDetailScreen`/`PostDetailPlaceholder` (Task 6).
- Produces:
```kotlin
data class SearchUiState(
    val query: String = "",
    val results: List<Post> = emptyList(),
    val pendingBookmarkToggles: Set<String> = emptySet(),
)
class SearchViewModel(private val postsRepository: PostsRepository) : ViewModel() {
    val uiState: StateFlow<SearchUiState>
    fun onQueryChanged(query: String)
    fun onBookmarkClick(postId: String)
}
```
Search becomes fully self-contained — it no longer reads `FeedViewModel`'s loaded posts (that cross-tab coupling conflicted with the design doc's "independent backstack per top-level destination" decision). It fetches its own copy of posts (`postsRepository.fetchPosts(Topic.All, cursor = null)`) on first load, then filters client-side by title substring, matching today's filter behavior.

`SkimbyteBottomNavBar`'s `onSearchClick: (() -> Unit)?` parameter is removed — `Search` is now just another `TopLevelDestination.entries` entry, handled by the existing generic `onDestinationSelected` callback.

Both `FeedNavHost` and the new `SearchNavHost` gain an `isCompact: Boolean` parameter (alongside their existing `selected`/`onDestinationSelected`) and own their own `Scaffold`+bottom chrome, per the design doc's "Detail page chrome... hides the bottom nav entirely" decision: `bottomBar = { if (isCompact && backStack.size == 1) SkimbyteBottomNavBar(selected, onDestinationSelected, fabMenu = ...) }`. When `!isCompact`, neither host renders any bottom chrome at all — Task 11's `MainShell` renders the `NavigationRail` itself, outside and independent of whichever tab/backstack depth is active, since the rail never needs to hide (expanded width always shows list and detail as two simultaneous panes, never an immersive single-pane detail). Both hosts also wrap their `NavDisplay` in a `SharedTransitionLayout`, passing a `SharedElementContext` (Task 6) down to whichever entry currently renders `PostCard`/`PostDetailScreen`.

- [ ] **Step 1: Write the failing `SearchViewModelTest`**

```kotlin
@Test fun `empty query yields no results even after posts load`() = runTest {
    val vm = SearchViewModel(fakePostsRepositoryWith(listOf(postNamed("Kotlin 2.4"))))
    advanceUntilIdle()
    assertEquals(emptyList(), vm.uiState.value.results)
}
@Test fun `query filters loaded posts by title substring`() = runTest {
    val vm = SearchViewModel(fakePostsRepositoryWith(listOf(postNamed("Kotlin 2.4"), postNamed("Android Studio"))))
    advanceUntilIdle()
    vm.onQueryChanged("kotlin")
    assertEquals(listOf("Kotlin 2.4"), vm.uiState.value.results.map { it.title })
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.SearchViewModelTest"`
Expected: FAIL

- [ ] **Step 3: Implement `SearchUiState`/`SearchViewModel`**

Fetch once in `init` via `viewModelScope.launch`; `onQueryChanged` updates `query` and recomputes `results` by filtering the already-loaded posts (same substring/`ignoreCase` logic `SearchScreen` used to do locally). `onBookmarkClick` delegates to `postsRepository.setBookmarked`, mirroring `FeedViewModel.toggleBookmark`'s optimistic-update pattern.

- [ ] **Step 4: Update `FeedModule.kt`**

Add `viewModel { SearchViewModel(get()) }`.

- [ ] **Step 5: Update `SearchScreen`**

Change its parameters to take `uiState: SearchUiState`, `onQueryChanged: (String) -> Unit`, `onBookmarkClick: (String) -> Unit`, `onCardClick: (String) -> Unit`, `sharedTransition: SharedElementContext? = null` (Task 6's type, forwarded unchanged to every `PostCard` it renders) — it no longer takes `posts`/`pendingBookmarkToggles`/`onReadSourceClick`/`onSourceClick` directly from the caller (call `::openUrl` directly for the "Read full source" action, matching `BookmarkDetailScreen`'s existing pattern). Remove its own `onBack` parameter and back button — Search is a top-level tab now, not a pushed entry with somewhere to "back" to.

- [ ] **Step 6: Create `SearchNavHost`**

Signature: `SearchNavHost(selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, isCompact: Boolean)`. Mirrors `BookmarksNavHost`'s structure: its own `navConfig` (polymorphic `SearchHome`, `PostDetail`), its own `rememberNavBackStack(navConfig, SearchHome)`, its own `rememberListDetailSceneStrategy<NavKey>()`, `entry<SearchHome>` (list pane, pushes `PostDetail(post.id)` on card click) and `entry<PostDetail>` (detail pane, rendering `PostDetailScreen` with `onBookmarkClick` toggling in place via `viewModel.onBookmarkClick`, and `onBack = { backStack.removeLastOrNull() }`). Wrap the `NavDisplay` in `SharedTransitionLayout`, threading the resulting `SharedElementContext` into both the `SearchHome` entry's `SearchScreen` call and the `PostDetail` entry's `PostDetailScreen` call — same pattern as `FeedNavHost`'s Step 7. Own a `Scaffold` whose `bottomBar` is `{ if (isCompact && backStack.size == 1) SkimbyteBottomNavBar(selected, onDestinationSelected, fabMenu = null) }`.

- [ ] **Step 7: Update `FeedNavHost`**

Add the `isCompact: Boolean` parameter. Remove the `SearchHome` entry and the `onSearchClick` wiring entirely (Search is no longer reachable from inside the Feed tab). Replace the `entry<SourceDetail>` block with `entry<PostDetail>`, looking the post up from `uiState.posts` by `key.postId`, with `onBookmarkClick = { viewModel.onBookmarkTapped(key.postId) }` (toggle-in-place semantics, matching the Review Focus item). Remove the now-unused `SourceDetailPlaceholder` import/usage, replacing the list pane's `detailPlaceholder` with `PostDetailPlaceholder`. Change the existing `Scaffold`'s `bottomBar` condition to `if (isCompact && backStack.size == 1)` (previously it rendered unconditionally). Wrap the existing `NavDisplay` call in `SharedTransitionLayout`, threading the resulting `SharedElementContext` into the `FeedHome` entry's `PostCard` calls (via `FeedScreen`, Task 7) and the `PostDetail` entry's `PostDetailScreen` call.

- [ ] **Step 8: Update `SkimbyteBottomNavBar`**

Remove the `onSearchClick` parameter and the `IconButton`/`Icons.Filled.Search` block that used it. Add `TopLevelDestination.Search -> Icons.Filled.Search` to the existing `icon()` `when`. Update its own two `@Preview`s (no `onSearchClick` argument needed anymore) and add a third preview with `selected = TopLevelDestination.Search`.

- [ ] **Step 9: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.SearchViewModelTest"`
Expected: PASS

- [ ] **Step 10: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedNavHost.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchNavHost.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/SearchUiState.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/SearchViewModel.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchScreen.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedModule.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteBottomNavBar.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/SearchViewModelTest.kt
git commit -m "Promote Search to an independent top-level destination with its own backstack"
```

---

## Task 9: `BookmarksNavHost` → `PostDetail`

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarksNavHost.kt`
- Delete: `shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarkDetailScreen.kt`
- Test: Roborazzi goldens under `shared/src/androidHostTest/roborazzi/` referencing `BookmarkDetailScreen` — delete and regenerate if any exist

**Interfaces:**
- Consumes: `PostDetail`, `PostDetailScreen`, `PostDetailPlaceholder` (Tasks 4, 6).
- `BookmarksNavHost` gains the same `selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, isCompact: Boolean` parameters as `FeedNavHost`/`SearchNavHost` (Task 8) — today it has none of these; its chrome is currently built by the caller (`App.kt`) instead of by the host itself, which is what made the "hide chrome behind Detail" requirement impossible to satisfy before this migration. It now owns its own `Scaffold`/`bottomBar` and `SharedTransitionLayout`, same pattern as the other two hosts.

- [ ] **Step 1: Update `BookmarksNavHost`**

Add the `selected`, `onDestinationSelected`, `isCompact` parameters. Wrap the existing `NavDisplay` in `SharedTransitionLayout` and its own `Scaffold` whose `bottomBar = { if (isCompact && backStack.size == 1) SkimbyteBottomNavBar(selected, onDestinationSelected, fabMenu = null) }`. Replace the `entry<BookmarkDetail>` block with `entry<PostDetail>`, looking the post up from `uiState.posts` by `key.postId` (same lookup pattern already there, just renamed). Keep the existing unbookmark-then-pop behavior: `onBookmarkClick = { viewModel.onUnbookmark(key.postId); backStack.removeLastOrNull() }` — this is the Review Focus divergence point; Bookmarks pops back on unbookmark, Feed/Search (Task 8) do not. Replace the list pane's `detailPlaceholder` from `BookmarkDetailPlaceholder` to `PostDetailPlaceholder`. `BookmarksListScreen` renders plain `ListItem` rows, not `PostCard` — there's no image to share an element from on that side, so only the `PostDetail` entry's `PostDetailScreen` call receives the `SharedElementContext`; it will simply animate in from wherever the list row was tapped without a continuous image transform. This is a reasonable, smaller-scope visual outcome for Bookmarks specifically; Home's deck/list (Task 7, which does use `PostCard` throughout) gets the full image-to-image transition the design doc describes.

- [ ] **Step 2: Delete `BookmarkDetailScreen.kt`**

Remove the file. Its `@Preview`s go with it.

- [ ] **Step 3: Check and fix any Roborazzi goldens**

Run: `grep -rl "BookmarkDetail" shared/src/androidHostTest/` — delete any golden images found and note they'll be regenerated.

- [ ] **Step 4: Run bookmarks tests**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.bookmarks.*"`
Expected: PASS (no logic in `BookmarksViewModel` changed, only the nav host wiring)

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarksNavHost.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/bookmarks/ui/BookmarkDetailScreen.kt
git commit -m "Replace BookmarkDetail with unified PostDetailScreen"
```

---

## Task 10: `AccountNavHost`, `SettingsScreen`, `SkimbyteNavigationRail`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/SettingsUiState.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/SettingsViewModel.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/SettingsModule.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/settings/ui/SettingsScreen.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountNavHost.kt`
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteNavigationRail.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt`
- Create: `designsheet/.../components/SettingsScreenCatalog.kt`, `SkimbyteNavigationRailCatalog.kt`
- Modify: `designsheet/.../catalog/CatalogRegistry.kt`
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/settings/SettingsViewModelTest.kt`

**Interfaces:**
- Consumes: `ThemeModeStore` (Task 1/2), `TopicPreferencesStore` (Task 3), `TopicsViewModel`/`TopicsScreen` (Task 5), `AccountHome`/`Settings`/`Topics` nav keys (Task 4).
- Produces:
```kotlin
data class SettingsUiState(val themeMode: ThemeMode = ThemeMode.System)
class SettingsViewModel(private val themeModeStore: ThemeModeStore) : ViewModel() {
    val uiState: StateFlow<SettingsUiState>
    fun onThemeModeSelected(mode: ThemeMode)
}
```
`SettingsScreen(uiState: SettingsUiState, onThemeModeSelected: (ThemeMode) -> Unit, onEditInterestsClick: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier)`. `AccountScreen` gains one new parameter: `onSettingsClick: () -> Unit` (a button/icon that was not there before). `SkimbyteNavigationRail(selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, fabMenu: (@Composable () -> Unit)?, modifier: Modifier = Modifier)` — mirrors `SkimbyteBottomNavBar`'s destination-iteration logic using `NavigationRail`/`NavigationRailItem` instead of `HorizontalFloatingToolbar`/`IconButton`. `AccountNavHost` takes `selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, isCompact: Boolean`, same as the other three hosts (Tasks 8, 9), and owns its own `Scaffold`/`bottomBar` gated on `isCompact && backStack.size == 1` — Account has no `PostCard`/image content, so it does not need a `SharedTransitionLayout`.

- [ ] **Step 1: Write the failing `SettingsViewModelTest`**

```kotlin
@Test fun `reads initial theme mode from store`() {
    val store = FakeThemeModeStore(ThemeMode.Dark)
    val vm = SettingsViewModel(store)
    assertEquals(ThemeMode.Dark, vm.uiState.value.themeMode)
}
@Test fun `onThemeModeSelected persists and updates state`() {
    val store = FakeThemeModeStore(ThemeMode.System)
    val vm = SettingsViewModel(store)
    vm.onThemeModeSelected(ThemeMode.Light)
    assertEquals(ThemeMode.Light, vm.uiState.value.themeMode)
    assertEquals(ThemeMode.Light, store.get())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.settings.SettingsViewModelTest"`
Expected: FAIL

- [ ] **Step 3: Implement `SettingsUiState`/`SettingsViewModel`/`SettingsModule`**

- [ ] **Step 4: Implement `SettingsScreen`**

A radio group (`RadioButton`/`ListItem` rows) for `ThemeMode.entries`, plus a `ListItem`/`Button` labelled "Edit interests" calling `onEditInterestsClick`, with a `TopAppBar` back button. Needs light + dark `@Preview`s for each `ThemeMode`.

- [ ] **Step 5: Update `AccountScreen`**

Add `onSettingsClick: () -> Unit` parameter; add a settings `IconButton`/gear icon near the existing logout button, visible only `if (isLoggedIn)` (matches the existing conditional structure). Update its three existing `@Preview`s to pass `onSettingsClick = {}`.

- [ ] **Step 6: Implement `AccountNavHost`**

Signature: `AccountNavHost(selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, isCompact: Boolean)`. Mirrors `BookmarksNavHost`'s structure with nav keys `AccountHome`, `Settings`, `Topics`: `entry<AccountHome>` renders `AccountScreen` (via `AccountRoute`'s existing `koinInject<AuthRepository>()` pattern) with `onSettingsClick = { backStack.add(Settings) }`; `entry<Settings>` renders `SettingsScreen` via `koinViewModel<SettingsViewModel>()`, with `onEditInterestsClick = { backStack.add(Topics) }` and `onBack = { backStack.removeLastOrNull() }`; `entry<Topics>` renders `TopicsScreen` via a `koinViewModel<TopicsViewModel> { parametersOf(true) }` (constructing with `isEditing = true`), with `onConfirm` also popping back (`backStack.removeLastOrNull()`) after the ViewModel's own `onConfirm()` persists the change. No `ListDetailSceneStrategy` here — Account's three screens are a simple linear stack, not a list/detail pair. Own a `Scaffold` whose `bottomBar = { if (isCompact && backStack.size == 1) SkimbyteBottomNavBar(selected, onDestinationSelected, fabMenu = null) }`.

- [ ] **Step 7: Implement `SkimbyteNavigationRail`**

Same destination-iteration/icon-mapping logic as `SkimbyteBottomNavBar`, using `NavigationRail { TopLevelDestination.entries.forEach { NavigationRailItem(selected = it == selected, onClick = { onDestinationSelected(it) }, icon = { Icon(it.icon(), null) }) } }`, with `fabMenu` rendered in the rail's `header` slot if non-null.

- [ ] **Step 8: Add designsheet catalog entries**

`CatalogItem("settings-screen", "Settings", "Account")`, `CatalogItem("navigation-rail", "Navigation rail", "Navigation")`.

- [ ] **Step 9: Register `settingsModule` in `AppModule.kt`**

- [ ] **Step 10: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.settings.SettingsViewModelTest"`
Expected: PASS

- [ ] **Step 11: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/settings shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountNavHost.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteNavigationRail.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/di/AppModule.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SettingsScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SkimbyteNavigationRailCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/settings/SettingsViewModelTest.kt
git commit -m "Add Account nav host with Settings (theme switcher) and interest editing"
```

---

## Task 11: `App.kt` rewrite — Splash, onboarding gate, adaptive 4-destination shell

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/App.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/AppNavigationTest.kt` (new Compose UI test)

**Interfaces:**
- Consumes: `TopicPreferencesStore`, `ThemeModeStore` (via `koinInject`), `SplashScreen`/`TopicsScreen`/`TopicsViewModel` (Task 5), `FeedNavHost`, `BookmarksNavHost`, `SearchNavHost`, `AccountNavHost`, `SkimbyteBottomNavBar`, `SkimbyteNavigationRail`.

- [ ] **Step 1: Write the failing Compose UI test**

```kotlin
class AppNavigationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun `switching away from Home and back preserves its backstack depth`() {
        // seed TopicPreferencesStore fake with onboarding already completed
        composeRule.setContent { App() }
        // navigate Home -> tap a card -> PostDetail is shown
        // switch to Bookmarks tab
        // switch back to Home tab
        // assert PostDetail is still shown (backstack wasn't reset to FeedHome)
    }
}
```
(Exact node-finding calls depend on test tags already used elsewhere in the project's Compose UI tests — follow the existing convention in `shared/src/androidHostTest` for finding `PostCard`/nav bar nodes; add `Modifier.testTag(...)` to `PostCard` and the nav bar/rail destination buttons only if no existing finder works, matching this project's established testing style.)

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "io.redigit.skimbyte.AppNavigationTest"`
Expected: FAIL (either compile error against the old `App.kt`, or the backstack genuinely resets)

- [ ] **Step 3: Rewrite `App.kt`**

Structure:
```kotlin
sealed interface AppStage {
    data object Splash : AppStage
    data object Onboarding : AppStage
    data object Main : AppStage
}

@Composable
@Preview
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        val themeModeStore = koinInject<ThemeModeStore>()
        SkimbyteTheme(themeMode = themeModeStore.get()) {
            val topicPreferencesStore = koinInject<TopicPreferencesStore>()
            var stage by remember { mutableStateOf<AppStage>(AppStage.Splash) }
            LaunchedEffect(Unit) {
                stage = if (topicPreferencesStore.hasCompletedOnboarding()) AppStage.Main else AppStage.Onboarding
            }
            when (val currentStage = stage) {
                AppStage.Splash -> SplashScreen()
                AppStage.Onboarding -> {
                    val topicsViewModel = koinViewModel<TopicsViewModel> { parametersOf(false) }
                    val uiState by topicsViewModel.uiState.collectAsState()
                    TopicsScreen(
                        uiState = uiState,
                        onTopicToggled = topicsViewModel::onTopicToggled,
                        onConfirm = { topicsViewModel.onConfirm(); stage = AppStage.Main },
                    )
                }
                AppStage.Main -> MainShell()
            }
        }
    }
}

@Composable
private fun MainShell() {
    var selected by remember { mutableStateOf(TopLevelDestination.Feed) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val onDestinationSelected: (TopLevelDestination) -> Unit = { selected = it }
    Row {
        // The rail is deliberately outside the SaveableStateProvider below and never hidden:
        // at medium/expanded width, ListDetailSceneStrategy always shows list+detail as two
        // simultaneous panes, so there is never an "immersive single-pane detail" state to
        // hide chrome for — unlike the compact bottom bar, which each NavHost hides itself.
        if (!isCompact) SkimbyteNavigationRail(selected, onDestinationSelected, fabMenu = null)
        Box(Modifier.weight(1f)) {
            saveableStateHolder.SaveableStateProvider(selected) {
                when (selected) {
                    TopLevelDestination.Feed -> FeedNavHost(selected, onDestinationSelected, isCompact)
                    TopLevelDestination.Bookmarks -> BookmarksNavHost(selected, onDestinationSelected, isCompact)
                    TopLevelDestination.Search -> SearchNavHost(selected, onDestinationSelected, isCompact)
                    TopLevelDestination.Account -> AccountNavHost(selected, onDestinationSelected, isCompact)
                }
            }
        }
    }
}
```
`MainShell` itself owns no `Scaffold` and renders no bottom bar — each `NavHost` (Tasks 8, 9, 10) owns its own `Scaffold`/`bottomBar`, gated on `isCompact && backStack.size == 1`, which is what makes `PostDetail` (and Account's `Settings`/`Topics` pushes) correctly hide the compact bottom bar per the design doc's "Detail page chrome... hides the bottom nav entirely" decision — a single `Scaffold` wrapping all four hosts from above could not express that per-host, per-backstack-depth condition. The `rememberSaveableStateHolder()` + `SaveableStateProvider(selected)` wrapping is the fix for the tab-switch state-retention Review Focus item — without it, switching `selected` away and back recreates each `NavHost`'s `rememberNavBackStack` from scratch. `FeedNavHost` keeps its existing Feed-specific FAB menu logic internally (unchanged from today — it was already passed as a `fabMenu` lambda into its own internally-owned `SkimbyteBottomNavBar` call, which continues to work exactly as before since `FeedNavHost` still builds its own `Scaffold`); only `MainShell`'s rail passes `fabMenu = null` since the rail has no equivalent contextual-action slot wired up in this plan (a reasonable follow-up, not required here).

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "io.redigit.skimbyte.AppNavigationTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/App.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/AppNavigationTest.kt
git commit -m "Rewrite app shell: splash, onboarding gate, adaptive 4-destination nav with state retention"
```

---

## Task 12: Topic-edit feed fallback + full-suite verification

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewModel.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountNavHost.kt` (wire the new call into `entry<Topics>`'s `onConfirm`)
- Test: `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/FeedViewModelTest.kt` (add one case)

**Interfaces:**
- Consumes: Task 7's `FeedViewModel`; Task 10's `AccountNavHost`.

This task closes the last Review Focus item: today, `FeedViewModel` reads `topicPreferencesStore.selectedTopics()` only once at `init` — if the user edits their interests from Settings (Task 10) while the Feed tab's `ViewModel` is still alive (preserved across tab switches per Task 11), Feed's `selectedTopic`/`availableTopics` go stale.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun `editing interests away from the active filter falls back to a remaining topic`() = runTest {
    val store = FakeTopicPreferencesStore().apply { setSelectedTopics(setOf(Topic.Android, Topic.Kotlin)) }
    val vm = FeedViewModel(fakePostsRepository, fakeAuthRepository, fakeBookmarkIdsStore, store)
    vm.onTopicSelected(Topic.Android) // user is filtered on Android
    store.setSelectedTopics(setOf(Topic.Kotlin)) // edits interests, drops Android
    vm.onTopicPreferencesChanged() // see Step 3
    assertEquals(Topic.Kotlin, vm.uiState.value.selectedTopic)
    assertEquals(listOf(Topic.Kotlin), vm.uiState.value.availableTopics)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.FeedViewModelTest"`
Expected: FAIL (`onTopicPreferencesChanged` doesn't exist)

- [ ] **Step 3: Implement `onTopicPreferencesChanged()` on `FeedViewModel`**

Re-reads `topicPreferencesStore.selectedTopics()`; sets `availableTopics` to the new list; if the current `selectedTopic` is no longer in that set, falls back to its first element (reloading posts for the new topic via the existing `onTopicSelected` path), otherwise leaves `selectedTopic` untouched. Call this method from `AccountNavHost`'s `entry<Topics>` `onConfirm` callback (Task 10) by injecting `FeedViewModel` there too (`koinViewModel<FeedViewModel>()` — Koin's single-instance-per-scope `viewModel {}` definition means this resolves to the *same* Feed-tab instance as long as both `AccountNavHost` and `FeedNavHost` are composed under the same `KoinApplication`/`ViewModelStoreOwner`, which they are per Task 11's `MainShell`).

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :shared:jvmTest --tests "io.redigit.skimbyte.feed.FeedViewModelTest"`
Expected: PASS

- [ ] **Step 5: Run the full shared test suite**

Run: `./gradlew :shared:jvmTest :shared:testAndroidHostTest`
Expected: all PASS; fix any fallout from earlier tasks' deletions (stale imports, removed Roborazzi goldens) discovered here.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/FeedViewModel.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountNavHost.kt shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/FeedViewModelTest.kt
git commit -m "Re-sync feed topic filter when interests are edited; close out migration test suite"
```
