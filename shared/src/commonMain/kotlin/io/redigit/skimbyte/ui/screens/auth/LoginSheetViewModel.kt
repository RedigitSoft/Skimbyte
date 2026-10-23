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
            is LoginSheetIntent.EmailChanged -> _state.value = _state.value.copy(email = intent.value, error = null)
            is LoginSheetIntent.PasswordChanged -> _state.value = _state.value.copy(password = intent.value, error = null)
            LoginSheetIntent.ModeToggled -> _state.value = _state.value.copy(isRegisterMode = !_state.value.isRegisterMode, error = null)
            LoginSheetIntent.Submit -> submit()
            LoginSheetIntent.GoogleSignInStarted -> _state.value = _state.value.copy(isSubmitting = true, error = null)
            is LoginSheetIntent.GoogleRedirect -> googleRedirect(intent.redirectUri)
            LoginSheetIntent.Dismissed -> _state.value = LoginSheetState()
        }
    }

    private fun submit() {
        val state = _state.value
        if (state.email.isBlank() || state.password.isBlank() || state.isSubmitting) return
        _state.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            val result = if (state.isRegisterMode) {
                authRepository.register(state.email, state.password)
            } else {
                authRepository.login(state.email, state.password)
            }
            result.fold(
                onSuccess = {
                    _state.value = LoginSheetState()
                    _effects.emit(LoginSheetEffect.LoginSucceeded)
                },
                onFailure = {
                    val message = if (state.isRegisterMode) "Could not create account" else "Invalid email or password"
                    _state.value = _state.value.copy(isSubmitting = false, error = message)
                },
            )
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
