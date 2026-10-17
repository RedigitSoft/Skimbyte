package io.redigit.skimbyte.nav

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
data class BookmarkDetail(val postId: String) : NavKey

@Serializable
data object FeedHome : NavKey

@Serializable
data object SearchHome : NavKey

@Serializable
data class SourceDetail(val sourceName: String) : NavKey
