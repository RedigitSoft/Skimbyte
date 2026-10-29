# Material 3 Expressive redesign — feed, navigation, and source/topic detail

## Intent and why

Skimbyte's shell currently uses stock Material3 defaults (a teal accent, `NavigationSuiteScaffold`,
per-item `FilterChip` topics, a full-bleed one-post-per-page `VerticalPager`). This project restyles
the app into a distinctive, premium-feeling Material 3 Expressive experience, using a Dribbble video
as the structural/motion reference and `DESIGN.md` (an extraction of claude.com's visual language) as
the color-token source. Skimbyte is an open-source reference project meant to model high-end practice,
so the bar is a cohesive, intentional design system — not "it technically works."

## Reference material

- **Video** (`https://cdn.dribbble.com/userupload/26880143/file/original-3dccbd93ae35499fa0536ed14782dcbe.mp4`,
  downloaded and frame-sampled locally for structural study — not asset reproduction). It's an 11s
  looping demo of a dark photo-browsing app. Observed patterns, in order:
  - **Feed ("For You")**: tiny uppercase eyebrow label (date) above a bold large title, a horizontally
    scrollable row of category labels where only the *active* one has a filled rounded-pill
    background (others are plain text, no border/fill) — not uniform bordered chips.
  - Below that: one large, heavily-rounded hero card (photo, bottom-left avatar+name+meta overlay on
    a gradient scrim) with the **next card's top edge peeking** below it — a stacked/peeking card
    browser, not a full-bleed one-item-per-screen pager.
  - **Collection screen** (tap a category): same card treatment, "COLLECTIONS" eyebrow + bold title,
    a two-icon stack/grid view toggle top-right. Card content auto-cycles between photos/people.
  - **FAB menu moment**: pressing the nav's center button dims the card and morphs the nav's center
    button into a vertical cluster of circular action buttons (share, edit, play) stacked above an
    accent-filled circular confirm button — a textbook Material 3 Expressive FAB-menu transform.
  - **Author detail** (tap the avatar): hero image collapses into a shorter banner, a location line
    and bio paragraph appear below it, then a 2-column photo grid. The nav's leading icon swaps to a
    collapse/back affordance on this screen.
  - **Floating nav**, present on every screen: pill-shaped, dark, semi-opaque — leading icon, search,
    a raised center FAB, the user's own avatar, and one small trailing icon.

- **`DESIGN.md`**: a marketing-site (claude.com) design system extraction. Its component list
  (hero-band, pricing-tier-card, cta-band, footer, etc.) does not apply to a mobile app and is **not**
  reused. What *is* reused: the color tokens (warm dark surfaces + coral `#cc785c` single accent) and
  the general rhythm of an uppercase eyebrow label paired with a bold large title.

## Goals

1. New dark, coral-accented Material 3 Expressive theme (`shared/theme`), replacing the current teal
   scheme, built from `DESIGN.md`'s color tokens.
2. Feed restructured as a stacked/peeking vertical card browser, replacing today's full-bleed
   one-post-per-page `VerticalPager`.
3. Topic row restyled to a single sliding/filled active-pill indicator instead of per-item
   `FilterChip` borders.
4. New floating pill-shaped bottom navigation bar (replacing `NavigationSuiteScaffold`) with a raised
   center button that expands into a Material 3 Expressive FAB menu, scoped to the focused post, with
   three actions: **Bookmark**, **Share**, **Open source link**.
5. New **Source detail** screen: tapping a post's source name opens a screen with a collapsing-banner
   header (source name + post count) and that source's posts in the same stacked-card browser. This is
   the video's "author detail" pattern re-skinned onto Skimbyte's actual domain (`Post.sourceName`,
   a publication, not a person) — confirmed with the user as the intended mapping.
6. Small **in-app search** (nav search icon): filters currently-loaded feed posts by title substring,
   rendered via the same stacked-card browser. No new backend endpoint. Operates across *all*
   currently-loaded posts regardless of the feed's active topic filter — search and topic-filter are
   independent, not composed (my call: predictable "search everything I've seen" beats a filter that
   silently narrows based on whatever topic pill was last tapped).
7. Small **Account** screen (nav trailing avatar): logged out → opens the existing `LoginSheet` exactly
   as it's already invoked elsewhere (e.g. `BookmarksListScreen`) — same `ModalBottomSheet` presentation
   on every target, no new adaptive treatment, since that's how it already ships today. Logged in →
   shows the session email and a log-out action, built on the existing `AuthRepository`.
8. Every new/changed component gets light+dark `@Preview`s, unit/UI/Roborazzi tests per
   `CONTRIBUTING.md`, and a `designsheet` catalog entry in the same change.
9. Foldable/resizable-window support preserved throughout (no hardcoded breakpoints).

## Non-goals

- No new backend/API surface. Source detail and search both operate on posts already fetched by
  `PostsRepository`/`FeedViewModel` — no new network calls in this pass.
- No profile editing, avatar upload, or multi-account support in the Account screen — email + log out
  only.
- Not attempting pixel-identical reproduction of the reference video's stock photography or specific
  illustration assets — only its structural, motion, and component patterns inform an original
  implementation.
- Not switching to a licensed/bundled serif display font (see Typography decision below).

## Anti-goals (would make this a failure even if it "works")

- Breaking the foldable/adaptive requirement: hardcoding phone-only breakpoints for the stacked-card
  feed or the floating nav bar instead of using `material3.adaptive` window-size-class/posture APIs.
- Nesting the new stacked-card feed's scroll inside another scrollable container in the same
  orientation (the documented infinite-constraint crash).
- Shipping any new composable without a `@Preview`, any new ViewModel logic without a unit test, or
  any new/changed shared design-system component without a Roborazzi test and a `designsheet` entry.
- Introducing a second accent color or a non-dark surface tone — `DESIGN.md` explicitly warns against
  a fourth surface tone; the app stays single-accent (coral) on dark throughout.
- Using `Navigation Compose` (nav2) anywhere instead of Navigation 3.

## Design decisions (confirmed with the user, or stated as defaults during brainstorming with no
objection)

