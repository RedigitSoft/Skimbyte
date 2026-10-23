package io.redigit.skimbyte.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.onboarding.TopicPreferencesStore
import io.redigit.skimbyte.posts.BookmarkIdsStore
import io.redigit.skimbyte.posts.PostsRepository
import io.redigit.skimbyte.posts.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class FeedViewModel(
    private val postsRepository: PostsRepository,
    private val authRepository: AuthRepository,
    private val bookmarkIdsStore: BookmarkIdsStore,
    private val topicPreferencesStore: TopicPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        val storedTopics = topicPreferencesStore.selectedTopics()
        if (storedTopics.isNotEmpty()) {
            val availableTopics = storedTopics.toList()
            _uiState.value = _uiState.value.copy(
                availableTopics = availableTopics,
                selectedTopic = availableTopics.first(),
            )
        }
        if (authRepository.session.value != null) {
            viewModelScope.launch { bookmarkIdsStore.refresh() }
        }
        loadInitial()
        viewModelScope.launch {
            authRepository.session.collect { session ->
                if (session != null) bookmarkIdsStore.refresh() else bookmarkIdsStore.clear()
            }
        }
        viewModelScope.launch {
            topicPreferencesStore.selectedTopicsFlow.drop(1).collect { onTopicPreferencesChanged() }
        }
    }

    fun onTopicPreferencesChanged() {
        val storedTopics = topicPreferencesStore.selectedTopics().toList()
        _uiState.value = _uiState.value.copy(availableTopics = storedTopics)
        val currentTopic = _uiState.value.selectedTopic
        if (storedTopics.isNotEmpty() && currentTopic !in storedTopics) {
            onTopicSelected(storedTopics.first())
        }
    }

    fun onTopicSelected(topic: Topic) {
        if (topic == _uiState.value.selectedTopic) return
        _uiState.value = _uiState.value.copy(
            selectedTopic = topic,
            posts = emptyList(),
            currentIndex = 0,
            nextCursor = null,
        )
        loadInitial()
    }

    fun onViewModeToggled() {
        val newMode = if (_uiState.value.viewMode == FeedViewMode.Stack) FeedViewMode.List else FeedViewMode.Stack
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }

    fun onPageChanged(index: Int) {
        _uiState.value = _uiState.value.copy(currentIndex = index)
        val state = _uiState.value
        if (index >= state.posts.size - PREFETCH_THRESHOLD && state.nextCursor != null && !state.isLoadingMore) {
            loadMore()
        }
    }

    fun onBookmarkTapped(postId: String) {
        if (authRepository.session.value == null) {
            _uiState.value = _uiState.value.copy(pendingBookmarkAction = PendingBookmarkAction(postId))
            return
        }
        toggleBookmark(postId)
    }

    fun onLoginSucceeded() {
        val pending = _uiState.value.pendingBookmarkAction ?: return
        _uiState.value = _uiState.value.copy(pendingBookmarkAction = null)
        toggleBookmark(pending.postId)
    }

    fun onLoginDismissed() {
        _uiState.value = _uiState.value.copy(pendingBookmarkAction = null)
    }

    private fun toggleBookmark(postId: String) {
        val currentlyBookmarked = _uiState.value.posts.firstOrNull { it.id == postId }?.isBookmarked ?: return
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
        _uiState.value = _uiState.value.copy(
            posts = _uiState.value.posts.map { if (it.id == postId) it.copy(isBookmarked = bookmarked) else it },
        )
    }

    private fun loadInitial() {
        _uiState.value = _uiState.value.copy(isLoadingInitial = true, error = null)
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(_uiState.value.selectedTopic, cursor = null)
            result.fold(
                onSuccess = { page ->
                    _uiState.value = _uiState.value.copy(
                        posts = page.items,
                        nextCursor = page.nextCursor,
                        isLoadingInitial = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoadingInitial = false, error = FeedError.Network)
                },
            )
        }
    }

    private fun loadMore() {
        val cursor = _uiState.value.nextCursor ?: return
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(_uiState.value.selectedTopic, cursor)
            result.fold(
                onSuccess = { page ->
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts + page.items,
                        nextCursor = page.nextCursor,
                        isLoadingMore = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false, error = FeedError.Network)
                },
            )
        }
    }

    private companion object {
        const val PREFETCH_THRESHOLD = 3
    }
}
