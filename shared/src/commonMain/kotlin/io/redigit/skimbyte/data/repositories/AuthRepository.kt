package io.redigit.skimbyte.data.repositories

import kotlinx.coroutines.flow.StateFlow

data class Session(val accessToken: String)

interface AuthRepository {
    val session: StateFlow<Session?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    /** [redirectUri] is the [AuthConfig.GOOGLE_REDIRECT_URI]-based URI (with `access_token`/
     * `refresh_token` query params) the auth server's Google OAuth callback redirected to. */
    suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit>
    fun logout()
}
