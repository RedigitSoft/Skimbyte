package io.redigit.skimbyte.util

import io.redigit.skimbyte.data.remote.BackendConfig

// The deep link is captured by MainActivity's intent-filter (manifestPlaceholders, sourced from
// local.properties) and forwarded to GoogleAuthCallback from onNewIntent.
actual fun authRedirectUri(): String = BackendConfig.GOOGLE_REDIRECT_URI
