package io.redigit.skimbyte.auth

import io.redigit.skimbyte.auth.dto.LoginResponseDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSessionStorage(initialAccessToken: String? = null) : SessionStorage {
    private var accessToken: String? = initialAccessToken
    private var refreshToken: String? = null

    override fun accessToken(): String? = accessToken
    override fun refreshToken(): String? = refreshToken
    override fun save(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }
    override fun clear() {
        accessToken = null
        refreshToken = null
    }
}

private class FakeAuthApi(private val result: Result<LoginResponseDto>) : AuthApi {
    override suspend fun login(email: String, password: String): LoginResponseDto = result.getOrThrow()
    override suspend fun refresh(refreshToken: String): LoginResponseDto = result.getOrThrow()
}

class AuthRepositoryImplTest {

    @Test
    fun login_success_persistsSession_andUpdatesSessionFlow() = runTest {
        val sessionStorage = FakeSessionStorage()
        val authApi = FakeAuthApi(Result.success(LoginResponseDto("access-1", "refresh-1", 3600)))
        val repository = AuthRepositoryImpl(authApi, sessionStorage)

        val result = repository.login("demo@skimbyte.app", "demo")

        assertTrue(result.isSuccess)
        assertEquals("access-1", sessionStorage.accessToken())
        assertEquals("access-1", repository.session.value?.accessToken)
    }

    @Test
    fun login_failure_returnsFailure_andLeavesSessionNull() = runTest {
        val sessionStorage = FakeSessionStorage()
        val authApi = FakeAuthApi(Result.failure(RuntimeException("invalid credentials")))
        val repository = AuthRepositoryImpl(authApi, sessionStorage)

        val result = repository.login("wrong@skimbyte.app", "wrong")

        assertTrue(result.isFailure)
        assertNull(repository.session.value)
        assertNull(sessionStorage.accessToken())
    }

    @Test
    fun existingPersistedToken_isReflectedAsSessionAtConstruction() {
        val sessionStorage = FakeSessionStorage(initialAccessToken = "already-logged-in")
        val repository = AuthRepositoryImpl(FakeAuthApi(Result.failure(RuntimeException())), sessionStorage)

        assertEquals("already-logged-in", repository.session.value?.accessToken)
    }

    @Test
    fun logout_clearsSessionStorage_andSessionFlow() {
        val sessionStorage = FakeSessionStorage(initialAccessToken = "already-logged-in")
        val repository = AuthRepositoryImpl(FakeAuthApi(Result.failure(RuntimeException())), sessionStorage)

        repository.logout()

        assertNull(repository.session.value)
        assertNull(sessionStorage.accessToken())
    }
}
