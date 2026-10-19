package io.redigit.skimbyte.feed

import io.redigit.skimbyte.posts.Post

data class SearchUiState(
    val query: String = "",
    val results: List<Post> = emptyList(),
    val pendingBookmarkToggles: Set<String> = emptySet(),
)
