package io.redigit.skimbyte.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redigit.skimbyte.data.local.TopicPreferencesStore
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.data.repositories.AuthRepository
import io.redigit.skimbyte.data.repositories.BookmarkIdsStore
import io.redigit.skimbyte.data.repositories.PostsRepository
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

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    init {
        // "All" is always the first filter, followed by the user's chosen interests, and is the
        // default selection so the feed opens showing everything.
        _state.value = _state.value.copy(
            availableTopics = listOf(Topic.All) + topicPreferencesStore.selectedTopics(),
            selectedTopic = Topic.All,
        )
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

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.TopicSelected -> onTopicSelected(intent.topic)
            FeedIntent.ViewModeToggled -> onViewModeToggled()
            is FeedIntent.PageChanged -> onPageChanged(intent.index)
            is FeedIntent.BookmarkTapped -> onBookmarkTapped(intent.postId)
            FeedIntent.LoginSucceeded -> onLoginSucceeded()
            FeedIntent.LoginDismissed -> onLoginDismissed()
        }
    }

    private fun onTopicPreferencesChanged() {
        val storedTopics = topicPreferencesStore.selectedTopics().toList()
        _state.value = _state.value.copy(availableTopics = listOf(Topic.All) + storedTopics)
        val currentTopic = _state.value.selectedTopic
        if (currentTopic != Topic.All && currentTopic !in storedTopics) {
            onTopicSelected(Topic.All)
        }
    }

    private fun onTopicSelected(topic: Topic) {
        if (topic == _state.value.selectedTopic) return
        _state.value = _state.value.copy(
            selectedTopic = topic,
            posts = emptyList(),
            currentIndex = 0,
            nextCursor = null,
        )
        loadInitial()
    }

    private fun onViewModeToggled() {
        val newMode = if (_state.value.viewMode == FeedViewMode.Stack) FeedViewMode.List else FeedViewMode.Stack
        _state.value = _state.value.copy(viewMode = newMode)
    }

    private fun onPageChanged(index: Int) {
        _state.value = _state.value.copy(currentIndex = index)
        val state = _state.value
        if (index >= state.posts.size - PREFETCH_THRESHOLD && state.nextCursor != null && !state.isLoadingMore) {
            loadMore()
        }
    }

    private fun onBookmarkTapped(postId: String) {
        if (authRepository.session.value == null) {
            _state.value = _state.value.copy(pendingBookmarkAction = PendingBookmarkAction(postId))
            return
        }
        toggleBookmark(postId)
    }

    private fun onLoginSucceeded() {
        val pending = _state.value.pendingBookmarkAction ?: return
        _state.value = _state.value.copy(pendingBookmarkAction = null)
        toggleBookmark(pending.postId)
    }

    private fun onLoginDismissed() {
        _state.value = _state.value.copy(pendingBookmarkAction = null)
    }

    private fun toggleBookmark(postId: String) {
        val currentlyBookmarked = _state.value.posts.firstOrNull { it.id == postId }?.isBookmarked ?: return
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
        _state.value = _state.value.copy(
            posts = _state.value.posts.map { if (it.id == postId) it.copy(isBookmarked = bookmarked) else it },
        )
    }

    private fun loadInitial() {
        _state.value = _state.value.copy(isLoadingInitial = true, error = null)
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(_state.value.selectedTopic, cursor = null)
            result.fold(
                onSuccess = { page ->
                    _state.value = _state.value.copy(
                        posts = page.items,
                        nextCursor = page.nextCursor,
                        isLoadingInitial = false,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoadingInitial = false, error = FeedError.Network)
                },
            )
        }
    }

    private fun loadMore() {
        val cursor = _state.value.nextCursor ?: return
        _state.value = _state.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(_state.value.selectedTopic, cursor)
            result.fold(
                onSuccess = { page ->
                    _state.value = _state.value.copy(
                        posts = _state.value.posts + page.items,
                        nextCursor = page.nextCursor,
                        isLoadingMore = false,
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoadingMore = false, error = FeedError.Network)
                },
            )
        }
    }

    private companion object {
        const val PREFETCH_THRESHOLD = 3
    }
}
