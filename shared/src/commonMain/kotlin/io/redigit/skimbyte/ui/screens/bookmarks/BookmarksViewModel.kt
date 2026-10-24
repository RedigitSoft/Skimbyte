package io.redigit.skimbyte.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val postsRepository: PostsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())
    val state: StateFlow<BookmarksState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                if (session == null) {
                    _state.value = BookmarksState(isLoggedOut = true)
                } else {
                    _state.value = _state.value.copy(isLoggedOut = false)
                    loadInitial()
                }
            }
        }
    }

    fun onIntent(intent: BookmarksIntent) {
        when (intent) {
            is BookmarksIntent.Unbookmark -> onUnbookmark(intent.postId)
            BookmarksIntent.PostsNearEnd -> onPostsNearEnd()
            BookmarksIntent.LoginSucceeded -> loadInitial()
        }
    }

    private fun onUnbookmark(postId: String) {
        _state.value = _state.value.copy(posts = _state.value.posts.filterNot { it.id == postId })
        viewModelScope.launch {
            val result = postsRepository.setBookmarked(postId, bookmarked = false)
            // Reverting precisely (re-inserting at its original position) isn't worth the
            // complexity here; a reload keeps the list correct on the rare failure case.
            if (result.isFailure) loadInitial()
        }
    }

    private fun onPostsNearEnd() {
        val state = _state.value
        if (state.nextCursor != null && !state.isLoadingMore) loadMore()
    }

    private fun loadInitial() {
        _state.value = _state.value.copy(isLoadingInitial = true, error = null)
        viewModelScope.launch {
            val result = postsRepository.fetchBookmarkedPosts(cursor = null)
            result.fold(
                onSuccess = { page ->
                    _state.value = _state.value.copy(
                        posts = page.items,
                        nextCursor = page.nextCursor,
                        isLoadingInitial = false,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoadingInitial = false, error = BookmarksError.Network)
                },
            )
        }
    }

    private fun loadMore() {
        val cursor = _state.value.nextCursor ?: return
        _state.value = _state.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = postsRepository.fetchBookmarkedPosts(cursor)
            result.fold(
                onSuccess = { page ->
                    _state.value = _state.value.copy(
                        posts = _state.value.posts + page.items,
                        nextCursor = page.nextCursor,
                        isLoadingMore = false,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoadingMore = false, error = BookmarksError.Network)
                },
            )
        }
    }
}
