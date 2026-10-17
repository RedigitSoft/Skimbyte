package io.redigit.skimbyte.bookmarks

import io.redigit.skimbyte.posts.Post

data class BookmarksUiState(
    val posts: List<Post> = emptyList(),
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val error: BookmarksError? = null,
    val isLoggedOut: Boolean = false,
)

enum class BookmarksError { Network, Unknown }
