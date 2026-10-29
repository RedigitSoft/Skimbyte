# Material 3 Expressive Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle Skimbyte into a coral-accented, dark Material 3 Expressive app: a stacked/peeking-card feed, a single-active-pill topic row, a floating pill nav bar with an expanding FAB menu, and new Search/Source-detail/Account screens — replacing the current teal/`NavigationSuiteScaffold`/`FilterChip`/full-bleed-pager shell.

**Architecture:** Theme tokens land first (color, shape, eyebrow text style), then presentational leaf components (`TopicFilterRow`, `PostCard`, `StackedCardBrowser`, `SkimbyteFabMenu`, `SkimbyteBottomNavBar`), then navigation plumbing (`SkimbyteNavKeys`, `FeedNavHost` hosting Feed/Search/SourceDetail, `App.kt`). Search and Source-detail are kept deliberately stateless — both derive from `FeedViewModel`'s already-loaded `uiState.posts` via inline filtering in the Nav3 entry provider (the same pattern `BookmarksNavHost` already uses for `BookmarkDetail`), so no new ViewModels are introduced.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material 3 (`org.jetbrains.compose.material3:material3` `1.12.0-alpha03` — verified via the jar's class list to already contain `FloatingActionButtonMenu`, `ToggleFloatingActionButton`, `FloatingActionButtonMenuItem`, `HorizontalFloatingToolbar`, `MaterialShapes`; **no version bump needed**), Navigation 3, `material3-adaptive` (`ListDetailSceneStrategy`), Koin, Roborazzi.

**Spec:** `docs/superpowers/specs/2026-10-17-material3-expressive-redesign-design.md`

## Global Constraints

- Every composable that renders UI gets at least light+dark `@Preview`s (CONTRIBUTING.md).
- Composables are stateless; state is hoisted to a ViewModel exposing immutable `StateFlow<UiState>` (CONTRIBUTING.md). `remember { mutableStateOf(...) }` is reserved for UI-local/transient concerns only.
- No hardcoded colors/dimensions/text styles — pull from `MaterialTheme` and shared tokens (CONTRIBUTING.md).
- Unit tests for all logic; Compose UI tests for every interactive/conditional composable; Roborazzi screenshot tests (light/dark) for shared design-system components (CONTRIBUTING.md).
- Koin DI only, across all targets (CONTRIBUTING.md).
- `designsheet` is updated in the same change as any new/changed shared component or token (CONTRIBUTING.md).
- Dependencies only via `gradle/libs.versions.toml` — no unlisted coordinates (CONTRIBUTING.md).
- `material3.adaptive` for foldable/resizable-window support, via Nav3's `ListDetailSceneStrategy` — no hardcoded breakpoints (CLAUDE.md, CONTRIBUTING.md).
- Never nest a scrollable inside another scrollable of the same orientation (CLAUDE.md) — `StackedCardBrowser`'s internal `VerticalPager` is the single vertical scrollable per screen; any bounded content inside a card (e.g. markdown summaries) must keep a fixed `heightIn(max = ...)` rather than its own unbounded vertical scroll.
- Navigation 3 only, no Navigation Compose (nav2) (CONTRIBUTING.md).
- App colors: `Primary` = `#CC785C`, `OnPrimary` = `#FFFFFF`, `Background` = `#181715`, `Surface` = `#252320`, `SurfaceVariant` = `#1F1E1B`, `OnBackground`/`OnSurface` = `#FAF9F5`, `OnSurfaceVariant` = `#A09D96`, `Error` = `#C64545` (spec, Design decision 1).

## Review Focus

- `PostCard`/`StackedCardBrowser` rendering a post whose `imageUrl == null` — must show a themed placeholder, not a blank/crashing card (spec Design decision 9; existing `imagePost`/no-image states in `PostCardCatalog`).
- `StackedCardBrowser` given an empty `items` list (e.g. a topic or search query with zero matches) — must render an empty state rather than crash on `rememberPagerState(pageCount = { 0 })`.
- The FAB menu's target post must not silently change while the menu is expanded, even if `uiState.posts` mutates underneath it (spec Design decision 7: focus locks for the duration the menu is open).
- `SourceDetailScreen`'s filter must match `Post.sourceName` by exact string equality on the already-`decodeHtmlEntities()`-normalized value, so a source with posts across multiple topics still shows all of them, not just one topic's subset.
- Tapping "Bookmark" in the FAB menu while logged out must trigger the existing login flow (`FeedUiState.pendingBookmarkAction` → `LoginSheet`), exactly like today's `BookmarkButton` tap — not fail silently (spec Design decision 4; `FeedViewModel.onBookmarkTapped`).

---

### Task 1: Coral/dark color palette

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Color.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/ColorTokensContent.kt` (find via `CatalogItem("color-tokens", ...)`)

**Interfaces:**
- Produces: `SkimbyteAccent` (coral `0xFFCC785C`), `SkimbyteOnPrimary` (`0xFFFFFFFF`), `SkimbyteBackground` (`0xFF181715`), `SkimbyteSurface` (`0xFF252320`), `SkimbyteSurfaceVariant` (`0xFF1F1E1B`), `SkimbyteOnBackground` (`0xFFFAF9F5`), `SkimbyteOnSurfaceVariant` (`0xFFA09D96`), `SkimbyteError` (`0xFFC64545`) — all `androidx.compose.ui.graphics.Color` vals in `Color.kt`. `SkimbyteTheme`'s dark `ColorScheme` wires `onSurfaceVariant = SkimbyteOnSurfaceVariant` in addition to the fields it already sets.

- [ ] **Step 1: Update `Color.kt`** — replace the existing teal-era vals with the eight vals listed above, using the exact hex values from Global Constraints.

- [ ] **Step 2: Update `Theme.kt`'s dark `colorScheme`** — add `onSurfaceVariant = SkimbyteOnSurfaceVariant` and `onPrimary = SkimbyteOnPrimary` (replacing the current `onPrimary = SkimbyteBackground`) to the existing `darkColorScheme(...)` call; leave the light-scheme branch using the same `primary = SkimbyteAccent`.

- [ ] **Step 3: Update `ColorTokensContent.kt`** so its swatches reflect the new values (read the file first — it almost certainly renders `MaterialTheme.colorScheme.*` swatches already, so no change may be needed beyond confirming it's not hardcoding old hex values; fix any hardcoded references found).

- [ ] **Step 4: Run existing Roborazzi goldens to see the diff**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest`
Expected: FAILS — `BookmarkButtonScreenshotTest`, `TopicFilterRowScreenshotTest`, and `LoginSheetScreenshotTest` goldens no longer match (color change). This is expected; do not fix yet.

