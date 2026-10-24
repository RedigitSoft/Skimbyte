package io.redigit.skimbyte.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges the platform entry point (e.g. Android's `MainActivity.onNewIntent`, which captures the
 * [AuthConfig.GOOGLE_REDIRECT_URI] redirect the auth server sends after OAuth completes) into the
 * Compose layer, which has no direct access to that entry point.
 *
 * This is the single OAuth redirect path, shared by every provider: Google and GitHub both redirect
 * back to the same mobile deep link carrying `access_token`/`refresh_token`, so the token handling is
 * provider-agnostic (the provider only differs in the `/start` URL, see `LoginSheet`).
 */
object GoogleAuthCallback {
    private val _redirects = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val redirects: SharedFlow<String> = _redirects.asSharedFlow()

    fun onRedirect(uri: String) {
        _redirects.tryEmit(uri)
    }
}
