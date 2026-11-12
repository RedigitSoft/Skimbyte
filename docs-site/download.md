# Download

Skimbyte is one Kotlin Multiplatform + Compose Multiplatform codebase, so the same app
runs natively everywhere. Grab it for your platform:

## Desktop

All desktop installers are built and published by
[GitHub Actions](https://github.com/redigitsoft/Skimbyte/blob/main/.github/workflows/release.yml)
on every release — download them from the
**[latest release](https://github.com/redigitsoft/Skimbyte/releases/latest)**.

| Platform | File | Notes |
|---|---|---|
| macOS (Apple Silicon) | `Skimbyte-x.y.z-arm64.dmg` | Signed and notarized — opens without warnings |
| Windows (x64) | `Skimbyte-x.y.z-x86_64.msi` | Unsigned for now: SmartScreen will warn — choose "More info" → "Run anyway" |
| Linux (x64, Debian/Ubuntu) | `skimbyte_x.y.z_amd64.deb` | Install with `sudo apt install ./skimbyte_x.y.z_amd64.deb` |

## Mobile

- **iOS** — App Store release in progress.
- **Android** — Play Store release in progress.

## Web

The [live designsheet](designsheet/) (the app's component catalog) runs in your browser
via Kotlin/Wasm today; a hosted build of the full web app is planned.

## Run from source

Every target builds from a fresh clone with no setup — the app starts in demo mode on
bundled sample data:

```sh
git clone https://github.com/redigitsoft/Skimbyte.git
cd Skimbyte
./gradlew :desktopApp:run
```

See the [README](https://github.com/redigitsoft/Skimbyte#readme) for the Android, iOS,
and web run commands.
