package io.redigit.skimbyte.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginSheetViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginSheetUiState())
    val uiState: StateFlow<LoginSheetUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onSubmit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.isSubmitting) return
        _uiState.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            val result = authRepository.login(state.email, state.password)
            result.fold(
                onSuccess = {
                    _uiState.value = LoginSheetUiState()
                    onSuccess()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Invalid email or password")
                },
            )
        }
    }

    fun resetForDismiss() {
        _uiState.value = LoginSheetUiState()
    }
}
