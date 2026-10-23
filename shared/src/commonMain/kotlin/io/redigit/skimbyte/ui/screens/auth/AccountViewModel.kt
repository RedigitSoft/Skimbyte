package io.redigit.skimbyte.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.ProfileRepository
import io.redigit.skimbyte.util.PickedImage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AccountEffect>()
    val effects: SharedFlow<AccountEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.session, profileRepository.profile) { session, profile -> session to profile }
                .collect { (session, profile) ->
                    _state.value = _state.value.copy(
                        isLoggedIn = session != null,
                        avatarUrl = profile?.avatarUrl,
                        bannerUrl = profile?.bannerUrl,
                    )
                }
        }
    }

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.Logout -> authRepository.logout()
            is AccountIntent.AvatarPicked -> onAvatarPicked(intent.image)
            is AccountIntent.BannerPicked -> onBannerPicked(intent.image)
        }
    }

    private fun onAvatarPicked(image: PickedImage) {
        if (_state.value.avatarUploadInProgress) return
        _state.value = _state.value.copy(avatarUploadInProgress = true, avatarError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadAvatar(image)
            _state.value = _state.value.copy(
                avatarUploadInProgress = false,
                avatarError = result.exceptionOrNull()?.let { "Could not update avatar" },
            )
        }
    }

    private fun onBannerPicked(image: PickedImage) {
        if (_state.value.bannerUploadInProgress) return
        _state.value = _state.value.copy(bannerUploadInProgress = true, bannerError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadBanner(image)
            _state.value = _state.value.copy(
                bannerUploadInProgress = false,
                bannerError = result.exceptionOrNull()?.let { "Could not update banner" },
            )
        }
    }
}
