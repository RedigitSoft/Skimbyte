package io.redigit.skimbyte.ui.screens.bookmarks

sealed interface BookmarksIntent {
    data class Unbookmark(val postId: String) : BookmarksIntent
    data object PostsNearEnd : BookmarksIntent
    data object LoginSucceeded : BookmarksIntent
    data object Refresh : BookmarksIntent
}
