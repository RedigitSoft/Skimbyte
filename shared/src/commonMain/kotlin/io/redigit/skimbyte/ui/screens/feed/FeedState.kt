package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic

/**
 * The feed is a 2D pager: horizontal across [availableTopics], vertical (one card at a time) through
 * each topic's posts. Each topic therefore keeps its own [TopicFeed] (posts, scroll position,
 * pagination) so adjacent pager pages - which are composed simultaneously - show the right content.
 */
data class FeedState(
    val availableTopics: List<Topic> = emptyList(),
    val selectedTopic: Topic = Topic.All,
    val feeds: Map<Topic, TopicFeed> = emptyMap(),
    val pendingBookmarkAction: PendingBookmarkAction? = null,
    val pendingBookmarkToggles: Set<String> = emptySet(),
    /** Whether a pull-to-refresh of the selected topic is in flight. */
    val isRefreshing: Boolean = false,
) {
    /** The feed for the currently selected topic (empty if not yet loaded). */
    val currentFeed: TopicFeed get() = feeds[selectedTopic] ?: TopicFeed()
}

data class TopicFeed(
    val posts: List<Post> = emptyList(),
    val currentIndex: Int = 0,
    val nextCursor: String? = null,
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: FeedError? = null,
)

data class PendingBookmarkAction(val postId: String)

enum class FeedError { Network, Unknown }
