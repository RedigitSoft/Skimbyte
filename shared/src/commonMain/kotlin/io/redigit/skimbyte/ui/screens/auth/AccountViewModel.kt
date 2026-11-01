package io.redigit.skimbyte.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.ProfileRepository
import io.redigit.skimbyte.data.repositories.UploadStage
import io.redigit.skimbyte.data.repositories.UserDataRepository
import io.redigit.skimbyte.ui.components.ImageUploadState
import io.redigit.skimbyte.ui.components.isBusy
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
    followedTopics: StateFlow<Set<Topic>>,
    bookmarkIds: StateFlow<Set<String>>,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AccountEffect>()
    val effects: SharedFlow<AccountEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                authRepository.session,
                profileRepository.profile,
                followedTopics,
                bookmarkIds,
            ) { session, profile, topics, bookmarks ->
                val loggedIn = session != null
                ProfileDerived(
                    loggedIn = loggedIn,
                    avatarUrl = profile?.avatarUrl,
                    bannerUrl = profile?.bannerUrl,
                    // Only real data is surfaced: fields the server doesn't return yet stay null
                    // and their profile sections simply don't render. The handle and saved count
                    // are derived from data we do have.
                    displayName = if (loggedIn) profile?.displayName else null,
                    handle = if (loggedIn) {
                        profile?.handle ?: profile?.userId?.let { "@" + it.take(8) }
                    } else {
                        null
                    },
                    bio = if (loggedIn) profile?.bio else null,
                    email = if (loggedIn) profile?.email else null,
                    isVerified = loggedIn && profile?.isVerified == true,
                    savedCount = if (loggedIn) profile?.savedCount ?: bookmarks.size else null,
                    followedTopics = if (loggedIn) topics.toList() else emptyList(),
                )
            }.collect { d ->
                // Preserve the upload-in-progress / error fields, which this stream doesn't own.
                _state.value = _state.value.copy(
                    isLoggedIn = d.loggedIn,
                    avatarUrl = d.avatarUrl,
                    bannerUrl = d.bannerUrl,
                    displayName = d.displayName,
                    handle = d.handle,
                    email = d.email,
                    bio = d.bio,
                    isVerified = d.isVerified,
                    savedCount = d.savedCount,
                    followedTopics = d.followedTopics,
                )
            }
        }
    }

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.Logout -> authRepository.logout()
            is AccountIntent.AvatarPicked ->
                _state.value = _state.value.copy(pendingCrop = PendingCrop(ImageTarget.Avatar, intent.image))
            is AccountIntent.BannerPicked ->
                _state.value = _state.value.copy(pendingCrop = PendingCrop(ImageTarget.Banner, intent.image))
            is AccountIntent.CropConfirmed -> onCropConfirmed(intent.image)
            AccountIntent.CropCancelled -> _state.value = _state.value.copy(pendingCrop = null)
            AccountIntent.AvatarRemoved -> viewModelScope.launch { profileRepository.removeAvatar() }
            AccountIntent.BannerRemoved -> viewModelScope.launch { profileRepository.removeBanner() }
            AccountIntent.Refresh -> onRefresh()
            AccountIntent.DeleteDataConfirmed -> onDeleteData()
        }
    }

    private fun onDeleteData() {
        if (_state.value.deleteDataInProgress) return
        _state.value = _state.value.copy(deleteDataInProgress = true, deleteDataError = null)
        viewModelScope.launch {
            val result = userDataRepository.deleteAll()
            _state.value = _state.value.copy(
                deleteDataInProgress = false,
                deleteDataError = result.exceptionOrNull()?.let { "Could not delete your data" },
            )
            // Sign out only once the server confirms the delete; the session flow then flips to
            // signed-out and the combine block re-renders the screen.
            if (result.isSuccess) authRepository.logout()
        }
    }

    private fun onRefresh() {
        if (_state.value.isRefreshing) return
        _state.value = _state.value.copy(isRefreshing = true)
        viewModelScope.launch {
            profileRepository.refresh()
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    // The image arrives already cropped to the right aspect from the interactive cropper, so it's
    // uploaded as-is here.
    private fun onCropConfirmed(image: PickedImage) {
        val target = _state.value.pendingCrop?.target ?: return
        _state.value = _state.value.copy(pendingCrop = null)
        when (target) {
            ImageTarget.Avatar -> uploadAvatar(image)
            ImageTarget.Banner -> uploadBanner(image)
        }
    }

    private fun uploadAvatar(image: PickedImage) {
        if (_state.value.avatarUpload.isBusy) return
        _state.value = _state.value.copy(avatarUpload = ImageUploadState.Compressing, avatarError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadAvatar(image) { stage ->
                _state.value = _state.value.copy(avatarUpload = stage.toUploadState())
            }
            _state.value = _state.value.copy(
                avatarUpload = if (result.isSuccess) ImageUploadState.Idle else ImageUploadState.Error,
                avatarError = result.exceptionOrNull()?.let { "Could not update avatar" },
            )
        }
    }

    private fun uploadBanner(image: PickedImage) {
        if (_state.value.bannerUpload.isBusy) return
        _state.value = _state.value.copy(bannerUpload = ImageUploadState.Compressing, bannerError = null)
        viewModelScope.launch {
            val result = profileRepository.uploadBanner(image) { stage ->
                _state.value = _state.value.copy(bannerUpload = stage.toUploadState())
            }
            _state.value = _state.value.copy(
                bannerUpload = if (result.isSuccess) ImageUploadState.Idle else ImageUploadState.Error,
                bannerError = result.exceptionOrNull()?.let { "Could not update banner" },
            )
        }
    }

    private fun UploadStage.toUploadState(): ImageUploadState = when (this) {
        UploadStage.Compressing -> ImageUploadState.Compressing
        UploadStage.Uploading -> ImageUploadState.Uploading
    }

    private data class ProfileDerived(
        val loggedIn: Boolean,
        val avatarUrl: String?,
        val bannerUrl: String?,
        val displayName: String?,
        val handle: String?,
        val email: String?,
        val bio: String?,
        val isVerified: Boolean,
        val savedCount: Int?,
        val followedTopics: List<Topic>,
    )
}
