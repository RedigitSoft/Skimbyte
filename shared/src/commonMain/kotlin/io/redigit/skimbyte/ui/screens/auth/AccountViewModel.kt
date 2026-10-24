package io.redigit.skimbyte.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.models.Topic
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

/**
 * Placeholder profile values shown until the `/me` endpoint returns the real ones. Centralized and
 * clearly named so they're trivial to delete once the server fields land - see SERVER_API.md for
 * the exact contract the backend needs to provide.
 */
internal object ProfilePlaceholders {
    const val DISPLAY_NAME = "Skimbyte Reader"
    const val HANDLE = "@skimbyte"
    const val BIO = "Reading the daily developer brief on Skimbyte."
    const val VERIFIED = true
    const val READ_COUNT = 142
    const val STREAK_DAYS = 14
    val WEEKLY_READS = listOf(8, 12, 6, 14, 20, 9, 11)
}

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    followedTopics: StateFlow<Set<Topic>>,
    bookmarkIds: StateFlow<Set<String>>,
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
                    displayName = if (loggedIn) profile?.displayName ?: ProfilePlaceholders.DISPLAY_NAME else null,
                    handle = if (loggedIn) {
                        profile?.handle ?: profile?.userId?.let { "@" + it.take(8) } ?: ProfilePlaceholders.HANDLE
                    } else {
                        null
                    },
                    bio = if (loggedIn) profile?.bio ?: ProfilePlaceholders.BIO else null,
                    isVerified = loggedIn && (profile?.isVerified ?: ProfilePlaceholders.VERIFIED),
                    readCount = if (loggedIn) profile?.readCount ?: ProfilePlaceholders.READ_COUNT else null,
                    savedCount = if (loggedIn) profile?.savedCount ?: bookmarks.size else null,
                    streakDays = if (loggedIn) profile?.streakDays ?: ProfilePlaceholders.STREAK_DAYS else null,
                    weeklyReads = if (loggedIn) {
                        profile?.weeklyReads?.ifEmpty { null } ?: ProfilePlaceholders.WEEKLY_READS
                    } else {
                        emptyList()
                    },
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
                    bio = d.bio,
                    isVerified = d.isVerified,
                    readCount = d.readCount,
                    savedCount = d.savedCount,
                    streakDays = d.streakDays,
                    weeklyReads = d.weeklyReads,
                    followedTopics = d.followedTopics,
                )
            }
        }
    }

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.Logout -> authRepository.logout()
            is AccountIntent.AvatarPicked -> onAvatarPicked(intent.image)
            is AccountIntent.BannerPicked -> onBannerPicked(intent.image)
            AccountIntent.AvatarRemoved -> viewModelScope.launch { profileRepository.removeAvatar() }
            AccountIntent.BannerRemoved -> viewModelScope.launch { profileRepository.removeBanner() }
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

    private data class ProfileDerived(
        val loggedIn: Boolean,
        val avatarUrl: String?,
        val bannerUrl: String?,
        val displayName: String?,
        val handle: String?,
        val bio: String?,
        val isVerified: Boolean,
        val readCount: Int?,
        val savedCount: Int?,
        val streakDays: Int?,
        val weeklyReads: List<Int>,
        val followedTopics: List<Topic>,
    )
}
