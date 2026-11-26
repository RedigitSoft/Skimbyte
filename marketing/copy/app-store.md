# iOS App Store listing — App Store Connect submission pack

Assets: `marketing/out/appstore/` — icon-1024.png, 01–05 screenshots (1290×2796,
the 6.7" size; App Store Connect scales it for smaller devices), ipad-01-feed.png
(2732×2048, the accepted 12.9"/13" size — required because the app runs on iPad),
universal-5244x2950.png (the 16:9 universal creative asset, no alpha).

First shipped version: **1.0.2**, live since 2026-11-23 (`VERSION` →
`MARKETING_VERSION` via `iosApp/Configuration/Config.xcconfig`).

## Name (max 30 chars)

> Skimbyte — Dev News, Skimmed
<!-- 28 chars -->

## Subtitle (max 30 chars)

> The daily developer brief
<!-- 25 chars -->

## Promotional text (max 170 chars, editable without review)

> Kotlin, Swift, Flutter, Android, AI and more — summarized into a once-a-day brief
> you finish with your coffee. Free, open source, no ads, no tracking.
<!-- 150 chars -->

## Description (max 4000 chars)

> The day's developer news. Skimmed.
>
> Skimbyte reads the developer-news firehose and turns it into a once-a-day brief of
> short, readable summaries — finish it with your coffee instead of doomscrolling a
> dozen feeds.
>
> PICK YOUR TOPICS
> Kotlin, Android, KMP / Compose Multiplatform, iOS & Swift, Flutter & Dart, React
> Native, AI for mobile devs, tooling & products, XR — follow what you build, skip
> what you don't.
>
> ONE BRIEF AT A TIME
> Each story is a card: a one-minute summary, the source, and the read time. Swipe
> to the next, or open the full article when it's worth your attention.
>
> BOOKMARK THE KEEPERS
> Sign in with Google or GitHub to sync bookmarks across your devices — no account
> needed just to read.
>
> MAKE IT YOURS
> Matte black or off-white theme, and ten sharp accent colors.
>
> NO ADS. NO TRACKING.
> Skimbyte is free and open source (Apache-2.0). No ads, no analytics, no engagement
> tricks — just the news, summarized. The entire app is public on GitHub.
>
> STRAIGHT FROM THE SOURCE
> Summaries are AI-generated from the original articles and always attributed —
> every card names its source and links to the full story.
>
> ONE CODEBASE, EVERY PLATFORM
> Skimbyte is built with Kotlin Multiplatform and Compose Multiplatform — the same
> code ships natively on iOS, Android, macOS, Windows, Linux, and the web.
>
> Website: https://skimbyte.com
> Source: https://github.com/RedigitSoft/Skimbyte

<!-- Matches the live Play description (incl. STRAIGHT FROM THE SOURCE). If
     APPLE_SIGN_IN_ENABLED is flipped on before submission, change the BOOKMARK
     line to "Sign in with Apple, Google, or GitHub…". -->

## Keywords (max 100 chars, comma-separated, no spaces)

> developer,news,kotlin,swift,flutter,android,ai,digest,brief,rss,tech,programming,daily,summary
<!-- 95 chars -->

## What's New (release notes for 1.0.2 — same as Play)

> Welcome to Skimbyte — your daily, skimmable brief of developer news.
>
> • One short summary per story, so you stay current in minutes
> • Pick your topics and get a feed tuned to them
> • Bookmark stories and sync them across your devices
> • Every story links to its original source
> • Light and dark themes with accent colors
> • Sign in with Google or GitHub
>
> Thanks for trying our first release. We'd love your feedback!

## URLs

- Marketing URL: https://skimbyte.com
- Privacy Policy URL: https://skimbyte.com/docs/privacy/
- Terms of Use (EULA) URL: https://skimbyte.com/docs/terms/
- Support URL: https://github.com/RedigitSoft/Skimbyte/issues

## Categories, price, copyright

- Primary category: **News** · Secondary: **Developer Tools**
- Price: Free, no in-app purchases
- Copyright: `© 2026 Redigit Softwares Private Limited`

## App Review Information

- Contact: Akshay — akshay@redigit.io — phone: fill in App Store Connect directly
  (review contact info is private to Apple; keep it out of this public file)
- **Sign-in is NOT required** to use the app (leave the demo-account checkbox off);
  reading, topics, and theming all work signed out. Sign-in only adds bookmark sync.
- Notes field — paste:

  > Skimbyte works fully without an account: browsing the daily brief, picking
  > topics, and themes need no sign-in. Signing in (Google or GitHub OAuth) only
  > adds bookmark syncing.
  >
  > To review the signed-in experience without a Google/GitHub account, open this
  > URL on the device — it signs the app into a dedicated reviewer account via the
  > same deep-link callback the OAuth flows use:
  >
  > <REVIEWER-SIGN-IN-URL>
  >
  > Account deletion (Guideline 5.1.1(v)) is in Account → Delete my data; it
  > deletes the server-side account and uploaded images immediately.

  Replace `<REVIEWER-SIGN-IN-URL>` with the Skimbyte reviewer sign-in URL from the
  auth server's private config — the same one used in the Play App access form.
  Never paste any part of it (path, ids, or key) into this public file.

## App Privacy (nutrition labels)

Mirrors the live Play data-safety form. Signed-out users: nothing collected.

- **Collect data?** Yes.
- **Contact Info → Email Address** and **Name**: collected (from Google/GitHub
  OAuth at sign-in), **linked to identity**, purpose **App Functionality** only,
  **not** used for tracking.
- **Identifiers → User ID**: collected (account id for bookmark sync), linked,
  App Functionality, no tracking.
- **User Content → Photos or Videos**: collected only if the user uploads an
  avatar/banner, linked, App Functionality, no tracking.
- Everything else (location, browsing history, usage data, diagnostics,
  purchases…): **Not collected** — there are no analytics or crash SDKs.
- Tracking (ATT): **No** — nothing leaves for ads/data brokers; no ATT prompt.
- Data is encrypted in transit; users can delete their data in-app
  (Account → Delete my data) or via https://skimbyte.com/docs/privacy/#deleting-your-data

## Age rating questionnaire

All content categories: **None** (curated developer-news summaries). Key answers:

- Unrestricted web access: **No** — article links open in the system browser
  (`UIApplication.openURL`), not an in-app browser.
- Gambling, contests, horror, mature themes, etc.: No.
- User-generated content visible to others: No (avatars/banners are only shown to
  their owner).

Expected rating: **4+**.

## Content rights declaration

App Store Connect asks whether the app shows third-party content. Answer **Yes,
and I have the necessary rights**: cards show AI-generated summaries of publicly
available articles with the source always named and linked; full articles open at
the source's own site. (Same model the live Play listing declares.)

## Submission checklist (for the next release)

1.0.2 shipped to the App Store on 2026-11-23 and was approved **without** Sign in
with Apple — a deliberate deferral. For update submissions:

1. **Guideline 4.8 (Login Services).** The app offers Google/GitHub login only;
   Apple approved 1.0.2 anyway, but the question can resurface on any update
   review. The client flow is built but gated off
   (`ApiConfig.APPLE_SIGN_IN_ENABLED = false`, awaiting the auth server's
   `/auth/oauth/apple/start`). If/when enabling: verify the server endpoint,
   flip the flag, update the description/What's New sign-in lines, and rebuild.
2. Build & archive from Xcode (`iosApp/`), upload via Organizer;
   `MARKETING_VERSION` follows `VERSION`.
3. Screenshots, App Privacy, age rating, and agreements are one-time forms —
   already filled; only touch them if the relevant facts change.
4. Update the review notes if the sign-in options change; the reviewer URL
   (from the auth server's private config) must cold-start the app on a device.

## Notes

- Icon: App Store Connect wants the 1024×1024 full-square PNG without alpha
  (`icon-1024.png` is flattened, square corners).
- The Xcode asset catalog icon in `iosApp/` is separate and already set up.
- Screenshot captions are baked into the panels (same five as Play).