# Design system

Skimbyte's UI is built from a small set of shared tokens and reusable components, all cataloged
in the **designsheet** — a standalone, runnable style guide.

## The designsheet

Every shared design token and reusable component has a catalog entry. The designsheet is a real
app (Navigation 3, adaptive list-detail layout) that builds for desktop and the web:

```sh
./gradlew :designsheet:run                        # desktop
./gradlew :designsheet:wasmJsBrowserDevelopmentRun # web (dev server)
```

A deployed build is browsable at [designsheet/](../designsheet/) on this site.

!!! note "Contribution rule"
    Any new or changed shared component or token must be reflected in the designsheet **in the
    same change** — the catalog is the source of truth for what exists, and it is what keeps the
    system honest.

## Principles

- **Theme:** Material 3 Expressive (`MaterialExpressiveTheme` with spring-based motion), with
  Skimbyte's brand colors layered onto the expressive color roles. Dark is the default.
- **No hardcoded values:** colors, shapes, and type always come from `MaterialTheme` /
  shared tokens — components never embed raw hex values or dp-sized text styles.
- **Stateless components:** every reusable composable takes `(state, callbacks)` and hoists all
  state; each has light + dark `@Preview`s.
- **Screenshot-tested:** shared components carry Roborazzi goldens, so any pixel change is a
  reviewed diff, not a surprise.

## Brand assets

The icon and wordmark SVGs live in `docs/branding/` and are the single source every platform
icon is generated from (Android mipmaps + adaptive vectors, iOS asset catalog, desktop
`.icns`/`.ico`/`.png`, web favicons).
