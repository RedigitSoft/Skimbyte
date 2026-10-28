# Security Policy

## Supported versions

Skimbyte is a reference project under active development; only the latest
commit on `main` is supported.

## Reporting a vulnerability

Please **do not** open a public issue for security problems.

Report vulnerabilities privately via
[GitHub Security Advisories](../../security/advisories/new) or by emailing
**akshay@redigit.io**. Include steps to reproduce and the affected
platform(s) (Android / iOS / Desktop / Web).

You can expect an acknowledgement within a week. Fixes land on `main` as
regular commits once verified.

## Scope

This repository contains the client app only. The backend API it talks to is
closed-source; issues that are purely server-side are out of scope here, but
reports about how the client handles server data (auth tokens, session
storage, TLS) are very welcome.