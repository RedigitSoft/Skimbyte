package io.redigit.skimbyte.auth

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

    override fun logout() {
        sessionStorage.clear()
        _session.value = null
    }
}