- [ ] **Step 5: Regenerate the goldens for components touched only by the color change**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest`
Expected: new goldens written under `shared/src/androidHostTest/roborazzi/`.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Color.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/ColorTokensContent.kt shared/src/androidHostTest/roborazzi/
git commit -m "theme: switch to coral/dark Material 3 palette"
```

---

### Task 2: Expressive shape scale

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Shape.kt`
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/ShapeTokensContent.kt`

**Interfaces:**
- Produces: `val SkimbyteShapes: androidx.compose.material3.Shapes` in `Shape.kt`, using `RoundedCornerShape`: `extraSmall = 12.dp`, `small = 16.dp`, `medium = 20.dp`, `large = 28.dp`, `extraLarge = 32.dp` (Expressive-scale radii, one tier larger than M3 defaults, per spec Design decision 3).
- Consumes: nothing new.

- [ ] **Step 1: Write `Shape.kt`** with `SkimbyteShapes` as specified above.

- [ ] **Step 2: Wire `shapes = SkimbyteShapes` into both the dark and light `MaterialTheme(...)` calls in `Theme.kt`.**

- [ ] **Step 3: Update `ShapeTokensContent.kt`** to render swatches for all five shape roles via `MaterialTheme.shapes.*` (read the existing file first; extend its swatch list if it only covers a subset today).

- [ ] **Step 4: Add/verify a light+dark `@Preview` on `ShapeTokensContent`** (it's a designsheet catalog page already rendered inside `SkimbyteTheme`, so confirm it already has one; add if missing).

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Shape.kt shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/Theme.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/ShapeTokensContent.kt
git commit -m "theme: add Material 3 Expressive shape scale"
```

---

### Task 3: `EyebrowText` component

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/EyebrowText.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/theme/EyebrowTextScreenshotTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/TypographyTokensContent.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`

**Interfaces:**
- Produces: `@Composable fun EyebrowText(text: String, modifier: Modifier = Modifier)` in `theme/EyebrowText.kt`. Renders `text.uppercase()` with `MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)` and `color = MaterialTheme.colorScheme.onSurfaceVariant`.
- Consumed by: Task 6 (`PostCard`), Task 10 (`SourceDetailScreen`), Task 12 (`FeedScreen`) for the "eyebrow label above bold title" rhythm from the spec.

- [ ] **Step 1: Write the Roborazzi test** `EyebrowTextScreenshotTest` in `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/theme/`, following the existing pattern in `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/` (e.g. `TopicFilterRowScreenshotTest` — read it first for the exact Roborazzi/Robolectric boilerplate this project uses). Capture one golden rendering `EyebrowText("Sunday, September 7")` in light and one in dark `SkimbyteTheme`.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest --tests "*EyebrowText*"`
Expected: FAIL — `EyebrowText` is unresolved.

- [ ] **Step 3: Implement `EyebrowText`** as specified in Interfaces, with a light+dark `@Preview`.

- [ ] **Step 4: Record the goldens**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "*EyebrowText*"`
Expected: new goldens committed under `shared/src/androidHostTest/roborazzi/`.

- [ ] **Step 5: Add a designsheet entry** — add `EyebrowText("Sunday, September 7")` as an example inside `TypographyTokensContent.kt`'s existing content (no new `CatalogItem` needed; it's a typography-token usage example, consistent with how that page already demonstrates the type scale — read the file first to match its structure).

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/theme/EyebrowText.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/theme/ shared/src/androidHostTest/roborazzi/ designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/tokens/TypographyTokensContent.kt
git commit -m "theme: add EyebrowText label component"
```

---

### Task 4: `TopicFilterRow` restyle — single active pill

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/TopicFilterRow.kt`
- Modify: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/TopicFilterRowScreenshotTest.kt` (read first to match existing structure)
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/TopicFilterCatalog.kt`

**Interfaces:**
- Signature is unchanged: `TopicFilterRow(selectedTopic: Topic, onTopicSelected: (Topic) -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Update the existing Compose UI test** (if one exists under `commonTest`/`androidHostTest` beyond the screenshot test — check `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/` first) to assert the active topic's text node has a distinct semantic/content marker (e.g. `onClick` still fires per topic — behavior doesn't change, so this is mostly a no-op check; skip adding a new test if the existing interaction test already covers "tapping a topic invokes `onTopicSelected`" without asserting on chip-specific semantics).

- [ ] **Step 2: Reimplement the `Row` content** — replace the `FilterChip` loop with, per topic: if `topic == selectedTopic`, a `Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primary) { Text(topic.displayLabel, color = MaterialTheme.colorScheme.onPrimary, ..., modifier = Modifier.clickable { onTopicSelected(topic) }.padding(horizontal = 16.dp, vertical = 8.dp)) }`; otherwise a plain `Text(topic.displayLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onTopicSelected(topic) }.padding(horizontal = 16.dp, vertical = 8.dp))`. Keep the outer `horizontalScroll` + `Arrangement.spacedBy(8.dp)`.

- [ ] **Step 3: Update both existing `@Preview`s** (`TopicFilterRowPreview`, `TopicFilterRowAllSelectedPreview`) — no signature change needed, just confirm they still compile/render.

- [ ] **Step 4: Re-record the Roborazzi goldens**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "*TopicFilterRow*"`
Expected: new goldens reflecting the pill style.

