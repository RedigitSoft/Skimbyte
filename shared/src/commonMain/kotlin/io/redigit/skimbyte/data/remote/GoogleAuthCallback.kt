package io.redigit.skimbyte.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges the platform entry point (e.g. Android's `MainActivity.onNewIntent`, which captures the
 * [AuthConfig.GOOGLE_REDIRECT_URI] redirect the auth server sends after Google OAuth completes)
 * into the Compose layer, which has no direct access to that entry point.
 */
object GoogleAuthCallback {
    private val _redirects = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val redirects: SharedFlow<String> = _redirects.asSharedFlow()

    fun onRedirect(uri: String) {
        _redirects.tryEmit(uri)
    }
}
