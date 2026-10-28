package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

private class FakeAuthRepository(
    private val googleResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun completeGoogleSignIn(redirectUri: String): Result<Unit> = googleResult
    override fun logout() = Unit
}

class LoginSheetViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() = Dispatchers.resetMain()

    @Test
    fun githubSignIn_showsComingSoonMessage() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository())

        viewModel.onIntent(LoginSheetIntent.GithubSignInStarted)

        assertEquals("GitHub sign-in is coming soon", viewModel.state.value.error)
    }

    @Test
    fun appleSignIn_showsComingSoonMessage() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository())

        viewModel.onIntent(LoginSheetIntent.AppleSignInStarted)

        assertEquals("Apple sign-in is coming soon", viewModel.state.value.error)
    }

    @Test
    fun googleSignInStarted_setsSubmitting() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository())

        viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)

        assertTrue(viewModel.state.value.isSubmitting)
    }

    @Test
    fun googleRedirect_success_resetsState_andEmitsLoginSucceeded() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
        val effects = mutableListOf<LoginSheetEffect>()
        val collectJob = launch { viewModel.effects.toList(effects) }
        yield()

        viewModel.onIntent(LoginSheetIntent.GoogleRedirect("skimbyte://auth/callback?access_token=a&refresh_token=b"))
        yield()

        assertEquals(listOf<LoginSheetEffect>(LoginSheetEffect.LoginSucceeded), effects)
        assertEquals(LoginSheetState(), viewModel.state.value)
        collectJob.cancel()
    }

    @Test
    fun googleRedirect_failure_setsError_andStopsSubmitting() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException("boom"))))
        viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)

        viewModel.onIntent(LoginSheetIntent.GoogleRedirect("skimbyte://auth/callback"))

        assertEquals(false, viewModel.state.value.isSubmitting)
        assertEquals("Google sign-in failed", viewModel.state.value.error)
    }

    @Test
    fun dismissed_resetsState() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository())
        viewModel.onIntent(LoginSheetIntent.GithubSignInStarted) // sets an error

        viewModel.onIntent(LoginSheetIntent.Dismissed)

        assertEquals(LoginSheetState(), viewModel.state.value)
    }
}
