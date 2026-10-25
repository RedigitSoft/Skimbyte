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
        // "All" is always the first page, followed by the user's chosen interests, and is selected
        // first so the feed opens showing everything.
        _state.value = _state.value.copy(
            availableTopics = listOf(Topic.All) + topicPreferencesStore.selectedTopics(),
            selectedTopic = Topic.All,
        )
        if (authRepository.session.value != null) {
            viewModelScope.launch { bookmarkIdsStore.refresh() }
        }
        loadInitial(Topic.All)
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
            is FeedIntent.PageChanged -> onPageChanged(intent.index)
            is FeedIntent.BookmarkTapped -> onBookmarkTapped(intent.postId)
            FeedIntent.LoginSucceeded -> onLoginSucceeded()
            FeedIntent.LoginDismissed -> onLoginDismissed()
            FeedIntent.Refresh -> onRefresh()
        }
    }

    private fun onRefresh() {
        if (_state.value.isRefreshing) return
        val topic = _state.value.selectedTopic
        _state.value = _state.value.copy(isRefreshing = true)
        if (authRepository.session.value != null) {
            viewModelScope.launch { bookmarkIdsStore.refresh() }
        }
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(topic, cursor = null)
            result.fold(
                onSuccess = { page ->
                    updateFeed(topic) {
                        it.copy(posts = page.items, nextCursor = page.nextCursor, currentIndex = 0, error = null)
                    }
                },
                onFailure = { updateFeed(topic) { it.copy(error = FeedError.Network) } },
            )
            _state.value = _state.value.copy(isRefreshing = false)
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
        _state.value = _state.value.copy(selectedTopic = topic)
        val feed = _state.value.feeds[topic]
        if (feed == null || (feed.posts.isEmpty() && !feed.isLoadingInitial)) {
            loadInitial(topic)
        }
    }

    private fun onPageChanged(index: Int) {
        val topic = _state.value.selectedTopic
        updateFeed(topic) { it.copy(currentIndex = index) }
        val feed = _state.value.feeds[topic] ?: return
        if (index >= feed.posts.size - PREFETCH_THRESHOLD && feed.nextCursor != null && !feed.isLoadingMore) {
            loadMore(topic)
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
        val current = _state.value.feeds.values.firstNotNullOfOrNull { feed ->
            feed.posts.firstOrNull { it.id == postId }
        } ?: return
        val newValue = !current.isBookmarked
        setBookmarkInAllFeeds(postId, newValue)
        _state.value = _state.value.copy(pendingBookmarkToggles = _state.value.pendingBookmarkToggles + postId)
        viewModelScope.launch {
            val result = postsRepository.setBookmarked(postId, newValue)
            if (result.isFailure) {
                setBookmarkInAllFeeds(postId, current.isBookmarked)
            }
            _state.value = _state.value.copy(pendingBookmarkToggles = _state.value.pendingBookmarkToggles - postId)
        }
    }

    // A post can appear in both "All" and its topic feed, so bookmark state is kept in sync across
    // every loaded feed that contains it.
    private fun setBookmarkInAllFeeds(postId: String, bookmarked: Boolean) {
        _state.value = _state.value.copy(
            feeds = _state.value.feeds.mapValues { (_, feed) ->
                feed.copy(posts = feed.posts.map { if (it.id == postId) it.copy(isBookmarked = bookmarked) else it })
            },
        )
    }

    private fun loadInitial(topic: Topic) {
        updateFeed(topic) { it.copy(isLoadingInitial = true, error = null) }
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(topic, cursor = null)
            result.fold(
                onSuccess = { page ->
                    updateFeed(topic) {
                        it.copy(posts = page.items, nextCursor = page.nextCursor, isLoadingInitial = false)
                    }
                },
                onFailure = { updateFeed(topic) { it.copy(isLoadingInitial = false, error = FeedError.Network) } },
            )
        }
    }

    private fun loadMore(topic: Topic) {
        val cursor = _state.value.feeds[topic]?.nextCursor ?: return
        updateFeed(topic) { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val result = postsRepository.fetchPosts(topic, cursor)
            result.fold(
                onSuccess = { page ->
                    updateFeed(topic) {
                        it.copy(posts = it.posts + page.items, nextCursor = page.nextCursor, isLoadingMore = false)
                    }
                },
                onFailure = { updateFeed(topic) { it.copy(isLoadingMore = false, error = FeedError.Network) } },
            )
        }
    }

    private fun updateFeed(topic: Topic, transform: (TopicFeed) -> TopicFeed) {
        val current = _state.value.feeds[topic] ?: TopicFeed()
        _state.value = _state.value.copy(feeds = _state.value.feeds + (topic to transform(current)))
    }

    private companion object {
        const val PREFETCH_THRESHOLD = 3
    }
}
