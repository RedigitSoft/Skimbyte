package io.redigit.skimbyte.auth

import io.redigit.skimbyte.data.local.SessionStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The app runs two independent Ktor `HttpClient`s against [SessionStorage] — the general one in
 * `networkModule` and the dedicated one in `authModule` — each with its own `Auth` plugin, so
 * each only single-flights refreshes against itself. The server's refresh token is single-use,
 * so without cross-client coordination, concurrent 401s on both clients can each present the
 * same stored refresh token; the second submission gets flagged as replay and the whole session
 * is revoked. This mutex serializes refresh attempts across every client sharing it (wire the
 * same Koin-provided instance into both), and a caller that loses the race is handed whatever
 * the winner already stored instead of replaying a token that's already been consumed.
 */
class RefreshCoordinator(private val sessionStorage: SessionStorage) {
    private val mutex = Mutex()

    suspend fun refresh(presentedRefreshToken: String, rotate: suspend (String) -> Pair<String, String>?): Pair<String, String>? =
        mutex.withLock {
            val storedRefreshToken = sessionStorage.refreshToken()
            if (storedRefreshToken != null && storedRefreshToken != presentedRefreshToken) {
                sessionStorage.accessToken()?.let { return@withLock it to storedRefreshToken }
            }
            val rotated = rotate(presentedRefreshToken) ?: return@withLock null
            sessionStorage.save(rotated.first, rotated.second)
            rotated
        }
}
