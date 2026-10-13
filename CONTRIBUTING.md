# Contributing to Skimbyte

Skimbyte is an open-source Kotlin Multiplatform + Compose Multiplatform reference project. The goal is for it to be a high-quality example that developers can learn from and take inspiration from — not just a working app. Every contribution should meet the standards below, no exceptions for "quick" additions.

## Tech stack

- Always build against the **latest stable** Kotlin and Compose Multiplatform versions pinned in `gradle/libs.versions.toml`. Bump them proactively when new stable releases land rather than letting the project drift.
- **Material3**, preferring **Material3 Expressive** components/motion/shape where available. No Material2.
- **Coroutines + Flow** for async work and state streams.
- **Koin** for dependency injection across all targets (Android, iOS, JVM, JS, Wasm) — no platform-specific DI frameworks.
- **Navigation 3** (`androidx.navigation3`, via the JetBrains-maintained multiplatform artifacts) for all screen-to-screen navigation. No Navigation Compose (nav2) in new code.
- `kotlinx.serialization` for any serialization needs, including Nav3's type-safe `NavKey` routes.
- All dependency coordinates and versions go through the version catalog (`gradle/libs.versions.toml`); never hardcode a dependency string in a module's `build.gradle.kts`.

## Architecture

- MVVM with unidirectional data flow: a ViewModel exposes a single immutable UI state (`StateFlow<UiState>`); composables are stateless and push user intent back up via lambdas/event callbacks. Avoid two-way bindings.
- Business/domain logic lives in `shared`'s `commonMain`. Only drop into `expect`/`actual` when a platform API genuinely has no common abstraction.
- UI state classes are immutable (`data class`, `val` properties) and annotated `@Immutable`/`@Stable` where the Compose compiler can't infer stability, so recomposition stays scoped and predictable.

## Compose standards

- Composable function names are `PascalCase`; the first optional parameter is always `modifier: Modifier = Modifier`.
- Stateless-first: screen/feature-level composables take state + event lambdas. Only keep `remember { mutableStateOf(...) }` for UI-local concerns (animation, scroll, transient visual state) — never for anything that represents real app state.
- No hardcoded colors, dimensions, or text styles. Pull from `MaterialTheme` and the shared design tokens (color scheme, typography scale, spacing, shape).
- **Every composable that renders UI must have at least one `@Preview`.** Provide light/dark previews at minimum, and additional previews for distinct states/variants/sizes where meaningful.
- Prefer Material3 Expressive motion, shape, and color systems, and immersive/adaptive layout primitives (edge-to-edge, `safeContentPadding`, adaptive layout APIs) over bespoke one-off implementations.
- Keep composable parameters stable: avoid plain mutable collections or capturing lambdas with unstable closures in hot-path composables.
- **Every screen must support foldable devices and resizable/arbitrary window sizes** — compact phone, folded/unfolded foldable postures, tablet, and resizable desktop/web windows. Use `material3.adaptive` (window size class, posture) and `adaptive-navigation3`'s `ListDetailSceneStrategy`/`SupportingPaneScaffold` with Nav3 rather than hardcoding breakpoints or assuming a single-pane phone layout.
- A composable that nests one scrollable inside another in the same orientation (e.g. `LazyColumn`/`LazyVerticalGrid` inside `Modifier.verticalScroll`) is a bug, not a style choice — it crashes with an infinite-constraint exception. Each piece of content owns its own scrolling.

## Testing standards

Every piece of logic and every UI component needs tests — this is non-negotiable for a project meant to model best practices.

- **Unit tests** for all logic: ViewModels, use cases, reducers, utilities. Live in the relevant `*Test` source set (`commonTest`, `jvmTest`, etc.) and run on every target that owns the code.
- **UI tests** for every composable with interaction or conditional rendering, using `createComposeRule` / the Compose Multiplatform `runComposeUiTest` API, covering its distinct states.
- **Screenshot/visual regression tests** via Roborazzi for shared design-system components, captured per theme (light/dark) at minimum. Golden images are committed next to the test that produces them (`src/androidHostTest/roborazzi/`, configured via the `roborazzi { outputDir = ... }` Gradle extension) — never left in `build/`, which is gitignored. Regenerate with `./gradlew :shared:recordRoborazziAndroidHostTest` and commit the result; verify with `./gradlew :shared:verifyRoborazziAndroidHostTest`.
- A PR adding logic without tests, or a new composable without a preview, should not be merged.

## The `designsheet` module

`designsheet` is a standalone, runnable module (desktop + web entry points) that acts as a living style guide / component catalog for the whole design system — similar in spirit to Material Catalog or Now in Android's component showcase. It contains:

- Every design token: color scheme, typography scale, spacing scale, shape scale, elevation.
- Every reusable component from `shared`, rendered in all of its meaningful states/variants, each backed by a `@Preview`.

Navigation between catalog entries is Nav3 + `ListDetailSceneStrategy`, so the catalog itself doubles as a reference implementation of the foldable/resizable-window requirement above: it reflows between a single list-only pane (compact width) and a side-by-side list+detail pane (expanded width, unfolded foldable, resized desktop/web window).

When you add or change a shared component or token, update its entry in `designsheet` in the same PR — it must never fall out of sync with the actual design system.

## Documentation

Public APIs (composables, ViewModels, use cases) get a short KDoc comment describing intent/usage/non-obvious behavior — not a restatement of the signature.