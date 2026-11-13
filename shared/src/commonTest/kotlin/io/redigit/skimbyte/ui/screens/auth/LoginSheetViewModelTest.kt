package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAuthRepository : AuthRepository {
    val sessionFlow = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = sessionFlow
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = Result.success(Unit)
    override fun logout() = Unit
}

class LoginSheetViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    private fun viewModel(
        repository: FakeAuthRepository = FakeAuthRepository(),
        redirectFailures: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    ) = LoginSheetViewModel(repository, redirectFailures)

    // The gated providers' contract is relative to their ApiConfig flag (GitHub is live today,
    // Apple parked): enabled → a real submission starts; disabled → an honest "coming soon".
    // Asserting against the flag keeps these green across future flips.

    @Test
    fun githubSignIn_matchesItsAvailabilityFlag() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(LoginSheetIntent.GithubSignInStarted)

        if (ApiConfig.GITHUB_SIGN_IN_ENABLED) {
            assertTrue(viewModel.state.value.isSubmitting)
            assertEquals(null, viewModel.state.value.error)
        } else {
            assertEquals("GitHub sign-in is coming soon", viewModel.state.value.error)
        }
    }

    @Test
    fun appleSignIn_matchesItsAvailabilityFlag() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(LoginSheetIntent.AppleSignInStarted)

        if (ApiConfig.APPLE_SIGN_IN_ENABLED) {
            assertTrue(viewModel.state.value.isSubmitting)
            assertEquals(null, viewModel.state.value.error)
        } else {
            assertEquals("Apple sign-in is coming soon", viewModel.state.value.error)
        }
    }

    @Test
    fun googleSignInStarted_setsSubmitting() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)

        assertTrue(viewModel.state.value.isSubmitting)
    }

    @Test
    fun sessionAppearing_resetsState_andEmitsLoginSucceeded() = runTest {
        // Sign-in completion is app-level (AuthRedirectHandler); the sheet reacts to the session
        // appearing — regardless of whether the sheet or a deep link initiated it.
        val repository = FakeAuthRepository()
        val viewModel = viewModel(repository)
        viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)
        val effects = mutableListOf<LoginSheetEffect>()
        val collectJob = launch { viewModel.effects.toList(effects) }
        yield()

        repository.sessionFlow.value = Session(accessToken = "a")
        yield()

        assertEquals(listOf<LoginSheetEffect>(LoginSheetEffect.LoginSucceeded), effects)
        assertEquals(LoginSheetState(), viewModel.state.value)
        collectJob.cancel()
    }

    @Test
    fun redirectFailure_setsError_andStopsSubmitting() = runTest {
        val redirectFailures = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val viewModel = viewModel(redirectFailures = redirectFailures)
        viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)

        redirectFailures.emit(Unit)
        yield()

        assertEquals(false, viewModel.state.value.isSubmitting)
        assertEquals("Google sign-in failed", viewModel.state.value.error)
    }

    @Test
    fun dismissed_resetsState() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(LoginSheetIntent.GithubSignInStarted) // sets an error

        viewModel.onIntent(LoginSheetIntent.Dismissed)

        assertEquals(LoginSheetState(), viewModel.state.value)
    }
}
