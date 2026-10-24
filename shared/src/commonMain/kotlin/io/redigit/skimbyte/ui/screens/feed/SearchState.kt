package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post

data class SearchState(
    val query: String = "",
    val results: List<Post> = emptyList(),
    val pendingBookmarkToggles: Set<String> = emptySet(),
)
