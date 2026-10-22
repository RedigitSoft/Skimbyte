package io.redigit.skimbyte.auth

import io.ktor.http.Url
import io.redigit.skimbyte.data.local.SessionStorage
import io.redigit.skimbyte.data.remote.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionStorage: SessionStorage,
) : AuthRepository {

    private val _session = MutableStateFlow(sessionStorage.accessToken()?.let(::Session))
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.login(email, password)
        sessionStorage.save(response.accessToken, response.refreshToken)
        _session.value = Session(response.accessToken)
    }

    override suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.register(email, password)
        sessionStorage.save(response.accessToken, response.refreshToken)
        _session.value = Session(response.accessToken)
    }

    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = runCatching {
        val parameters = Url(redirectUri).parameters
        val accessToken = parameters["access_token"] ?: error("Missing access_token")
        val refreshToken = parameters["refresh_token"] ?: error("Missing refresh_token")
        sessionStorage.save(accessToken, refreshToken)
        _session.value = Session(accessToken)
    }

    override fun logout() {
        sessionStorage.clear()
        _session.value = null
    }
}
