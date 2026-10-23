package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.local.SessionStorage
import io.redigit.skimbyte.data.models.dto.LoginResponseDto
import io.redigit.skimbyte.data.models.dto.MeResponseDto
import io.redigit.skimbyte.data.remote.AuthApi
import io.redigit.skimbyte.util.PickedImage
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
    override suspend fun register(email: String, password: String): LoginResponseDto = result.getOrThrow()
    override suspend fun refresh(refreshToken: String): LoginResponseDto = result.getOrThrow()
    override suspend fun me(): MeResponseDto = error("unused")
    override suspend fun uploadImage(type: String, image: PickedImage) = error("unused")
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
    fun register_success_persistsSession_andUpdatesSessionFlow() = runTest {
        val sessionStorage = FakeSessionStorage()
        val authApi = FakeAuthApi(Result.success(LoginResponseDto("access-2", "refresh-2", 3600)))
        val repository = AuthRepositoryImpl(authApi, sessionStorage)

        val result = repository.register("new@skimbyte.app", "newpass")

        assertTrue(result.isSuccess)
        assertEquals("access-2", sessionStorage.accessToken())
        assertEquals("access-2", repository.session.value?.accessToken)
    }

    @Test
    fun completeGoogleSignIn_parsesTokensFromRedirectUri_andPersistsSession() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = AuthRepositoryImpl(FakeAuthApi(Result.failure(RuntimeException())), sessionStorage)

        val result = repository.completeGoogleSignIn(
            "skimbyte://auth/callback?access_token=google-access&refresh_token=google-refresh&token_type=Bearer",
        )

        assertTrue(result.isSuccess)
        assertEquals("google-access", sessionStorage.accessToken())
        assertEquals("google-refresh", sessionStorage.refreshToken())
        assertEquals("google-access", repository.session.value?.accessToken)
    }

    @Test
    fun completeGoogleSignIn_missingTokenInRedirect_returnsFailure() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = AuthRepositoryImpl(FakeAuthApi(Result.failure(RuntimeException())), sessionStorage)

        val result = repository.completeGoogleSignIn("skimbyte://auth/callback?access_token=only-access")

        assertTrue(result.isFailure)
        assertNull(repository.session.value)
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