1. **Color** — `Primary` = `DESIGN.md` coral `#cc785c`, `OnPrimary` = white. Dark surfaces re-based on
   `DESIGN.md`'s *warm* dark tones instead of the current cool near-black: `Background` = `surface-dark`
   `#181715`, `Surface` = `surface-dark-elevated` `#252320`, `SurfaceVariant` = `surface-dark-soft`
   `#1f1e1b`, `OnBackground`/`OnSurface` = `on-dark` `#faf9f5`, `OnSurfaceVariant` = `on-dark-soft`
   `#a09d96`, `Error` = `#c64545`. The shipped app stays dark-only per the existing product-spec
   comment in `Theme.kt`; the `darkTheme` parameter and a light `@Preview` variant remain for
   component-level previews only, using the same coral primary on a light scheme.
2. **Typography** — keep the existing system/default sans type face; do **not** bundle `DESIGN.md`'s
   serif display font (no font resources exist in the project today, and the video's own type is a
   bold sans, not a serif, so matching the video and skipping a new font dependency point the same
   way). Adopt `DESIGN.md`'s *rhythm*, not its literal typeface: bold, tightly-tracked large titles for
   screen headers, and a new small-caps-style uppercase "eyebrow" label style (derived from
   `labelSmall`) shown above every screen title — mirrors both the video's date/"COLLECTIONS" labels
   and `DESIGN.md`'s `caption-uppercase` token.
3. **Shape** — a single hierarchical large-radius `Shapes` scale (Material 3 Expressive), larger than
   default M3 corner radii across cards, the nav bar, and buttons — matches both `DESIGN.md`'s rounded
   scale and the video's consistently large card radius.
4. **Feed** — the `VerticalPager` full-bleed card becomes a stacked/peeking card browser: the current
   card large and centered, the next card's top edge peeking below it, swipe/scroll snaps between
   items. Left to the builder: the concrete Compose primitive (e.g. a custom snapping `LazyColumn`, or
   a `VerticalPager` with scaled/offset neighbor pages) — must not violate the nested-scrollable rule.
5. **Topic row** — replace `FilterChip`-per-item with a single sliding/filled pill marking the active
   topic; inactive topics render as plain text; row stays horizontally scrollable. (Verified: `Topic`
   is already a flat, single-select enum — `Kotlin`/`Android`/`Kmp`/`Tooling`/`All` — so a single
   active-pill indicator is mechanically straightforward, no data-model change needed.)
6. **Navigation shell** — replace `NavigationSuiteScaffold` with a custom floating pill-shaped bottom
   bar: Feed · Search · raised center FAB-menu · Bookmarks · Account, still routed through Navigation 3.
   Left to the builder: its adaptive behavior at expanded/unfolded widths (stay a floating bar vs.
   switch to a rail) using `material3.adaptive` window size class — no hardcoded breakpoint.
