package io.redigit.skimbyte.data.repositories

import io.redigit.skimbyte.data.remote.GoogleAuthCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingAuthRepository(
    private val googleResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    val completedRedirects = mutableListOf<String>()
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> {
        completedRedirects += redirectUri
        return googleResult
    }
    override fun logout() = Unit
}

class AuthRedirectHandlerTest {

    @AfterTest
    fun clearReplayedRedirect() {
        GoogleAuthCallback.consume() // don't leak a replayed redirect into other tests
    }

    @Test
    fun completesARedirect_thatArrivedBeforeCollectionStarted() = runTest {
        // The cold-start case: Android delivers the deep link in MainActivity.onCreate, before
        // App's composition launches the handler. GoogleAuthCallback's replay must bridge the gap.
        val repository = RecordingAuthRepository()
        GoogleAuthCallback.onRedirect("skimbyte://auth/callback?access_token=a&refresh_token=b")

        val job = launch { AuthRedirectHandler(repository).run() }
        yield()

        assertEquals(listOf("skimbyte://auth/callback?access_token=a&refresh_token=b"), repository.completedRedirects)
        job.cancel()
    }

    @Test
    fun completesARedirect_thatArrivesWhileCollecting() = runTest {
        val repository = RecordingAuthRepository()
        val job = launch { AuthRedirectHandler(repository).run() }
        yield()

        GoogleAuthCallback.onRedirect("skimbyte://auth/callback?access_token=a&refresh_token=b")
        yield()

        assertEquals(1, repository.completedRedirects.size)
        job.cancel()
    }

    @Test
    fun aHandledRedirect_isNotRedeliveredToLaterCollectors() = runTest {
        // Guards the logout case: without consume(), the replayed redirect would re-complete
        // sign-in for any collector that starts later.
        val first = RecordingAuthRepository()
        GoogleAuthCallback.onRedirect("skimbyte://auth/callback?access_token=a")
        val firstJob = launch { AuthRedirectHandler(first).run() }
        yield()
        firstJob.cancel()

        val second = RecordingAuthRepository()
        val secondJob = launch { AuthRedirectHandler(second).run() }
        yield()

        assertEquals(1, first.completedRedirects.size)
        assertTrue(second.completedRedirects.isEmpty())
        secondJob.cancel()
    }

    @Test
    fun failure_emitsOntoFailures() = runTest {
        val repository = RecordingAuthRepository(Result.failure(RuntimeException("boom")))
        val handler = AuthRedirectHandler(repository)
        val failures = mutableListOf<Unit>()
        val failuresJob = launch { handler.failures.toList(failures) }
        val handlerJob = launch { handler.run() }
        yield()

        GoogleAuthCallback.onRedirect("skimbyte://auth/callback")
        // Two hops: the redirect resumes the handler, whose failure emission then resumes the
        // failures collector — each needs its own pass of the test scheduler.
        yield()
        yield()

        assertEquals(1, failures.size)
        handlerJob.cancel()
        failuresJob.cancel()
    }
}
