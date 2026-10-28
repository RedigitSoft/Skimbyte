package io.redigit.skimbyte.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Search is parked for now - its screens/VM remain in ui/screens/feed, re-add the entry (and the
// icon/shell branches) to bring the tab back.
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
data object SearchHome : NavKey

@Serializable
data class PostDetail(val postId: String) : NavKey

@Serializable
data object AccountHome : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object Topics : NavKey

@Serializable
data object AvatarViewer : NavKey

@Serializable
data object BannerViewer : NavKey