- [ ] **Step 5: Update `TopicFilterCatalog.kt`** — no structural change expected (it already renders `TopicFilterRow` in its states); re-run and visually confirm via `:designsheet:run`.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/TopicFilterRow.kt shared/src/androidHostTest/roborazzi/
git commit -m "feed: restyle TopicFilterRow as a single active pill"
```

---

### Task 5: `StackedCardBrowser` primitive

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/StackedCardBrowser.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/StackedCardBrowserTest.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/StackedCardBrowserScreenshotTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/StackedCardBrowserCatalog.kt`

**Interfaces:**
- Produces:
  ```kotlin
  @Composable
  fun <T> StackedCardBrowser(
      items: List<T>,
      onIndexChanged: (Int) -> Unit,
      modifier: Modifier = Modifier,
      resetSignal: Any? = null,
      emptyContent: @Composable () -> Unit = {},
      cardContent: @Composable (item: T) -> Unit,
  )
  ```
  Internally: `rememberPagerState(pageCount = { items.size })`; if `items.isEmpty()`, render `emptyContent()` instead of a `VerticalPager`. `LaunchedEffect(pagerState.currentPage) { onIndexChanged(pagerState.currentPage) }`. `LaunchedEffect(resetSignal) { if (resetSignal != null) pagerState.animateScrollToPage(0) }`. The `VerticalPager` uses `contentPadding = PaddingValues(bottom = 64.dp)` and `pageSpacing = 12.dp` so the next card's top edge peeks below the current one (the spec's "stacked/peeking" card feed, Design decision 4), rendering `cardContent(items[page])` per page.
- Consumed by: Task 12 (`FeedScreen`), Task 10 (`SourceDetailScreen`), Task 11 (`SearchScreen`) — all three pass `Post` as `T`.

- [ ] **Step 1: Write a Compose UI test** `StackedCardBrowserTest` with `createComposeRule()` (or `runComposeUiTest`, per project convention — check an existing interactive-composable test, e.g. `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/` or `androidHostTest` for the established pattern). Cover: (a) with 3 string items, swiping/advancing to the next page invokes `onIndexChanged(1)`; (b) with `items = emptyList()`, `emptyContent` is shown and no `VerticalPager` content is composed; (c) changing `resetSignal` from `null` to a new value scrolls back to page 0.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*StackedCardBrowserTest*"`
Expected: FAIL — `StackedCardBrowser` is unresolved.

- [ ] **Step 3: Implement `StackedCardBrowser`** per the Interfaces block.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*StackedCardBrowserTest*"`
Expected: PASS

- [ ] **Step 5: Write a Roborazzi screenshot test** (`StackedCardBrowserScreenshotTest`) with simple colored-box `cardContent` showing the peeking-next-card effect, light+dark.

- [ ] **Step 6: Record the goldens**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "*StackedCardBrowser*"`

- [ ] **Step 7: Add a `@Preview` to `StackedCardBrowser.kt`** itself (light+dark) with sample string content.

- [ ] **Step 8: Add a designsheet catalog entry** — create `StackedCardBrowserCatalog.kt` with a `StackedCardBrowserCatalogContent()` composable demonstrating it with 4 placeholder cards, and register it in `CatalogRegistry.kt` as `CatalogItem("stacked-card-browser", "Stacked card browser", "Feed") { StackedCardBrowserCatalogContent() }`.

- [ ] **Step 9: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/StackedCardBrowser.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/StackedCardBrowser* shared/src/androidHostTest/roborazzi/ designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/StackedCardBrowserCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "feed: add StackedCardBrowser peeking-card primitive"
```

---

