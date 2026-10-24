package io.redigit.skimbyte.ui.screens.feed

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class BookmarkClicked(val postId: String) : SearchIntent
}
