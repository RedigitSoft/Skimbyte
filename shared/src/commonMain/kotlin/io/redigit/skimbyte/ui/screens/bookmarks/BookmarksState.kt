package io.redigit.skimbyte.ui.screens.bookmarks

import io.redigit.skimbyte.data.models.Post

data class BookmarksState(
    val posts: List<Post> = emptyList(),
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val error: BookmarksError? = null,
    val isLoggedOut: Boolean = false,
)

enum class BookmarksError { Network, Unknown }
