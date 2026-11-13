package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.remote.GoogleAuthCallback
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * App-level consumer of the OAuth redirect deep link ([GoogleAuthCallback]). [run] is launched once
 * at the root of `App` and lives as long as the app, so a redirect completes sign-in no matter
 * which screen is visible. The normal flow returns while the login sheet is open — but a sign-in
 * deep link can equally arrive with no sheet showing, even cold-starting the app, which is why the
 * sheet can't own this collection.
 *
 * Success needs no event of its own: completing sign-in publishes [AuthRepository.session], which
 * the login sheet (and everything else) already observes.
 */
class AuthRedirectHandler(private val authRepository: AuthRepository) {

    private val _failures = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** A redirect arrived but completing sign-in failed — the login sheet shows its error off this. */
    val failures: SharedFlow<Unit> = _failures.asSharedFlow()

    /** Collects forever; launch once from an app-lifetime scope. */
    suspend fun run() {
        GoogleAuthCallback.redirects.collect { uri ->
            // Consume before completing: once picked up, the redirect must never re-fire, even if
            // this collection is cancelled and restarted mid-flight.
            GoogleAuthCallback.consume()
            if (authRepository.completeGoogleSignIn(uri).isFailure) {
                _failures.tryEmit(Unit)
            }
        }
    }
}
