package io.redigit.skimbyte.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.posts.PostsRepository
import io.redigit.skimbyte.posts.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Search is fully self-contained: unlike the old design where it read [FeedViewModel]'s already-
 * loaded posts, it fetches its own copy on first load and filters it client-side. This avoids the
 * cross-tab coupling that conflicted with the "independent backstack per top-level destination"
 * decision - Search can now be opened without the Feed tab ever having been visited.
 */
class SearchViewModel(private val postsRepository: PostsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var loadedPosts: List<Post> = emptyList()

    init {
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(Topic.All, cursor = null)
            result.onSuccess { page ->
                loadedPosts = page.items
                recomputeResults()
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        recomputeResults()
    }

    fun onBookmarkClick(postId: String) {
        val currentlyBookmarked = loadedPosts.firstOrNull { it.id == postId }?.isBookmarked ?: return
        val newValue = !currentlyBookmarked
        updatePostBookmarkState(postId, newValue)
        _uiState.value = _uiState.value.copy(pendingBookmarkToggles = _uiState.value.pendingBookmarkToggles + postId)
        viewModelScope.launch {
            val result = postsRepository.setBookmarked(postId, newValue)
            if (result.isFailure) {
                updatePostBookmarkState(postId, currentlyBookmarked)
            }
            _uiState.value = _uiState.value.copy(pendingBookmarkToggles = _uiState.value.pendingBookmarkToggles - postId)
        }
    }

    private fun updatePostBookmarkState(postId: String, bookmarked: Boolean) {
        loadedPosts = loadedPosts.map { if (it.id == postId) it.copy(isBookmarked = bookmarked) else it }
        recomputeResults()
    }

    private fun recomputeResults() {
        val query = _uiState.value.query
        _uiState.value = _uiState.value.copy(
            results = if (query.isBlank()) emptyList() else loadedPosts.filter { it.title.contains(query, ignoreCase = true) },
        )
    }
}
