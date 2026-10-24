package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic

data class FeedState(
    val selectedTopic: Topic = Topic.All,
    val viewMode: FeedViewMode = FeedViewMode.Stack,
    val availableTopics: List<Topic> = emptyList(),
    val posts: List<Post> = emptyList(),
    val currentIndex: Int = 0,
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: String? = null,
    val error: FeedError? = null,
    val pendingBookmarkAction: PendingBookmarkAction? = null,
    val pendingBookmarkToggles: Set<String> = emptySet(),
)

data class PendingBookmarkAction(val postId: String)

enum class FeedError { Network, Unknown }
