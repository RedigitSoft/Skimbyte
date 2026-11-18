package io.redigit.skimbyte.util

/**
 * Opens the user's email composer addressed to [address]. Kept separate from [openUrl] because the
 * `mailto:` scheme needs a different mechanism per platform — Android's Custom Tabs and the JVM's
 * `Desktop.browse` only handle web URLs, so routing a `mailto:` through [openUrl] would silently
 * fail on exactly the platforms this app ships to a store.
 */
expect fun openEmail(address: String)