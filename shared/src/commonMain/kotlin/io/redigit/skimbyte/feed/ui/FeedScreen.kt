package io.redigit.skimbyte.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.auth.ui.LoginSheet
import io.redigit.skimbyte.feed.FeedUiState
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.posts.Topic
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onTopicSelected: (Topic) -> Unit,
    onPageChanged: (Int) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onLoginSucceeded: () -> Unit,
    onLoginDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.pendingBookmarkAction != null) {
        LoginSheet(onDismiss = onLoginDismissed, onLoginSuccess = onLoginSucceeded)
    }
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            EyebrowText("Feed")
            Text("For You", style = MaterialTheme.typography.headlineMedium)
        }
        TopicFilterRow(selectedTopic = uiState.selectedTopic, onTopicSelected = onTopicSelected)
        when {
            uiState.isLoadingInitial -> LoadingState(modifier = Modifier.weight(1f))
            uiState.error != null && uiState.posts.isEmpty() -> ErrorState(modifier = Modifier.weight(1f))
            else -> StackedCardBrowser(
                items = uiState.posts,
                onIndexChanged = onPageChanged,
                resetSignal = uiState.selectedTopic,
                emptyContent = { EmptyState(modifier = Modifier.fillMaxSize()) },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) { post ->
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in uiState.pendingBookmarkToggles,
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onReadSourceClick = onReadSourceClick,
                    onSourceClick = onSourceClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Something went wrong.", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No posts yet.", style = MaterialTheme.typography.bodyLarge)
    }
}

private val previewPosts = listOf(
    Post(
        id = "1",
        topic = "Kotlin",
        title = "Kotlin 2.4.21 released",
        summaryMarkdown = "Faster Wasm compilation and reflection fixes.",
        sourceName = "Kotlin Releases",
        sourceUrl = "https://example.com",
        imageUrl = null,
        digestDate = "2026-10-13",
        publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
        isBookmarked = false,
    ),
)

@Preview
@Composable
private fun FeedScreenPreview() {
    SkimbyteTheme {
        FeedScreen(
            uiState = FeedUiState(posts = previewPosts),
            onTopicSelected = {},
            onPageChanged = {},
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@Preview
@Composable
private fun FeedScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        FeedScreen(
            uiState = FeedUiState(posts = previewPosts),
            onTopicSelected = {},
            onPageChanged = {},
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@Preview
@Composable
private fun FeedScreenLoadingPreview() {
    SkimbyteTheme {
        FeedScreen(
            uiState = FeedUiState(isLoadingInitial = true),
            onTopicSelected = {},
            onPageChanged = {},
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@Preview
@Composable
private fun FeedScreenEmptyPreview() {
    SkimbyteTheme {
        FeedScreen(
            uiState = FeedUiState(posts = emptyList()),
            onTopicSelected = {},
            onPageChanged = {},
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}
