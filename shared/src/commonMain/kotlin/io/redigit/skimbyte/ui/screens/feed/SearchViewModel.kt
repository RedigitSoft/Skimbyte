package io.redigit.skimbyte.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.PostsRepository
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

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

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

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
            is SearchIntent.BookmarkClicked -> onBookmarkClick(intent.postId)
        }
    }

    private fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
        recomputeResults()
    }

    private fun onBookmarkClick(postId: String) {
        val currentlyBookmarked = loadedPosts.firstOrNull { it.id == postId }?.isBookmarked ?: return
        val newValue = !currentlyBookmarked
        updatePostBookmarkState(postId, newValue)
        _state.value = _state.value.copy(pendingBookmarkToggles = _state.value.pendingBookmarkToggles + postId)
        viewModelScope.launch {
            val result = postsRepository.setBookmarked(postId, newValue)
            if (result.isFailure) {
                updatePostBookmarkState(postId, currentlyBookmarked)
            }
            _state.value = _state.value.copy(pendingBookmarkToggles = _state.value.pendingBookmarkToggles - postId)
        }
    }

    private fun updatePostBookmarkState(postId: String, bookmarked: Boolean) {
        loadedPosts = loadedPosts.map { if (it.id == postId) it.copy(isBookmarked = bookmarked) else it }
        recomputeResults()
    }

    private fun recomputeResults() {
        val query = _state.value.query
        _state.value = _state.value.copy(
            results = if (query.isBlank()) emptyList() else loadedPosts.filter { it.title.contains(query, ignoreCase = true) },
        )
    }
}
