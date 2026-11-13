# Desktop & web assets — where each one goes

## GitHub repository

- **Social preview** (repo Settings → General → Social preview):
  `marketing/out/social/github-preview-1280x640.png`
- **About blurb** (repo description, ≤ ~120 chars):
  > The day's developer news, skimmed. Open-source KMP/Compose app for Android, iOS, desktop & web — one codebase.
- **Topics:** `kotlin-multiplatform`, `compose-multiplatform`, `android`, `ios`,
  `desktop`, `wasm`, `news`, `rss`
- **README hero:** `marketing/out/desktop/hero-2880x1620.png` (desktop app in a
  macOS window) or `marketing/out/social/banner-1200x630.png` (phone + desktop).

## Website (GitHub Pages)

- **Open Graph / Twitter card image:** `website/og-image.png` (copied from
  `marketing/out/social/banner-1200x630.png`). Meta tags are in `website/index.html`.
- The landing page keeps using `website/screen-dark.png` / `screen-light.png`
  (real-data iOS captures) for the hero.

## GitHub release notes banner

Add at the top of a release:

```markdown
![Skimbyte on desktop](https://skimbyte.com/og-image.png)
```

## Release / download blurb (reusable)

> **Skimbyte for desktop** — the same daily developer brief, native on your Mac,
> Windows, or Linux machine. Signed & notarized DMG for Apple Silicon, MSI for
> Windows x64, DEB for Debian/Ubuntu. Packaged and published by GitHub Actions on
> every release — nothing hand-built.