### Task 6: `PostCard` restyle — hero-card look + source tap

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostCard.kt`
- Modify: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/PostCardScreenshotTest.kt` (check whether one exists; if not, this is a new file — the component is already screenshot-tested per `BookmarkButtonScreenshotTest`'s sibling naming, confirm by reading the `androidHostTest/feed` directory first)
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/PostCardCatalog.kt`

**Interfaces:**
- New signature: `PostCard(post: Post, isBookmarkLoading: Boolean, onBookmarkClick: () -> Unit, onReadSourceClick: (String) -> Unit, onSourceClick: (sourceName: String) -> Unit, modifier: Modifier = Modifier)` — adds `onSourceClick`.
- Consumed by: Task 12 (`FeedScreen`), Task 10, Task 11 — all must now pass `onSourceClick`.

- [ ] **Step 1: Update `PostCardPreview`, `PostCardBookmarkedPreview`, `PostCardLongTitlePreview`** to pass `onSourceClick = {}`, and add a new `PostCardNoImagePreview` (reuses `previewPost.copy(imageUrl = null)`) and `PostCardDarkPreview`/light pairing per CONTRIBUTING's light+dark minimum (check whether `@Preview` already defaults to dark via `SkimbyteTheme()`'s default `darkTheme = true` — if so, add one `darkTheme = false` preview explicitly, matching how other components in this codebase provide a light variant).

- [ ] **Step 2: Reimplement the card body** — `Card(shape = MaterialTheme.shapes.extraLarge)` containing:
  - A `Box` image area (fixed height, e.g. `220.dp`): `AsyncImage` (crop) if `post.imageUrl != null`, else a `Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant))` placeholder — both under the same `Box`. Overlay a bottom `Brush.verticalGradient` scrim (transparent → `colorScheme.scrim` or `Color.Black.copy(alpha = 0.6f)`), and at `Alignment.BottomStart`, an `EyebrowText("${post.topic} · ${post.publishedAt.toDisplayDate()}")` above a bold `Text(post.sourceName, color = Color.White, modifier = Modifier.clickable { onSourceClick(post.sourceName) })`. At `Alignment.TopEnd`, the existing `BookmarkButton` wrapped in a small translucent `Surface(shape = CircleShape, color = colorScheme.surface.copy(alpha = 0.6f))` for contrast over the photo.
  - Below the image: `Column(padding = 16.dp)` with `Text(post.title, style = MaterialTheme.typography.headlineSmall)`, the existing `Markdown(..., modifier = Modifier.heightIn(max = 240.dp))` call unchanged, and the existing `TextButton("Read full source")` row (drop the old inline topic/source/date row — that content moved to the image overlay).

- [ ] **Step 3: Run/update the Roborazzi goldens**

Run: `./gradlew :shared:recordRoborazziAndroidHostTest --tests "*PostCard*"`

- [ ] **Step 4: Update `PostCardCatalog.kt`** — add `onSourceClick = {}` to every call site, and add one example post with `imageUrl = null` if not already present (it is — `shortPost` has `imageUrl = null`; confirm it still renders the placeholder correctly).

- [ ] **Step 5: Update any other call sites of `PostCard`** found via grep (`grep -rn "PostCard(" shared/ designsheet/`) to pass `onSourceClick` — expect `FeedScreen.kt` (handled in Task 12) and `BookmarkDetailScreen.kt`. For `BookmarkDetailScreen.kt`, read it first; if it doesn't use the `PostCard` composable (it may render the post detail differently), no change is needed there — confirm by reading.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/PostCard.kt shared/src/androidHostTest/roborazzi/ designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/PostCardCatalog.kt
git commit -m "feed: restyle PostCard as an image-led hero card with source tap"
```

---

