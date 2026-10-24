package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Topic

sealed interface FeedIntent {
    data class TopicSelected(val topic: Topic) : FeedIntent
    data object ViewModeToggled : FeedIntent
    data class PageChanged(val index: Int) : FeedIntent
    data class BookmarkTapped(val postId: String) : FeedIntent
    data object LoginSucceeded : FeedIntent
    data object LoginDismissed : FeedIntent
}
