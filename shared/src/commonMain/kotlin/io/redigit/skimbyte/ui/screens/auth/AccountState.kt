package io.redigit.skimbyte.ui.screens.auth

import io.redigit.skimbyte.data.models.Topic

data class AccountState(
    val isLoggedIn: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val avatarUploadInProgress: Boolean = false,
    val bannerUploadInProgress: Boolean = false,
    val avatarError: String? = null,
    val bannerError: String? = null,
    // Identity + reading stats shown on the profile screen. Populated by AccountViewModel from the
    // real profile where available (handle derived from userId, saved count from bookmarks, followed
    // topics from preferences) and from placeholders otherwise (see ProfilePlaceholders / SERVER_API.md).
    val displayName: String? = null,
    val handle: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val readCount: Int? = null,
    val savedCount: Int? = null,
    val streakDays: Int? = null,
    val weeklyReads: List<Int> = emptyList(),
    val followedTopics: List<Topic> = emptyList(),
)
