# Skimbyte Privacy Policy

**Effective date: 2026-10-31**

> The canonical, always-current copy of this policy is the hosted version on the project
> website (`/privacy/`). This file is the same document, kept in the repository so it ships
> and versions with the code.

Skimbyte is a daily, skimmable developer-news brief. It is built to collect the minimum
needed to show you the feed and, if you sign in, sync your bookmarks across devices.
**No analytics, no ads, no trackers, and your data is never sold or shared.**

## What we collect

### If you never sign in

Nothing. The feed is public and reading it requires no account. Your topic interests,
theme choice, and other preferences are stored **only on your device** and are never
uploaded.

### If you sign in (optional)

Signing in uses **the auth server**, Redigit's shared authentication service.
the auth server holds your account data:

- Email address and display name. When you sign in with Google or GitHub, only your name
  and email are shared by the provider — never your contacts, repositories, or anything else.
- Optional avatar and banner images, if you upload them. These are stored on a
  DigitalOcean Spaces CDN.

The Skimbyte server itself stores exactly one kind of per-user data:

- **Bookmarks** — the id of each post you save plus a timestamp, keyed to a pseudonymous
  user id (the subject claim of your sign-in token). Nothing else about you is stored there.

### Always on your device, never uploaded

- Followed topics / interests
- Theme and display preferences
- Session tokens (sent only as `Authorization` headers to the Skimbyte and the auth server servers)

## What we don't do

- No analytics or telemetry SDKs — verifiable in the open-source code
  (`gradle/libs.versions.toml` lists every dependency).
- No advertising and no ad identifiers.
- No selling, renting, or sharing of personal data with third parties.

## Transport security

All communication with the Skimbyte and the auth server servers uses HTTPS.

## Deleting your data

In the app, open your **Account** screen and choose **Delete my data**. This immediately and
permanently deletes everything the Skimbyte server stores about you (your bookmarks) and
signs you out.

The sign-in account itself (email, display name, profile images) is shared across
Redigit apps and is not removed by this flow. To delete it entirely, email
**akshay@redigit.io** and it will be removed.

## Changes to this policy

Changes are published to the hosted copy of this page and recorded in this repository's git
history. Material changes will be noted in release notes.

## Contact

Questions or requests: **akshay@redigit.io**
