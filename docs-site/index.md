# Skimbyte

Skimbyte turns the firehose of developer news (Kotlin, Android, AI, tooling release feeds) into a
once-a-day brief of short, readable summaries. Pick your topics, skim the cards, bookmark what you
want to keep, and track your reading streak — the same UI, rendered natively from one Kotlin
codebase on every platform.

It is also deliberately a **reference project**: a production-shaped Kotlin Multiplatform +
Compose Multiplatform app with real architecture, a three-layer test suite, adaptive layouts, and
a living design-system catalog.

## Platforms

| Target | Entry point |
|---|---|
| Android | `androidApp/` |
| iOS | `iosApp/` (Xcode project) |
| Desktop (JVM) | `desktopApp/` |
| Web (Kotlin/Wasm + Kotlin/JS) | `webApp/` |

## Try it in 60 seconds

```sh
./gradlew :desktopApp:run
```

A fresh clone runs in **demo mode** out of the box: with no API URL configured, Koin swaps in a
mock network module and the whole app works end to end on bundled sample data — feed, topics,
bookmarks, settings. The live backend (posts API + auth server) is closed-source; to point the
app at a compatible backend, copy `local.properties.example` to `local.properties` and fill in
the endpoints.

## Explore

- [Architecture](architecture.md) — how the modules, state, navigation, and DI fit together.
- [Testing](testing.md) — the unit / Compose UI / screenshot test layers.
- [Design system](design-system.md) — tokens, components, and the designsheet catalog.
- [API reference](api/) — Dokka docs for the `shared` module.
- [Live designsheet](designsheet/) — the component catalog running in your browser (Wasm).
