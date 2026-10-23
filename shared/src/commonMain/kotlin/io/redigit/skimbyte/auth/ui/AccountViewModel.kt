package io.redigit.skimbyte.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.ProfileRepository
import io.redigit.skimbyte.util.PickedImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.session, profileRepository.profile) { session, profile -> session to profile }
                .collect { (session, profile) ->
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = session != null,
                        avatarUrl = profile?.avatarUrl,
                        bannerUrl = profile?.bannerUrl,
                    )
                }
        }
    }

    fun onLogout() = authRepository.logout()

    fun onAvatarPicked(image: PickedImage) {
        if (_uiState.value.avatarUploadInProgress) return
        _uiState.value = _uiState.value.copy(avatarUploadInProgress = true, avatarError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadAvatar(image)
            _uiState.value = _uiState.value.copy(
                avatarUploadInProgress = false,
                avatarError = result.exceptionOrNull()?.let { "Could not update avatar" },
            )
        }
    }

    fun onBannerPicked(image: PickedImage) {
        if (_uiState.value.bannerUploadInProgress) return
        _uiState.value = _uiState.value.copy(bannerUploadInProgress = true, bannerError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadBanner(image)
            _uiState.value = _uiState.value.copy(
                bannerUploadInProgress = false,
                bannerError = result.exceptionOrNull()?.let { "Could not update banner" },
            )
        }
    }
}
