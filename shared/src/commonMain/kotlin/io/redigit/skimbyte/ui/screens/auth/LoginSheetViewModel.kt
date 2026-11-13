package io.redigit.skimbyte.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.ui.SkimbyteStrings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginSheetViewModel(
    authRepository: AuthRepository,
    /** [io.redigit.skimbyte.data.repositories.AuthRedirectHandler.failures], injected as a bare
     * flow so the sheet depends only on the outcome events, not the handler itself. */
    redirectFailures: SharedFlow<Unit>,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginSheetState())
    val state: StateFlow<LoginSheetState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LoginSheetEffect>()
    val effects: SharedFlow<LoginSheetEffect> = _effects.asSharedFlow()

    init {
        // OAuth redirects are completed app-wide by AuthRedirectHandler (one can arrive with no
        // sheet showing at all). The sheet only reacts to the outcome: a session appearing means
        // sign-in succeeded, however it was initiated.
        viewModelScope.launch {
            authRepository.session.filterNotNull().first()
            _state.value = LoginSheetState()
            _effects.emit(LoginSheetEffect.LoginSucceeded)
        }
        viewModelScope.launch {
            redirectFailures.collect {
                _state.value = _state.value.copy(isSubmitting = false, error = SkimbyteStrings.Login.GOOGLE_FAILED)
            }
        }
    }

    fun onIntent(intent: LoginSheetIntent) {
        when (intent) {
            // The GitHub flow is scaffolded but gated on the server endpoint: when enabled it starts
            // like Google (the sheet opens the GitHub /start URL; the shared redirect completes it);
            // otherwise it surfaces an honest "coming soon" message. See ApiConfig.GITHUB_SIGN_IN_ENABLED.
            LoginSheetIntent.GithubSignInStarted ->
                _state.value = if (ApiConfig.GITHUB_SIGN_IN_ENABLED) {
                    _state.value.copy(isSubmitting = true, error = null)
                } else {
                    _state.value.copy(error = SkimbyteStrings.Login.GITHUB_COMING_SOON)
                }
            // Apple is gated exactly like GitHub: same provider-/start URL shape on the server,
            // same shared redirect completion. See ApiConfig.APPLE_SIGN_IN_ENABLED.
            LoginSheetIntent.AppleSignInStarted ->
                _state.value = if (ApiConfig.APPLE_SIGN_IN_ENABLED) {
                    _state.value.copy(isSubmitting = true, error = null)
                } else {
                    _state.value.copy(error = SkimbyteStrings.Login.APPLE_COMING_SOON)
                }
            LoginSheetIntent.GoogleSignInStarted -> _state.value = _state.value.copy(isSubmitting = true, error = null)
            LoginSheetIntent.Dismissed -> _state.value = LoginSheetState()
        }
    }
}
