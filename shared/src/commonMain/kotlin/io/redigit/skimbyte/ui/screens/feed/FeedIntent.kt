package io.redigit.skimbyte.ui.screens.feed

import io.redigit.skimbyte.data.models.Topic

sealed interface FeedIntent {
    /** The active topic changed - by tapping a filter chip or settling the horizontal pager. */
    data class TopicSelected(val topic: Topic) : FeedIntent

    /** The reader moved to vertical card [index] within the selected topic. */
    data class PageChanged(val index: Int) : FeedIntent
    data class BookmarkTapped(val postId: String) : FeedIntent
    data object LoginSucceeded : FeedIntent
    data object LoginDismissed : FeedIntent

    /** Pull-to-refresh on the current topic: reloads its first page from scratch. */
    data object Refresh : FeedIntent
}
