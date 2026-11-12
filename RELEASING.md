# Releasing Skimbyte

How each platform's build gets from this repo to users.

## Desktop (automated)

Push a `v1.x.y` tag. `.github/workflows/release.yml` packages the DMG (signed +
notarized when the secrets below are configured), MSI, and DEB on native runners and
attaches them to the GitHub Release. The website's download links always point at the
latest release.

One-time setup — add these repository secrets (details in the workflow header):
`MACOS_CERT_P12`, `MACOS_CERT_PASSWORD`, `MACOS_SIGNING_IDENTITY`,
`NOTARIZATION_APPLE_ID`, `NOTARIZATION_PASSWORD`, `NOTARIZATION_TEAM_ID`.

## iOS — App Store

One-time setup:

1. **App Store Connect record** — appstoreconnect.apple.com → My Apps → **+** → New App
   (platform iOS, bundle ID `io.redigit.skimbyte.Skimbyte`, any SKU). The very first app
   on the account also asks for a permanent, publicly visible company/seller name.
2. **Signing** — set `iosTeamId=` in `local.properties`; the Gradle task
   `:shared:generateIosLocalXcconfig` feeds it to Xcode via the gitignored
   `Local.xcconfig`.

Per release:

1. In Xcode: select the `iosApp` scheme, destination **Any iOS Device (arm64)**, bump the
   version/build in `iosApp/Configuration/Config.xcconfig` (`MARKETING_VERSION`,
   `CURRENT_PROJECT_VERSION`), then **Product → Archive**.
2. In the Organizer: **Distribute App → App Store Connect → Upload** (automatic signing;
   Xcode creates the distribution certificate and profile).
3. In App Store Connect, once the build finishes processing (~15 min):
   - Fill the version page: description, keywords, screenshots (6.9" and 6.5" iPhone
     sizes required; iPad if the iPad checkbox is on), support URL, and the
     **privacy policy URL** (`…/docs/privacy/` on the project site — must be live).
   - **App Privacy** section: declare data collection — for Skimbyte that's
     "Email address + Name, linked to identity, for app functionality" (sign-in via
     the auth server), and bookmarks are tied to an account identifier. No tracking.
   - Sign-in-required apps must offer **account deletion** — Skimbyte has it
     (Account → Delete my data); point the reviewer to it in the review notes, and
     provide a demo account or note that Google/GitHub/Apple sign-in works.
4. **Submit for Review.** First review typically takes 1–3 days.

## Android — Play Store

One-time setup:

1. **Play Console account** — play.google.com/console, one-time $25 fee. New personal
   accounts must run a 14-day closed test with 12+ testers before production access —
   budget for this.
2. **Upload keystore** — the repo has no release signing config yet. Generate a keystore
   (`keytool -genkeypair -v -keystore skimbyte-upload.jks -alias upload -keyalg RSA
   -keysize 2048 -validity 10000`), keep it out of git, and wire a `signingConfigs.release`
   block in `androidApp/build.gradle.kts` reading the path/passwords from
   `local.properties` (same pattern as the other local config). Opt in to **Play App
   Signing** on first upload — Google holds the app signing key, the keystore above is
   only the upload key.
3. **Create the app** in Play Console and complete the setup checklist: app content
   declarations (privacy policy URL, data-safety form mirroring the App Privacy answers
   above, content rating questionnaire, ads declaration — none, target audience).

Per release:

1. Bump `versionCode`/`versionName` in `androidApp/build.gradle.kts`.
2. Build the bundle: `./gradlew :androidApp:bundleRelease` →
   `androidApp/build/outputs/bundle/release/androidApp-release.aab`.
3. Play Console → Testing/Production → **Create new release**, upload the `.aab`, write
   release notes, roll out.

## Website + docs

Deployed automatically from `main` by `.github/workflows/deploy-site.yml`: the landing
page (`website/`) at the site root, MkDocs under `/docs/`, Dokka at `/docs/api/`, and the
Wasm designsheet at `/docs/designsheet/`.
