package io.redigit.skimbyte.auth.ui

import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        var succeeded = false

        viewModel.onSubmit { succeeded = true }

        assertTrue(!succeeded)
        assertEquals(false, viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun onSubmit_success_resetsState_andInvokesCallback() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
        viewModel.onEmailChanged("demo@skimbyte.app")
        viewModel.onPasswordChanged("demo")
        var succeeded = false

        viewModel.onSubmit { succeeded = true }

        assertTrue(succeeded)
        assertEquals(LoginSheetUiState(), viewModel.uiState.value)
    }

    @Test
    fun onSubmit_failure_setsError_andStopsSubmitting() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException("invalid"))))
        viewModel.onEmailChanged("wrong@skimbyte.app")
        viewModel.onPasswordChanged("wrong")
        var succeeded = false

        viewModel.onSubmit { succeeded = true }

        assertTrue(!succeeded)
        assertEquals(false, viewModel.uiState.value.isSubmitting)
        assertTrue(viewModel.uiState.value.error != null)
    }

    @Test
    fun onToggleMode_switchesToRegisterMode() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))

        viewModel.onToggleMode()

        assertTrue(viewModel.uiState.value.isRegisterMode)
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
        viewModel.onToggleMode()
        viewModel.onEmailChanged("new@skimbyte.app")
        viewModel.onPasswordChanged("newpass")

        viewModel.onSubmit {}

        assertTrue(registerCalled)
    }

    @Test
    fun onGoogleRedirect_success_resetsState_andInvokesCallback() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.success(Unit)))
        var succeeded = false

        viewModel.onGoogleRedirect("skimbyte://auth/callback?access_token=a&refresh_token=b") { succeeded = true }

        assertTrue(succeeded)
        assertEquals(LoginSheetUiState(), viewModel.uiState.value)
    }

    @Test
    fun onEmailChanged_clearsExistingError() = runTest {
        val viewModel = LoginSheetViewModel(FakeAuthRepository(Result.failure(RuntimeException())))
        viewModel.onEmailChanged("a")
        viewModel.onPasswordChanged("b")
        viewModel.onSubmit {}
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.onEmailChanged("new@skimbyte.app")

        assertNull(viewModel.uiState.value.error)
    }
}
