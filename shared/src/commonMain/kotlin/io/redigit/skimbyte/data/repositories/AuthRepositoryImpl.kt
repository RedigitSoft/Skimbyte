package io.redigit.skimbyte.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.http.Url
import io.redigit.skimbyte.data.local.SessionStorage
import io.redigit.skimbyte.data.remote.AuthApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionStorage: SessionStorage,
    /** App-lifetime scope for the fire-and-forget server-side logout revocation. */
    private val scope: CoroutineScope,
    /** Every client whose Auth plugin caches bearer tokens — see [clearCachedTokens]. */
    private val httpClients: List<HttpClient>,
) : AuthRepository {

    private val _session = MutableStateFlow(sessionStorage.accessToken()?.let(::Session))
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.login(email, password)
        sessionStorage.save(response.accessToken, response.refreshToken)
        clearCachedTokens()
        _session.value = Session(response.accessToken)
    }

    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.register(email, password)
        sessionStorage.save(response.accessToken, response.refreshToken)
        clearCachedTokens()
        _session.value = Session(response.accessToken)
    }

    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = runCatching {
        val parameters = Url(redirectUri).parameters
        val accessToken = parameters["access_token"] ?: error("Missing access_token")
        val refreshToken = parameters["refresh_token"] ?: error("Missing refresh_token")
        sessionStorage.save(accessToken, refreshToken)
        clearCachedTokens()
        _session.value = Session(accessToken)
    }

    override fun logout() {
        // Local sign-out is instant and must never fail; the server-side revocation of the
        // refresh-token family is best-effort in the background — if it can't be reached,
        // the token ages out on its own instead of staying live for its full TTL.
        val refreshToken = sessionStorage.refreshToken()
        sessionStorage.clear()
        clearCachedTokens()
        _session.value = null
        if (refreshToken != null) {
            scope.launch { runCatching { authApi.logout(refreshToken) } }
        }
    }

    /**
     * Ktor's Auth plugin caches the tokens `loadTokens` returned and keeps attaching them until
     * a 401 forces a reload — so without this, requests after logout (or after a different user
     * signs in) still carry the previous user's not-yet-expired access token, and `/me` serves
     * the wrong profile. Cleared on every session transition so the next request re-reads
     * [sessionStorage].
     */
    private fun clearCachedTokens() {
        httpClients.forEach { it.authProvider<BearerAuthProvider>()?.clearToken() }
    }
}
