package io.redigit.skimbyte.auth

import kotlinx.coroutines.flow.StateFlow

data class Session(val accessToken: String)

interface AuthRepository {
    val session: StateFlow<Session?>
    suspend fun login(email: String, password: String): Result<Unit>
    fun logout()
}
