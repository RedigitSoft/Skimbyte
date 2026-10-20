# Profile images (avatar + banner) — Skimbyte data layer

## Intent and why

the auth server (auth-server) is gaining `avatar_url`/`banner_url` on `GET /me` and an
upload endpoint (see the matching design doc in the `auth-server` repo). This
doc covers the Skimbyte side: fetching that profile data when a user is
logged in and making it available to the rest of the app, with no new UI yet.
Skimbyte already has an account/settings screen (`AccountScreen.kt`); wiring
the fetched data into it is explicitly out of scope for this change.

## Goals

- When a session exists (`AuthRepository.session` is non-null), fetch the
  user's profile (`GET /me`) and expose it as app state.
- Resolve the raw bucket-relative `avatarUrl`/`bannerUrl` paths the auth server
  returns into full, displayable CDN URLs.

## Non-goals

- No new screen or UI wiring — `AccountScreen` stays as-is.
- No upload UI / image picker — the auth server's upload endpoint exists but nothing
  in Skimbyte calls it yet.
- No auto-refresh of profile data on a timer or app-foreground — fetched once
  per session becoming available.

## Anti-goals

- Don't special-case avatar/banner URL resolution per source (Google-seeded
  vs user-uploaded) — the auth server always returns a bare path; Skimbyte always
  resolves it the same way.
- Don't reuse `ApiConfig.ASSET_BASE_URL`/`resolveImageUrl` for this — that
  pair is specifically skimbyte-server's own asset base (already used for
  post images) and points at a different host than the Spaces CDN.

## Constraints

- CDN domain: `https://cdn.example.com` — a custom subdomain in front of
  DigitalOcean Spaces' own CDN (not a reverse proxy; DNS CNAME + DO-managed
  TLS). Add `ApiConfig.SPACES_CDN_BASE_URL`, sourced the same way as
  `AUTH_BASE_URL`/`BASE_URL` (generated `BackendConfig`, via
  `local.properties` / `generateAuthConfig`-style Gradle task), defaulted to
  this value. DNS/CDN setup itself is out of scope here (DigitalOcean
  dashboard + DNS, not app code).
- `/me` already exists in the auth server today and is authenticated; the existing
  `authHttpClient` (in `AuthModule.kt`) already attaches the bearer token
  automatically via Ktor's `Auth` plugin — no new auth-wiring needed, just
  use that client.
- the auth server's rollout of the `avatarUrl`/`bannerUrl` fields may not have
  shipped yet when this lands. The `me()` fetch must fail soft: any error
  (network, deserialization, 404/old-shape response) is caught, logged, and
  leaves the profile `StateFlow` at `null` rather than throwing — this is
  what lets the two repos be built, reviewed, and deployed independently
  rather than as one atomic unit.

## Decided design

**Config** — add `SPACES_CDN_BASE_URL` to `ApiConfig.kt` alongside
`AUTH_BASE_URL`, sourced from `BackendConfig` the same way, blank by default.

**DTO** — extend whatever DTO backs `GET /me` (new, since `AuthApi` doesn't
call `/me` today) with `avatarUrl: String?` and `bannerUrl: String?`,
mirroring the auth server's `MeResponse` shape (`userId`, `tenantId`, `sessionId`,
`avatarUrl`, `bannerUrl`).

**API call** — add a `me()` method to `AuthApi`/`KtorAuthApi` (`GET
${ApiConfig.AUTH_BASE_URL}/me`) using the existing injected `authHttpClient`
qualifier, same pattern as `login`/`register`/`refresh`.

**URL resolution** — a new resolver parallel to `ApiConfig.resolveImageUrl`,
prefixing with `SPACES_CDN_BASE_URL` instead of `ASSET_BASE_URL`, applied to
`avatarUrl`/`bannerUrl` when mapping the DTO into a domain model.

**Data layer** — a `Profile` domain model (resolved, display-ready URLs) and
a repository holding `StateFlow<Profile?>`, populated by calling `me()`
whenever `AuthRepository.session` transitions to non-null (covers both fresh
login and app start with an already-stored session) and cleared on logout.
Wired into `authModule` via Koin, following the existing
`single`/`viewModel` registration style in that file.

## What's left to the builder

- Exact class/interface names and file placement (e.g. `ProfileApi` +
  `ProfileRepository` as new files under `auth/`, vs. folding `me()` /
  profile state directly into the existing `AuthApi`/`AuthRepository`) —
  pick whichever keeps `AuthRepository` focused on session lifecycle rather
  than growing it into a general user-data repository.
- Whether the profile fetch is triggered by the repository observing
  `AuthRepository.session` itself, or by `AuthRepositoryImpl` calling out
  after a successful login/session restore — pick whichever avoids a
  circular dependency between the two.
- Test coverage: DTO (de)serialization, URL-resolution correctness (bare path
  vs already-absolute), and repository behavior (fetch-on-session, clear-on-
  logout), following this module's existing test conventions.
- No `@Preview`/Compose UI test obligations apply here (CLAUDE.md's
  non-negotiables are about composables; this change adds no composables).