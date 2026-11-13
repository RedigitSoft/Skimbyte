# Skimbyte Privacy Policy

**Effective date: 2026-10-31**

> This page is the canonical, always-current copy of the policy. The same document is kept
> in the repository as `PRIVACY.md` so it ships and versions with the code.

Skimbyte is a daily, skimmable developer-news brief. It is built to collect the minimum
needed to show you the feed and, if you sign in, sync your bookmarks across devices.
**No analytics, no ads, no trackers, and your data is never sold or shared.**

## What we collect

### If you never sign in

Nothing. The feed is public and reading it requires no account. Your topic interests,
theme choice, and other preferences are stored **only on your device** and are never
uploaded.

### If you sign in (optional)

Signing in uses Redigit's authentication service, which holds your account data:

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
- Session tokens (sent only as `Authorization` headers to the Skimbyte and auth servers)

## What we don't do

- No analytics or telemetry SDKs — verifiable in the open-source code
  (`gradle/libs.versions.toml` lists every dependency).
- No advertising and no ad identifiers.
- No selling, renting, or sharing of personal data with third parties.

## Transport security

All communication with the Skimbyte and auth servers uses HTTPS.

## Deleting your data

In the app, open your **Account** screen and choose **Delete my data**. This immediately and
permanently deletes everything our servers store about you for Skimbyte — your bookmarks and
any avatar or banner images you uploaded — and signs you out. A minimal record that you
requested the deletion is kept. If you sign in again later, Skimbyte starts fresh for you.

## Changes to this policy

Changes are published to the hosted copy of this page and recorded in this repository's git
history. Material changes will be noted in release notes.

## Contact

Questions or requests: **akshay@redigit.io**
