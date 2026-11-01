package io.redigit.skimbyte.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

enum class TopLevelDestination {
    Feed,
    Bookmarks,
    Account,
}

@Serializable
data object BookmarksList : NavKey

@Serializable
data object FeedHome : NavKey

@Serializable
data class PostDetail(val postId: String) : NavKey

@Serializable
data object AccountHome : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object Topics : NavKey

@Serializable
data object Licenses : NavKey

@Serializable
data object AvatarViewer : NavKey

@Serializable
data object BannerViewer : NavKey
