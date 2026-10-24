package io.redigit.skimbyte.feed.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.FeedUiState
import io.redigit.skimbyte.feed.FeedViewMode
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onTopicSelected: (Topic) -> Unit,
    onPageChanged: (Int) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onViewModeToggled: () -> Unit,
    onLoginSucceeded: () -> Unit,
    onLoginDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    if (uiState.pendingBookmarkAction != null) {
        LoginSheet(onDismiss = onLoginDismissed, onLoginSuccess = onLoginSucceeded)
    }
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                EyebrowText("Feed")
                Text("For You", style = MaterialTheme.typography.headlineMedium)
            }
            FeedViewModeToggle(viewMode = uiState.viewMode, onClick = onViewModeToggled)
        }
        TopicFilterRow(
            topics = uiState.availableTopics,
            selectedTopic = uiState.selectedTopic,
            onTopicSelected = onTopicSelected,
        )
        when {
            uiState.isLoadingInitial -> LoadingState(modifier = Modifier.weight(1f))
            uiState.error != null && uiState.posts.isEmpty() -> ErrorState(modifier = Modifier.weight(1f))
            uiState.viewMode == FeedViewMode.List -> LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        isBookmarkLoading = post.id in uiState.pendingBookmarkToggles,
                        onBookmarkClick = { onBookmarkClick(post.id) },
                        onReadSourceClick = onReadSourceClick,
                        onCardClick = onCardClick,
                        modifier = Modifier.fillMaxWidth(),
                        sharedTransition = sharedTransition,
                    )
                }
            }
            else -> StackedCardBrowser(
                items = uiState.posts,
                onIndexChanged = onPageChanged,
                orientation = StackedCardOrientation.Horizontal,
                resetSignal = uiState.selectedTopic,
                emptyContent = { EmptyState(modifier = Modifier.fillMaxSize()) },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) { post ->
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in uiState.pendingBookmarkToggles,
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onReadSourceClick = onReadSourceClick,
                    onCardClick = onCardClick,
                    modifier = Modifier.fillMaxSize(),
                    sharedTransition = sharedTransition,
                )
            }
        }
    }
}

@Composable
private fun FeedViewModeToggle(viewMode: FeedViewMode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon: ImageVector = if (viewMode == FeedViewMode.Stack) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.ViewAgenda
    val contentDescription = if (viewMode == FeedViewMode.Stack) "Switch to list view" else "Switch to card view"
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp),
        )
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

@OptIn(ExperimentalSharedTransitionApi::class)
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
            onCardClick = {},
            onViewModeToggled = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
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
            onCardClick = {},
            onViewModeToggled = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
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
            onCardClick = {},
            onViewModeToggled = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
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
            onCardClick = {},
            onViewModeToggled = {},
            onLoginSucceeded = {},
            onLoginDismissed = {},
        )
    }
}
