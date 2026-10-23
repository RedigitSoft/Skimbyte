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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeAuthRepository(
    private val loginResult: Result<Unit>,
    private val registerResult: Result<Unit> = loginResult,
    private val googleResult: Result<Unit> = loginResult,
) : AuthRepository {
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override suspend fun login(email: String, password: String): Result<Unit> = loginResult
    override suspend fun register(email: String, password: String): Result<Unit> = registerResult
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
    fun onSubmit_blankFields_isNoOp() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))

        viewModel.onIntent(LoginSheetIntent.Submit)

        assertEquals(false, viewModel.state.value.isSubmitting)
    }

    @Test
    fun onSubmit_success_resetsState_andEmitsLoginSucceeded() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
        viewModel.onIntent(LoginSheetIntent.EmailChanged("demo@skimbyte.app"))
        viewModel.onIntent(LoginSheetIntent.PasswordChanged("demo"))
        val effects = mutableListOf<LoginSheetEffect>()
        val collectJob = launch { viewModel.effects.toList(effects) }
        // Dispatchers.Main (set below) and this test's own coroutine scope are backed by two
        // distinct UnconfinedTestDispatcher instances, so a yield() is needed on each side of the
        // rendezvous: once to let the collector above actually subscribe before we emit, and once
        // to let the emitted value actually reach the collector's buffer before we assert on it.
        yield()

        viewModel.onIntent(LoginSheetIntent.Submit)
        yield()

        assertEquals(listOf<LoginSheetEffect>(LoginSheetEffect.LoginSucceeded), effects)
        assertEquals(LoginSheetState(), viewModel.state.value)
        collectJob.cancel()
    }

    @Test
    fun onSubmit_failure_setsError_andStopsSubmitting() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException("invalid"))))
        viewModel.onIntent(LoginSheetIntent.EmailChanged("wrong@skimbyte.app"))
        viewModel.onIntent(LoginSheetIntent.PasswordChanged("wrong"))

        viewModel.onIntent(LoginSheetIntent.Submit)

        assertEquals(false, viewModel.state.value.isSubmitting)
        assertTrue(viewModel.state.value.error != null)
    }

    @Test
    fun onToggleMode_switchesToRegisterMode() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))

        viewModel.onIntent(LoginSheetIntent.ModeToggled)

        assertTrue(viewModel.state.value.isRegisterMode)
    }

    @Test
    fun onSubmit_inRegisterMode_callsRegister_notLogin() = runTest {
        var registerCalled = false
        val repository = object : AuthRepository {
            override val session: StateFlow<Session?> = MutableStateFlow(null)
            override suspend fun login(email: String, password: String) = Result.failure<Unit>(RuntimeException("login should not be called"))
            override suspend fun register(email: String, password: String): Result<Unit> {
                registerCalled = true
                return Result.success(Unit)
            }
            override suspend fun completeGoogleSignIn(redirectUri: String) = Result.success(Unit)
            override fun logout() = Unit
        }
        val viewModel = LoginSheetViewModel(repository)
        viewModel.onIntent(LoginSheetIntent.ModeToggled)
        viewModel.onIntent(LoginSheetIntent.EmailChanged("new@skimbyte.app"))
        viewModel.onIntent(LoginSheetIntent.PasswordChanged("newpass"))

        viewModel.onIntent(LoginSheetIntent.Submit)

        assertTrue(registerCalled)
    }

    @Test
    fun onGoogleRedirect_success_resetsState_andEmitsLoginSucceeded() = runTest {
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
    fun onEmailChanged_clearsExistingError() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException())))
        viewModel.onIntent(LoginSheetIntent.EmailChanged("a"))
        viewModel.onIntent(LoginSheetIntent.PasswordChanged("b"))
        viewModel.onIntent(LoginSheetIntent.Submit)
        assertTrue(viewModel.state.value.error != null)

        viewModel.onIntent(LoginSheetIntent.EmailChanged("new@skimbyte.app"))

        assertNull(viewModel.state.value.error)
    }
}
