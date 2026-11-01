package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.util.PickedImage

/** Which profile image an in-progress pick/crop targets. */
enum class ImageTarget { Avatar, Banner }

/** A just-picked image awaiting the interactive crop step before it's uploaded. */
data class PendingCrop(val target: ImageTarget, val image: PickedImage)

data class AccountState(
    val isLoggedIn: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val avatarUploadInProgress: Boolean = false,
    val bannerUploadInProgress: Boolean = false,
    val isRefreshing: Boolean = false,
    val pendingCrop: PendingCrop? = null,
    val avatarError: String? = null,
    val bannerError: String? = null,
    val deleteDataInProgress: Boolean = false,
    val deleteDataError: String? = null,
    // Identity shown on the profile screen, plus the saved count. Populated by AccountViewModel from
    // the real profile where available (handle derived from userId, saved count from bookmarks,
    // followed topics from preferences); fields the server doesn't return yet stay null/empty.
    val displayName: String? = null,
    val handle: String? = null,
    val email: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val savedCount: Int? = null,
    val followedTopics: List<Topic> = emptyList(),
)
