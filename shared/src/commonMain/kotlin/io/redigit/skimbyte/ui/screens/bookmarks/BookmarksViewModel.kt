package io.redigit.skimbyte.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.BookmarkIdsStore
import io.redigit.skimbyte.data.repositories.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val postsRepository: PostsRepository,
    private val authRepository: AuthRepository,
    private val bookmarkIdsStore: BookmarkIdsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())
    val state: StateFlow<BookmarksState> = _state.asStateFlow()

    private var reconcileInFlight = false

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
        // Bookmarks toggled on other screens (feed, post detail) flip the shared BookmarkIdsStore
        // instantly, so reacting to its *transitions* keeps this list current without a manual
        // pull-to-refresh. Transitions rather than absolute membership because the store only
        // tracks the newest page of ids — posts paginated deeper into this list are legitimately
        // absent from it and must not be dropped.
        viewModelScope.launch {
            var previous = bookmarkIdsStore.ids.value
            bookmarkIdsStore.ids.collect { current ->
                val added = current - previous
                val removed = previous - current
                previous = current
                reconcile(added, removed)
            }
        }
    }

    private fun reconcile(added: Set<String>, removed: Set<String>) {
        if (removed.isNotEmpty()) {
            _state.value = _state.value.copy(posts = _state.value.posts.filterNot { it.id in removed })
        }
        // An id this list has never seen means a bookmark made elsewhere: quietly re-fetch the
        // first page so the tab is already up to date when the user switches to it. Skipped while
        // the initial load is in flight — its response will contain the new id anyway.
        val state = _state.value
        if (state.isLoggedOut || state.isLoadingInitial) return
        if (added.any { id -> state.posts.none { it.id == id } }) reloadQuietly()
    }

    // Like onRefresh but with no indicator and best-effort: a failure just leaves the list stale,
    // and pull-to-refresh remains the explicit recovery path.
    private fun reloadQuietly() {
        if (reconcileInFlight) return
        reconcileInFlight = true
        viewModelScope.launch {
            postsRepository.fetchBookmarkedPosts(cursor = null).onSuccess { page ->
                _state.value = _state.value.copy(posts = page.items, nextCursor = page.nextCursor)
            }
            reconcileInFlight = false
        }
    }

    fun onIntent(intent: BookmarksIntent) {
        when (intent) {
            is BookmarksIntent.Unbookmark -> onUnbookmark(intent.postId)
            BookmarksIntent.PostsNearEnd -> onPostsNearEnd()
            BookmarksIntent.LoginSucceeded -> loadInitial()
            BookmarksIntent.Refresh -> onRefresh()
        }
    }

    // Unlike loadInitial(), this keeps the existing list on screen (no full-screen spinner) and
    // surfaces progress through the pull-to-refresh indicator via isRefreshing.
    private fun onRefresh() {
        if (_state.value.isRefreshing) return
        _state.value = _state.value.copy(isRefreshing = true, error = null)
        viewModelScope.launch {
            val result = postsRepository.fetchBookmarkedPosts(cursor = null)
            result.fold(
                onSuccess = { page ->
                    _state.value = _state.value.copy(
                        posts = page.items,
                        nextCursor = page.nextCursor,
                        isRefreshing = false,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isRefreshing = false, error = BookmarksError.Network)
                },
            )
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
