package io.redigit.skimbyte.util

/**
 * The OAuth redirect URI this platform can actually receive, passed to the auth server's
 * `/start` endpoint (which validates it against its exact-match allowlist).
 *
 * Mobile platforms return the custom-scheme deep link from local.properties
 * (`BackendConfig.GOOGLE_REDIRECT_URI`): the OS routes it back into the app. A desktop JVM
 * process can't register a URL scheme with the OS, so its actual uses the RFC 8252 loopback
 * pattern instead — a localhost HTTP listener started on first use, whose URL the system
 * browser can always reach.
 */
expect fun authRedirectUri(): String
