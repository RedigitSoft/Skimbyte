package io.redigit.skimbyte.auth.ui

import io.redigit.skimbyte.auth.AuthRepository
import io.redigit.skimbyte.auth.Session
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

private class FakeAuthRepository(private val loginResult: Result<Unit>) : AuthRepository {
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override suspend fun login(email: String, password: String): Result<Unit> = loginResult
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
