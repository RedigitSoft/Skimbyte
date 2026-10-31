package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.remote.MeApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeMeApi(private val error: Throwable? = null) : MeApi {
    var callCount = 0
    override suspend fun deleteUserData() {
        callCount++
        error?.let { throw it }
    }
}

class UserDataRepositoryImplTest {

    @Test
    fun deleteAll_success_callsTheServerOnce() = runTest {
        val meApi = FakeMeApi()
        val repository = UserDataRepositoryImpl(meApi)

        val result = repository.deleteAll()

        assertTrue(result.isSuccess)
        assertEquals(1, meApi.callCount)
    }

    @Test
    fun deleteAll_wrapsServerFailureInResult() = runTest {
        val repository = UserDataRepositoryImpl(FakeMeApi(error = RuntimeException("server down")))

        val result = repository.deleteAll()

        assertTrue(result.isFailure)
    }
}