### Task 7: `SkimbyteFabMenu` component

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteFabMenu.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/nav/SkimbyteFabMenuTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SkimbyteFabMenuCatalog.kt`

**Interfaces:**
- Produces:
  ```kotlin
  @Composable
  fun SkimbyteFabMenu(
      expanded: Boolean,
      onExpandedChange: (Boolean) -> Unit,
      isBookmarked: Boolean,
      isBookmarkLoading: Boolean,
      onBookmarkClick: () -> Unit,
      onShareClick: () -> Unit,
      onOpenSourceClick: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  Built on `androidx.compose.material3.FloatingActionButtonMenu` with its `button` slot as `ToggleFloatingActionButton(checked = expanded, onCheckedChange = onExpandedChange) { ... }` and three `FloatingActionButtonMenuItem` children (Bookmark/unbookmark toggle, Share, Open source link) — these symbols are confirmed present in the pinned `material3` `1.12.0-alpha03` artifact (verified by inspecting its class list), but their exact Kotlin parameter names/order were not recoverable from bytecode alone — look them up via IDE autocomplete or `Ctrl+click`-to-source before wiring the call, rather than guessing.
- Consumed by: Task 8 (`SkimbyteBottomNavBar`), Task 13 (`FeedNavHost`).

- [ ] **Step 1: Write the Compose UI test** `SkimbyteFabMenuTest` covering: (a) tapping the toggle button with `expanded = false` invokes `onExpandedChange(true)`; (b) with `expanded = true`, tapping the bookmark menu item invokes `onBookmarkClick`; (c) tapping share invokes `onShareClick`; (d) tapping open-source invokes `onOpenSourceClick`; (e) `isBookmarkLoading = true` disables/shows a loading indicator on the bookmark item instead of invoking `onBookmarkClick` when tapped (mirrors `BookmarkButton`'s existing loading-disables-tap behavior).

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SkimbyteFabMenuTest*"`
Expected: FAIL — `SkimbyteFabMenu` is unresolved.

- [ ] **Step 3: Implement `SkimbyteFabMenu`** per the Interfaces block, using `MaterialTheme.colorScheme.primary`/`onPrimary` for the toggle button's container/icon colors (no hardcoded colors, per Global Constraints).

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SkimbyteFabMenuTest*"`
Expected: PASS

- [ ] **Step 5: Add light+dark `@Preview`s** showing both the collapsed and expanded states.

- [ ] **Step 6: Add a designsheet catalog entry** (`SkimbyteFabMenuCatalog.kt`, registered as `CatalogItem("fab-menu", "FAB menu", "Navigation")`) demonstrating collapsed, expanded, bookmarked, and loading states.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteFabMenu.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/nav/ designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SkimbyteFabMenuCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "nav: add SkimbyteFabMenu expressive FAB menu"
```

---

### Task 8: `SkimbyteBottomNavBar` component

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteBottomNavBar.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/nav/SkimbyteBottomNavBarTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SkimbyteBottomNavBarCatalog.kt`

**Interfaces:**
- Consumes: `io.redigit.skimbyte.nav.TopLevelDestination` (Task 9), `SkimbyteFabMenu` (Task 7).
- Produces:
  ```kotlin
  @Composable
  fun SkimbyteBottomNavBar(
      selected: TopLevelDestination,
      onDestinationSelected: (TopLevelDestination) -> Unit,
      onSearchClick: (() -> Unit)?,
      fabMenu: (@Composable () -> Unit)?,
      modifier: Modifier = Modifier,
  )
  ```
  Built on `androidx.compose.material3.HorizontalFloatingToolbar` (confirmed present in the pinned artifact, including a FAB-docking variant — verify the exact overload/parameter names via IDE lookup, same caveat as Task 7) containing, left to right: a leading icon per `TopLevelDestination` (Feed=`Icons.Filled.Home`, Bookmarks=`Icons.Filled.Bookmark`, Account=`Icons.Filled.Person` — tapping selects that destination), a search `IconButton(Icons.Filled.Search)` shown only when `onSearchClick != null` (hidden — not just disabled — otherwise, since it has no meaning outside Feed), and the docked `fabMenu` content when non-null (otherwise the toolbar renders without a docked FAB). Use `MaterialTheme.shapes.extraLarge`/`FloatingToolbarDefaults.standardFloatingToolbarColors()`-equivalent (verify exact call) for the pill container, no hardcoded colors.
- Consumed by: Task 13 (`FeedNavHost`), Task 14 (`App.kt`'s Bookmarks/Account branches).

- [ ] **Step 1: Write the Compose UI test** `SkimbyteBottomNavBarTest` covering: (a) tapping each destination icon invokes `onDestinationSelected` with that destination; (b) the search icon is absent from the semantics tree when `onSearchClick == null`, and tapping it invokes `onSearchClick` when provided; (c) `fabMenu` content is composed when non-null and absent when null.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SkimbyteBottomNavBarTest*"`
Expected: FAIL — unresolved.

- [ ] **Step 3: Implement `SkimbyteBottomNavBar`** per Interfaces.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SkimbyteBottomNavBarTest*"`
Expected: PASS

- [ ] **Step 5: Add light+dark `@Preview`s** for: Feed selected with a FAB menu docked, Bookmarks selected with no FAB/no search icon, Account selected.

- [ ] **Step 6: Add a designsheet catalog entry** (`CatalogItem("bottom-nav-bar", "Bottom nav bar", "Navigation")`) mirroring the preview states.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/ui/SkimbyteBottomNavBar.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/nav/SkimbyteBottomNavBarTest.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SkimbyteBottomNavBarCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "nav: add SkimbyteBottomNavBar floating pill nav bar"
```

---

### Task 9: Nav keys — `TopLevelDestination`, `FeedHome`, `SearchHome`, `SourceDetail`

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/SkimbyteNavKeys.kt`

**Interfaces:**
- Produces: `enum class TopLevelDestination { Feed, Bookmarks, Account }` (removes nothing used elsewhere by name — `App.kt`, Task 14, is updated in the same change the removal would otherwise break); `@Serializable data object FeedHome : NavKey`; `@Serializable data object SearchHome : NavKey`; `@Serializable data class SourceDetail(val sourceName: String) : NavKey`. Existing `BookmarksList`/`BookmarkDetail` are untouched.
- Consumed by: Task 13 (`FeedNavHost`), Task 14 (`App.kt`), Task 8.

- [ ] **Step 1: Add the three new `NavKey` types and the `Account` enum entry** to `SkimbyteNavKeys.kt` as specified.

- [ ] **Step 2: Grep for every current usage of `TopLevelDestination`**

Run: `grep -rn "TopLevelDestination" shared/ androidApp/ designsheet/`
Expected output noted for Task 14 (this step doesn't change code — it's a scope check carried into the next task; no commit here).

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/nav/SkimbyteNavKeys.kt
git commit -m "nav: add Account destination and Feed/Search/SourceDetail nav keys"
```

---

### Task 10: `SourceDetailScreen`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SourceDetailScreen.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/SourceDetailScreenTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SourceDetailScreenCatalog.kt`

**Interfaces:**
- Consumes: `StackedCardBrowser` (Task 5), `PostCard` (Task 6), `EyebrowText` (Task 3).
- Produces:
  ```kotlin
  @Composable
  fun SourceDetailScreen(
      sourceName: String,
      posts: List<Post>,
      pendingBookmarkToggles: Set<String>,
      onBookmarkClick: (String) -> Unit,
      onReadSourceClick: (String) -> Unit,
      onBack: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  Filters internally: `val sourcePosts = remember(posts, sourceName) { posts.filter { it.sourceName == sourceName } }` (exact-match, per Review Focus item 4 — `Post.sourceName` is already `decodeHtmlEntities()`-normalized by `toDomain`, so no further normalization needed here). Header: a back `IconButton` + `EyebrowText("Source")` + bold `Text(sourceName)` + `Text("${sourcePosts.size} posts", style = bodySmall)`. Body: `StackedCardBrowser(items = sourcePosts, onIndexChanged = {}, emptyContent = { Text("No posts from this source yet.") }) { post -> PostCard(post = post, isBookmarkLoading = post.id in pendingBookmarkToggles, onBookmarkClick = { onBookmarkClick(post.id) }, onReadSourceClick = onReadSourceClick, onSourceClick = {}) }` — `onSourceClick = {}` here because tapping the source name while already on that source's detail screen is a no-op.
- Consumed by: Task 13 (`FeedNavHost`).

- [ ] **Step 1: Write the Compose UI test** `SourceDetailScreenTest` covering: (a) given posts from two different sources, only the matching source's posts are shown (Review Focus item 4); (b) tapping back invokes `onBack`; (c) tapping a card's bookmark button invokes `onBookmarkClick(postId)`; (d) given zero matching posts, the empty-state text is shown instead of a crash (Review Focus item 2).

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SourceDetailScreenTest*"`
Expected: FAIL — unresolved.

- [ ] **Step 3: Implement `SourceDetailScreen`** per Interfaces.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SourceDetailScreenTest*"`
Expected: PASS

- [ ] **Step 5: Add light+dark `@Preview`s** — one with posts, one with the empty state.

- [ ] **Step 6: Add a designsheet catalog entry** (`CatalogItem("source-detail", "Source detail", "Feed")`).

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SourceDetailScreen.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/SourceDetailScreenTest.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SourceDetailScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "feed: add SourceDetailScreen"
```

---

### Task 11: `SearchScreen`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchScreen.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/SearchScreenTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SearchScreenCatalog.kt`

**Interfaces:**
- Consumes: `StackedCardBrowser` (Task 5), `PostCard` (Task 6).
- Produces:
  ```kotlin
  @Composable
  fun SearchScreen(
      posts: List<Post>,
      pendingBookmarkToggles: Set<String>,
      onBookmarkClick: (String) -> Unit,
      onReadSourceClick: (String) -> Unit,
      onSourceClick: (String) -> Unit,
      onBack: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  Local UI-only state: `var query by remember { mutableStateOf("") }` (ephemeral, scoped to this screen — permitted under Global Constraints' `remember` carve-out). `val results = remember(posts, query) { if (query.isBlank()) emptyList() else posts.filter { it.title.contains(query, ignoreCase = true) } }` — operates on *all* `posts` regardless of any topic filter (spec Design decision 6: search is independent of the feed's topic filter). Header: back `IconButton` + `OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Search titles") })`. Body: if `query.isBlank()`, a prompt ("Type to search your loaded posts."); else `StackedCardBrowser(items = results, onIndexChanged = {}, emptyContent = { Text("No matches.") }) { post -> PostCard(...) }` wired the same way as `SourceDetailScreen`.
- Consumed by: Task 13 (`FeedNavHost`).

- [ ] **Step 1: Write the Compose UI test** `SearchScreenTest` covering: (a) with an empty query, no results are shown (prompt state); (b) typing a substring that matches one post's title (case-insensitive) shows only that post; (c) typing a substring matching zero posts shows the empty-state text (Review Focus item 2); (d) tapping back invokes `onBack`.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SearchScreenTest*"`
Expected: FAIL — unresolved.

- [ ] **Step 3: Implement `SearchScreen`** per Interfaces.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*SearchScreenTest*"`
Expected: PASS

- [ ] **Step 5: Add light+dark `@Preview`s** — empty-query, with-results, and no-matches states.

- [ ] **Step 6: Add a designsheet catalog entry** (`CatalogItem("search-screen", "Search", "Feed")`).

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/SearchScreen.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/SearchScreenTest.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/SearchScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "feed: add SearchScreen"
```

---

### Task 12: `FeedScreen` rewrite — stacked browser + FAB-menu focus state

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedScreen.kt`

**Interfaces:**
- Consumes: `StackedCardBrowser` (Task 5), restyled `TopicFilterRow` (Task 4), restyled `PostCard` (Task 6).
- New signature: adds `onSourceClick: (String) -> Unit` and exposes the currently-focused post for the FAB menu:
  ```kotlin
  @Composable
  fun FeedScreen(
      uiState: FeedUiState,
      onTopicSelected: (Topic) -> Unit,
      onPageChanged: (Int) -> Unit,
      onBookmarkClick: (String) -> Unit,
      onReadSourceClick: (String) -> Unit,
      onSourceClick: (String) -> Unit,
      onLoginSucceeded: () -> Unit,
      onLoginDismissed: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  (Same params as today plus `onSourceClick`; "focused post" is `uiState.posts.getOrNull(uiState.currentIndex)`, already tracked via `onPageChanged` → `FeedViewModel.onPageChanged` → `FeedUiState.currentIndex` — no new state needed. Task 13 reads `uiState.currentIndex`/`uiState.posts` directly to build the `SkimbyteFabMenu`, since the FAB menu itself is composed by `FeedNavHost`, not by `FeedScreen` — keeps `FeedScreen` focused on the list/topic-row/card-stack only.)
- Body: `Column { TopicFilterRow(...); StackedCardBrowser(items = uiState.posts, onIndexChanged = onPageChanged, resetSignal = uiState.selectedTopic, emptyContent = { EmptyState/LoadingState/ErrorState per existing branches }) { post -> PostCard(post = post, isBookmarkLoading = post.id in uiState.pendingBookmarkToggles, onBookmarkClick = { onBookmarkClick(post.id) }, onReadSourceClick = onReadSourceClick, onSourceClick = onSourceClick) } }`. Keep the existing `isLoadingInitial`/`error`/empty branches exactly as today, now rendered via `StackedCardBrowser`'s `emptyContent` slot when `uiState.posts.isEmpty()`, or as a full-screen replacement of the whole `StackedCardBrowser` when loading/erroring (match today's `when` branching, just re-pointed at the new browser).

- [ ] **Step 1: Update `FeedScreenPreview` and `FeedScreenLoadingPreview`** to pass `onSourceClick = {}`.

- [ ] **Step 2: Reimplement `FeedScreen`'s body** per Interfaces, removing the old `VerticalPager`/`Box` code and the manual `LaunchedEffect(pagerState.currentPage)`/`LaunchedEffect(uiState.selectedTopic)` pair (now owned by `StackedCardBrowser` via `onIndexChanged`/`resetSignal`).

- [ ] **Step 3: Add an error/empty-state `@Preview`** if not already covered (check existing previews — `FeedScreenLoadingPreview` exists; add `FeedScreenEmptyPreview` using `FeedUiState(posts = emptyList())` if missing).

- [ ] **Step 4: Update `shared/src/commonTest/kotlin/io/redigit/skimbyte/feed/` and/or `androidHostTest` tests** that reference `FeedScreen`'s old parameter list (grep first: `grep -rn "FeedScreen(" shared/`) to add `onSourceClick = {}`.

- [ ] **Step 5: Run the full feed test suite**

Run: `./gradlew :shared:testAndroidHostTest --tests "*Feed*"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedScreen.kt
git commit -m "feed: rebuild FeedScreen on StackedCardBrowser"
```

---

### Task 13: `FeedNavHost` — wires Feed/Search/SourceDetail, nav bar, and FAB menu

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedNavHost.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/FeedNavHostTest.kt`

**Interfaces:**
- Consumes: `FeedScreen` (Task 12), `SearchScreen` (Task 11), `SourceDetailScreen` (Task 10), `SkimbyteBottomNavBar` + `SkimbyteFabMenu` (Tasks 7–8), `FeedViewModel` (existing), `TopLevelDestination`/`FeedHome`/`SearchHome`/`SourceDetail` (Task 9).
- Produces: `@Composable fun FeedNavHost(selected: TopLevelDestination, onDestinationSelected: (TopLevelDestination) -> Unit, viewModel: FeedViewModel = koinViewModel())`.
- Mirrors `BookmarksNavHost.kt`'s existing structure exactly (read it first): a local `navConfig` `SavedStateConfiguration` registering `FeedHome`, `SearchHome`, `SourceDetail` as `NavKey` polymorphic subclasses; `rememberNavBackStack(navConfig, FeedHome)`; `rememberListDetailSceneStrategy<NavKey>()`; `NavDisplay` with `entryProvider`:
  - `entry<FeedHome>(metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { SourceDetailPlaceholder() }))` → `FeedScreen(uiState = uiState, ..., onSourceClick = { backStack.add(SourceDetail(it)) })`, if `uiState.pendingBookmarkAction != null` render `LoginSheet` as today.
  - `entry<SearchHome>(metadata = ListDetailSceneStrategy.listPane(...))` → `SearchScreen(posts = uiState.posts, pendingBookmarkToggles = uiState.pendingBookmarkToggles, onBookmarkClick = viewModel::onBookmarkTapped, onReadSourceClick = ::openUrl, onSourceClick = { backStack.add(SourceDetail(it)) }, onBack = { backStack.removeLastOrNull() })`.
  - `entry<SourceDetail>(metadata = ListDetailSceneStrategy.detailPane())` → `SourceDetailScreen(sourceName = key.sourceName, posts = uiState.posts, pendingBookmarkToggles = uiState.pendingBookmarkToggles, onBookmarkClick = viewModel::onBookmarkTapped, onReadSourceClick = ::openUrl, onBack = { backStack.removeLastOrNull() })`.
  Wraps the whole thing in `Scaffold(bottomBar = { SkimbyteBottomNavBar(selected = selected, onDestinationSelected = onDestinationSelected, onSearchClick = { backStack.add(SearchHome) }, fabMenu = { val focused = uiState.posts.getOrNull(uiState.currentIndex); if (focused != null) { var expanded by remember { mutableStateOf(false) }; var lockedPostId by remember { mutableStateOf<String?>(null) }; val targetId = if (expanded) lockedPostId ?: focused.id else focused.id; val target = uiState.posts.firstOrNull { it.id == targetId } ?: focused; SkimbyteFabMenu(expanded = expanded, onExpandedChange = { newExpanded -> expanded = newExpanded; lockedPostId = if (newExpanded) target.id else null }, isBookmarked = target.isBookmarked, isBookmarkLoading = target.id in uiState.pendingBookmarkToggles, onBookmarkClick = { viewModel.onBookmarkTapped(target.id) }, onShareClick = { /* platform share, out of scope per spec non-goals — no-op or openUrl(target.sourceUrl) as a reasonable stand-in */ openUrl(target.sourceUrl) }, onOpenSourceClick = { openUrl(target.sourceUrl) }) } }) }`. This is where Review Focus item 3 (focus locks while the menu is open) is implemented: `lockedPostId` is captured at expand-time and only cleared on collapse.
- Also render `LoginSheet` the same way `FeedScreen` does today when `uiState.pendingBookmarkAction != null`, so the FAB menu's bookmark action triggers the login flow identically to `BookmarkButton`'s (Review Focus item 5) — this falls out for free since both paths call `viewModel.onBookmarkTapped`.

- [ ] **Step 1: Write the Compose UI test** `FeedNavHostTest` covering: (a) tapping a `PostCard`'s source name navigates to `SourceDetailScreen` showing that source's posts; (b) tapping the nav bar's search icon shows `SearchScreen`; (c) opening the FAB menu, then changing `uiState.posts` (simulate via a fake `PostsRepository`/`FeedViewModel` state update), does **not** change which post the menu's actions target until it's closed and reopened (Review Focus item 3); (d) tapping "Bookmark" in the FAB menu while logged out shows `LoginSheet` (Review Focus item 5).

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*FeedNavHostTest*"`
Expected: FAIL — unresolved.

- [ ] **Step 3: Implement `FeedNavHost`** per Interfaces, reading `BookmarksNavHost.kt` first to match its exact Nav3/`SavedStateConfiguration`/`entryProvider` idioms.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*FeedNavHostTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/feed/ui/FeedNavHost.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/feed/FeedNavHostTest.kt
git commit -m "feed: add FeedNavHost wiring Feed/Search/SourceDetail with nav bar and FAB menu"
```

---

### Task 14: `AccountScreen`

**Files:**
- Create: `shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt`
- Test: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/AccountScreenTest.kt`
- Modify: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt`
- Create: `designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/AccountScreenCatalog.kt`

**Interfaces:**
- Consumes: `AuthRepository` (existing — read `AuthRepository.kt`/`AuthRepositoryImpl.kt` first to confirm the exact session-state shape, e.g. whether it exposes an email field directly or only a token).
- Produces: `@Composable fun AccountScreen(sessionEmail: String?, onLogout: () -> Unit, onLoginSucceeded: () -> Unit, modifier: Modifier = Modifier)` — stateless, same hoisting pattern as `BookmarksListScreen`. If `sessionEmail == null`, shows `LoginSheet(onDismiss = {}, onLoginSuccess = onLoginSucceeded)` inline (matching `BookmarksListScreen`'s logged-out branch); else shows `Text(sessionEmail)` + a `Button("Log out", onClick = onLogout)`.
- A thin router composable `AccountRoute()` (same file) reads `AuthRepository`'s session `StateFlow` via `koinInject()`/a tiny `AccountViewModel` if the existing `AuthRepository` doesn't already expose a convenient `email` — check first; if `AuthRepository.session` already carries an email-bearing session object, no new `AccountViewModel` is needed and `AccountRoute` can `collectAsState()` it directly, consistent with keeping this screen free of new ViewModel logic per the spec's YAGNI framing.

- [ ] **Step 1: Read `AuthRepository.kt` and `AuthRepositoryImpl.kt`** to confirm the session type's shape (does it carry an email string already, from the existing `LoginRequestDto`/session storage?).

- [ ] **Step 2: Write the Compose UI test** `AccountScreenTest` covering: (a) `sessionEmail = null` shows the `LoginSheet`; (b) `sessionEmail = "demo@skimbyte.app"` shows that email and a "Log out" button; (c) tapping "Log out" invokes `onLogout`.

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :shared:testAndroidHostTest --tests "*AccountScreenTest*"`
Expected: FAIL — unresolved.

- [ ] **Step 4: Implement `AccountScreen`** (and `AccountRoute` if needed) per Interfaces.

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :shared:testAndroidHostTest --tests "*AccountScreenTest*"`
Expected: PASS

- [ ] **Step 6: Add light+dark `@Preview`s** — logged-out and logged-in states.

- [ ] **Step 7: Add a designsheet catalog entry** (`CatalogItem("account-screen", "Account", "Bookmarks")`).

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/auth/ui/AccountScreen.kt shared/src/androidHostTest/kotlin/io/redigit/skimbyte/auth/AccountScreenTest.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/components/AccountScreenCatalog.kt designsheet/src/commonMain/kotlin/io/redigit/skimbyte/designsheet/catalog/CatalogRegistry.kt
git commit -m "auth: add AccountScreen"
```

---

### Task 15: `App.kt` rewiring — 3 destinations, floating nav everywhere

**Files:**
- Modify: `shared/src/commonMain/kotlin/io/redigit/skimbyte/App.kt`
- Modify: `shared/src/androidHostTest/kotlin/io/redigit/skimbyte/` (the deleted `AppScreenshotTest.kt` was removed pre-existing in this branch's working tree — read current `git status` first; if a replacement `AppScreenshotTest` is expected per CONTRIBUTING's screen-level coverage, add one covering Feed/Bookmarks/Account tab switching, otherwise skip — screens below `App` already carry their own coverage per Tasks 1–14)

**Interfaces:**
- Consumes: `FeedNavHost` (Task 13), `BookmarksNavHost` (existing, unchanged), `AccountRoute`/`AccountScreen` (Task 14), `SkimbyteBottomNavBar` (Task 8), `TopLevelDestination` (Task 9).

- [ ] **Step 1: Remove the `NavigationSuiteScaffold`/`NavigationSuiteScope.topLevelItem` code** from `App.kt`.

- [ ] **Step 2: Reimplement `App()`'s body**:
  ```kotlin
  var selectedTopLevel by remember { mutableStateOf(TopLevelDestination.Feed) }
  when (selectedTopLevel) {
      TopLevelDestination.Feed -> FeedNavHost(selected = selectedTopLevel, onDestinationSelected = { selectedTopLevel = it })
      TopLevelDestination.Bookmarks -> Scaffold(bottomBar = { SkimbyteBottomNavBar(selected = selectedTopLevel, onDestinationSelected = { selectedTopLevel = it }, onSearchClick = null, fabMenu = null) }) { padding -> Box(Modifier.padding(padding)) { BookmarksNavHost() } }
      TopLevelDestination.Account -> Scaffold(bottomBar = { SkimbyteBottomNavBar(selected = selectedTopLevel, onDestinationSelected = { selectedTopLevel = it }, onSearchClick = null, fabMenu = null) }) { padding -> Box(Modifier.padding(padding)) { AccountRoute() } }
  }
  ```
  (`FeedNavHost` owns its own `Scaffold`/bottom bar per Task 13, since it needs the FAB menu — the other two branches wrap their own `Scaffold` here since they don't.)

- [ ] **Step 3: Grep for any remaining references to the removed `topLevelItem`/`NavigationSuiteScaffold` imports** and clean them up.

- [ ] **Step 4: Run the full shared Android test suite**

Run: `./gradlew :shared:testAndroidHostTest`
Expected: PASS (all tasks' tests, plus any pre-existing ones untouched by this plan)

- [ ] **Step 5: Run the full Roborazzi verification**

Run: `./gradlew :shared:verifyRoborazziAndroidHostTest`
Expected: PASS — if any non-trivial diffs remain from upstream palette/shape changes not yet captured, record them (`recordRoborazziAndroidHostTest`) and re-verify.

- [ ] **Step 6: Launch the design sheet and the Android app for a manual visual pass** (per CLAUDE.md's UI-change verification guidance)

Run: `./gradlew :designsheet:run` and separately `./gradlew :androidApp:assembleDebug`
Expected: designsheet launches showing every new/changed catalog entry (Tasks 1–14) rendering correctly in both light and dark; confirm no crash navigating Feed → tap a source name → Source detail → back, Feed → search icon → type a query → back, and switching all three bottom tabs.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/io/redigit/skimbyte/App.kt
git commit -m "app: wire Feed/Bookmarks/Account through the floating nav bar"
```