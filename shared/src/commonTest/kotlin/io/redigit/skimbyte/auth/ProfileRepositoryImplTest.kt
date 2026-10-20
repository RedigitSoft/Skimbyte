package io.redigit.skimbyte.auth

import io.redigit.skimbyte.auth.dto.MeResponseDto
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeAuthApiForProfile(private val result: Result<MeResponseDto>) : AuthApi {
    override suspend fun login(email: String, password: String) = error("unused")
    override suspend fun register(email: String, password: String) = error("unused")
    override suspend fun refresh(refreshToken: String) = error("unused")
    override suspend fun me(): MeResponseDto = result.getOrThrow()
}

class ProfileRepositoryImplTest {

    @Test
    fun session_becoming_nonNull_fetches_and_populates_profile() = runTest(UnconfinedTestDispatcher()) {
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.success(MeResponseDto("u1", avatarUrl = "avatars/u1/a.jpg"))),
            sessionFlow,
            backgroundScope,
        )

        sessionFlow.value = Session("token")
        advanceUntilIdle()

        assertEquals("u1", repo.profile.value?.userId)
    }

    @Test
    fun session_becoming_null_clears_profile() = runTest(UnconfinedTestDispatcher()) {
        val sessionFlow = MutableStateFlow<Session?>(Session("token"))
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.success(MeResponseDto("u1"))),
            sessionFlow,
            backgroundScope,
        )
        advanceUntilIdle()

        sessionFlow.value = null
        advanceUntilIdle()

        assertNull(repo.profile.value)
    }

    @Test
    fun fetch_failure_leaves_profile_null_and_does_not_throw() = runTest(UnconfinedTestDispatcher()) {
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(
            FakeAuthApiForProfile(Result.failure(RuntimeException("404"))),
            sessionFlow,
            backgroundScope,
        )

        sessionFlow.value = Session("token")
        advanceUntilIdle()

        assertNull(repo.profile.value)
    }

    @Test
    fun second_login_after_logout_reflects_the_latest_session_profile() = runTest(UnconfinedTestDispatcher()) {
        var call = 0
        val api = object : AuthApi {
            override suspend fun login(email: String, password: String) = error("unused")
            override suspend fun register(email: String, password: String) = error("unused")
            override suspend fun refresh(refreshToken: String) = error("unused")
            override suspend fun me(): MeResponseDto {
                call++
                return MeResponseDto("u$call")
            }
        }
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)

        sessionFlow.value = Session("token-1")
        advanceUntilIdle()
        sessionFlow.value = null
        advanceUntilIdle()
        sessionFlow.value = Session("token-2")
        advanceUntilIdle()

        assertEquals("u2", repo.profile.value?.userId)
    }

    @Test
    fun logout_while_fetch_in_flight_leaves_profile_null() = runTest(UnconfinedTestDispatcher()) {
        val gate = CompletableDeferred<MeResponseDto>()
        val api = object : AuthApi {
            override suspend fun login(email: String, password: String) = error("unused")
            override suspend fun register(email: String, password: String) = error("unused")
            override suspend fun refresh(refreshToken: String) = error("unused")
            override suspend fun me(): MeResponseDto = gate.await()
        }
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)

        sessionFlow.value = Session("token")
        runCurrent() // me() is now suspended on gate.await()

        sessionFlow.value = null // logout while the fetch is still in flight
        advanceUntilIdle()

        assertNull(repo.profile.value)
    }

    @Test
    fun second_login_while_first_fetch_still_in_flight_reflects_the_second() = runTest(UnconfinedTestDispatcher()) {
        val gate1 = CompletableDeferred<MeResponseDto>()
        var callCount = 0
        val api = object : AuthApi {
            override suspend fun login(email: String, password: String) = error("unused")
            override suspend fun register(email: String, password: String) = error("unused")
            override suspend fun refresh(refreshToken: String) = error("unused")
            override suspend fun me(): MeResponseDto {
                callCount++
                // Only the first call waits on gate1; the second resolves immediately, so
                // completing gate1 later exercises only the (should-be-cancelled) first fetch.
                return if (callCount == 1) gate1.await() else MeResponseDto("u2")
            }
        }
        val sessionFlow = MutableStateFlow<Session?>(null)
        val repo = ProfileRepositoryImpl(api, sessionFlow, backgroundScope)

        sessionFlow.value = Session("token-1")
        runCurrent() // first me() is now suspended on gate1.await()

        sessionFlow.value = Session("token-2") // second login arrives before the first resolves
        advanceUntilIdle()

        gate1.complete(MeResponseDto("u1")) // first fetch's deferred resolves after the fact
        advanceUntilIdle()

        assertEquals("u2", repo.profile.value?.userId)
    }
}
