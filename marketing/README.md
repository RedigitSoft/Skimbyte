# Marketing assets

Store listings, social banners, and the copy that goes with them. Everything in
`out/` is generated from real app screenshots — no mockups.

## Layout

```
copy/        Listing & post text: play-store.md, app-store.md, social-post.md, desktop-web.md
shots/       Raw app captures (web/Wasm build in demo mode, exact device resolutions)
templates/   HTML templates the assets are rendered from
out/
  appstore/  icon-1024.png + five 1290×2796 panels (6.7-inch size)
  play/      icon-512.png, feature-graphic.png (1024×500) + five 1080×2340 panels
  social/    banner-1200x630.png (OG/post image), github-preview-1280x640.png (+@2x masters)
  desktop/   hero-2880x1620.png (desktop app in a macOS window)
```

The five screenshot panels (both stores, same order): feed dark, topic picker,
feed light, appearance/accents, post detail. Different panels deliberately show
different accent colors — it sells the customization.

`website/og-image.png` is a copy of `out/social/banner-1200x630.png`; the OG meta
tags in `website/index.html` point at it.

## Regenerating everything

```bash
npm i --prefix marketing playwright-core   # once
node marketing/generate.mjs                # renders out/ + website/og-image.png
```

`generate.mjs` re-renders every template at exact store dimensions, derives the
Play sizes from the App Store masters, rasterizes the store icons (needs
`rsvg-convert`, `brew install librsvg`), and refreshes `website/og-image.png`.
Panel captions live in the `PANELS` array at the top of the script.

`out/placeholder/post-placeholder-{light,dark}.png` (1600×1000) are branded
demo-post placeholder images (dithered field + S glyph); the matching dummy
title/description lives in `copy/demo-post.md`.

## How the raw captures were made

1. Serve the production Wasm build: `./gradlew :webApp:wasmJsBrowserDistribution`,
   then `python3 -m http.server 8787` from `webApp/build/dist/wasmJs/productionExecutable`.
2. Screenshot it headless (Playwright/Chromium) at 430×932 @3x for phone
   (= 1290×2796) and 1440×900 @2x for desktop — the app runs in demo mode on
   bundled sample data.
3. Render `templates/*.html` (caption + framed screenshot, app design tokens) at
   exact store dimensions; `sips` derives the Play sizes from the App Store masters.

## Still worth capturing later

- **Bookmarks screen** — demo mode gates it behind sign-in; grab it from a
  signed-in device and drop it into a `panel.html` render.
- **Play tablet screenshots** (7"/10") if the Android app targets tablets.
- The web captures show monogram placeholder thumbnails (demo mode has no image
  CDN). Captures against the real backend would show article images, like the
  iOS hero shots in `website/screen-*.png`.
