package io.redigit.skimbyte.posts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the logged-in user's bookmarked post IDs so the (fully anonymous) feed endpoint never
 * needs to know about auth — PostsRepositoryImpl stamps isBookmarked onto feed items by checking
 * this set client-side. Only the first page is fetched for the ID set (a few thousand bookmarks
 * would need real accumulation across pages; treated as an edge case to revisit, not a blocker).
 */
class BookmarkIdsStore(private val postsApi: PostsApi) {
    private val _ids = MutableStateFlow<Set<String>>(emptySet())
    val ids: StateFlow<Set<String>> = _ids.asStateFlow()

    suspend fun refresh() {
        // Best-effort: a failed refresh (network blip, expired session) just leaves the
        // bookmark badges stale rather than crashing the feed that doesn't depend on them.
        runCatching { postsApi.fetchBookmarkedPosts(cursor = null).items.map { it.id }.toSet() }
            .onSuccess { _ids.value = it }
    }

    fun clear() {
        _ids.value = emptySet()
    }

    fun markBookmarked(postId: String) {
        _ids.value = _ids.value + postId
    }

    fun markUnbookmarked(postId: String) {
        _ids.value = _ids.value - postId
    }
}
