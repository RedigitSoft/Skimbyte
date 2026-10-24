package io.redigit.skimbyte.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginSheetViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginSheetState())
    val state: StateFlow<LoginSheetState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LoginSheetEffect>()
    val effects: SharedFlow<LoginSheetEffect> = _effects.asSharedFlow()

    fun onIntent(intent: LoginSheetIntent) {
        when (intent) {
            // GitHub OAuth is not wired yet (needs server support); surface an honest message until
            // the dedicated GitHub task lands.
            LoginSheetIntent.GithubSignInStarted ->
                _state.value = _state.value.copy(error = "GitHub sign-in is coming soon")
            LoginSheetIntent.GoogleSignInStarted -> _state.value = _state.value.copy(isSubmitting = true, error = null)
            is LoginSheetIntent.GoogleRedirect -> googleRedirect(intent.redirectUri)
            LoginSheetIntent.Dismissed -> _state.value = LoginSheetState()
        }
    }

    private fun googleRedirect(redirectUri: String) {
        viewModelScope.launch {
            val result = authRepository.completeGoogleSignIn(redirectUri)
            result.fold(
                onSuccess = {
                    _state.value = LoginSheetState()
                    _effects.emit(LoginSheetEffect.LoginSucceeded)
                },
                onFailure = {
                    _state.value = _state.value.copy(isSubmitting = false, error = "Google sign-in failed")
                },
            )
        }
    }
}
