package io.redigit.skimbyte.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.auth.AuthRepository
import io.redigit.skimbyte.posts.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val postsRepository: PostsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                if (session == null) {
                    _uiState.value = BookmarksUiState(isLoggedOut = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoggedOut = false)
                    loadInitial()
                }
            }
        }
    }

    fun onLoginSucceeded() {
        loadInitial()
    }

    fun onUnbookmark(postId: String) {
        _uiState.value = _uiState.value.copy(posts = _uiState.value.posts.filterNot { it.id == postId })
        viewModelScope.launch {
            val result = postsRepository.setBookmarked(postId, bookmarked = false)
            // Reverting precisely (re-inserting at its original position) isn't worth the
            // complexity here; a reload keeps the list correct on the rare failure case.
            if (result.isFailure) loadInitial()
        }
    }

    fun onPostsNearEnd() {
        val state = _uiState.value
        if (state.nextCursor != null && !state.isLoadingMore) loadMore()
    }

    private fun loadInitial() {
        _uiState.value = _uiState.value.copy(isLoadingInitial = true, error = null)
        viewModelScope.launch {
            val result = postsRepository.fetchBookmarkedPosts(cursor = null)
            result.fold(
                onSuccess = { page ->
                    _uiState.value = _uiState.value.copy(
                        posts = page.items,
                        nextCursor = page.nextCursor,
                        isLoadingInitial = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoadingInitial = false, error = BookmarksError.Network)
                },
            )
        }
    }

    private fun loadMore() {
        val cursor = _uiState.value.nextCursor ?: return
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = postsRepository.fetchBookmarkedPosts(cursor)
            result.fold(
                onSuccess = { page ->
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts + page.items,
                        nextCursor = page.nextCursor,
                        isLoadingMore = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false, error = BookmarksError.Network)
                },
            )
        }
    }
}
