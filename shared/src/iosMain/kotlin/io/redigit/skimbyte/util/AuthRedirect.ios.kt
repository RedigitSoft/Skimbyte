package io.redigit.skimbyte.util

import io.redigit.skimbyte.data.remote.BackendConfig

// The deep link's custom scheme must be registered in iosApp's Info.plist (CFBundleURLTypes)
// and forwarded to GoogleAuthCallback from the SwiftUI entry point's onOpenURL.
actual fun authRedirectUri(): String = BackendConfig.GOOGLE_REDIRECT_URI