7. **FAB menu** — Material 3 Expressive's FAB-menu component, expanding to Bookmark / Share / Open
   source link for the currently focused feed post. "Focused post" = the current front/snapped card in
   the stacked browser (the same single-index concept `FeedUiState.currentIndex` already tracks via
   `onPageChanged`, regardless of which concrete Compose primitive decision 4 lands on); opening the
   menu locks the target to that post until the menu closes, even if the user scrolls behind it. Left
   to the builder: confirm the exact Material 3 Expressive API surface in the pinned `material3`
   version (`1.12.0-alpha03`) via the `material-3` skill during planning; if the alpha doesn't yet
   expose it, bump the version-catalog pin per `CONTRIBUTING.md`'s "bump proactively" guidance rather
   than hand-rolling the component. Because a `material3`/`material3-adaptive` alpha bump has app-wide
   blast radius (shapes/colors/behavior of every existing Material3 usage, not just the new FAB menu),
   do a full visual pass over `designsheet` and the existing screens after bumping, not just FAB-menu-
   specific testing.
8. **Source detail** — tapping a post's source name (in `PostCard`) navigates via Navigation 3 to a
   screen with a collapsing-banner header (eyebrow "SOURCE" + source name + post count) and that
   source's posts in the same stacked-card browser. Scoped to posts already loaded in the feed/session
   for v1 (filtered client-side) — no new `PostsRepository` method. (Verified: `FeedViewModel.loadMore`
   appends to a growing list with no eviction, so this is a genuine "not yet paged in" limitation, not
   a silent-data-loss one — a source's posts that haven't been scrolled to yet simply won't appear.
   Acceptable for v1, consistent with the Non-goals section.) If that reads as too limited during
   planning, adding a fetch-by-source method is a reasonable builder call, not a required one.
9. **PostCard** — restyled to the hero-card look (large rounded corners, image-led, bottom scrim
   gradient, source name + topic + date as a text overlay — no avatar, since sources are publications,
   not people) while keeping the existing bookmark button, markdown summary, and "read full source"
   affordance. Exact placement of the full markdown body (today shown inline on the full-bleed card) is
   left to the builder given the card is now more image-led/compact.

## Scope — files/areas touched

- `shared/theme/Color.kt`, `Theme.kt` — new palette, Expressive shape scale, eyebrow text style.
- `shared/feed/ui/FeedScreen.kt`, `PostCard.kt`, `TopicFilterRow.kt` — restructured.
- `shared/feed/ui/` (new) — stacked/peeking card browser primitive, FAB-menu component, floating nav
  bar component.
- `shared/nav/SkimbyteNavKeys.kt` — extend `TopLevelDestination`/`NavKey`s for Search, Account, Source
  detail.
- New feature slice for Source detail (view model/state deriving from already-loaded posts).
- New minimal Account screen (`shared/auth/ui` or similar), built on `AuthRepository`.
- `shared/App.kt` — new nav shell wiring the floating bar and all destinations.
- `designsheet/` — new/updated catalog entries for every new or changed shared component.
- Tests: unit tests for new ViewModel logic (source filtering, search filtering); Compose UI tests for
  new interactive composables; Roborazzi goldens for new/restyled shared components.

## Constraints (non-negotiable, from `CLAUDE.md` / `CONTRIBUTING.md`)

- Every UI composable gets at least light+dark `@Preview`s.
- Stateless composables; state hoisted to a ViewModel exposing immutable `StateFlow<UiState>`.
- No hardcoded colors/dimensions/text styles — `MaterialTheme` and shared tokens only.
- Unit tests for all logic; Compose UI tests for interactive/stateful composables; Roborazzi tests for
  shared design-system components.
- Koin DI only, across all targets.
- `designsheet` updated in the same change as any new/changed shared component or token.
- Dependencies only via `gradle/libs.versions.toml` — bump pinned versions if needed, don't add
  unlisted coordinates.
- `material3.adaptive` for foldable/resizable-window support — no hardcoded breakpoints; no nested
  same-orientation scrollables.
- Navigation 3 only, no Navigation Compose (nav2).

## What's left to the builder

- Exact Compose primitive for the stacked/peeking card browser.
- Exact Material 3 Expressive API names/usage for the FAB menu and the floating nav bar in the pinned
  `material3`/`material3Adaptive` versions — confirm via the `material-3` skill while planning; bump
  the catalog pin if the current alpha lacks what's needed.
- Exact adaptive behavior of the floating nav bar at expanded/unfolded widths.
- Whether Source detail needs a `PostsRepository` addition or can stay scoped to already-loaded posts
  (default: stays scoped, per Design decision 8).
- Final motion specifics for the FAB-menu expand/collapse and the card-peek transition — use Material 3
  Expressive's default motion/spring specs rather than hand-tuned custom animations, per
  `CONTRIBUTING.md`.