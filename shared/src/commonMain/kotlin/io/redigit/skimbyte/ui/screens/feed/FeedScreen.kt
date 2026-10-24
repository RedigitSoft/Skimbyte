package io.redigit.skimbyte.ui.screens.feed

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
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.PostCard
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.ui.components.StackedCardBrowser
import io.redigit.skimbyte.ui.components.StackedCardOrientation
import io.redigit.skimbyte.ui.components.TopicFilterRow
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedScreen(
    state: FeedState,
    onIntent: (FeedIntent) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    if (state.pendingBookmarkAction != null) {
        LoginSheet(
            onDismiss = { onIntent(FeedIntent.LoginDismissed) },
            onLoginSuccess = { onIntent(FeedIntent.LoginSucceeded) },
        )
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
            FeedViewModeToggle(viewMode = state.viewMode, onClick = { onIntent(FeedIntent.ViewModeToggled) })
        }
        TopicFilterRow(
            topics = state.availableTopics,
            selectedTopic = state.selectedTopic,
            onTopicSelected = { onIntent(FeedIntent.TopicSelected(it)) },
        )
        when {
            state.isLoadingInitial -> LoadingState(modifier = Modifier.weight(1f))
            state.error != null && state.posts.isEmpty() -> ErrorState(modifier = Modifier.weight(1f))
            state.viewMode == FeedViewMode.List -> LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        isBookmarkLoading = post.id in state.pendingBookmarkToggles,
                        onBookmarkClick = { onIntent(FeedIntent.BookmarkTapped(post.id)) },
                        onReadSourceClick = onReadSourceClick,
                        onCardClick = onCardClick,
                        modifier = Modifier.fillMaxWidth(),
                        sharedTransition = sharedTransition,
                    )
                }
            }
            else -> StackedCardBrowser(
                items = state.posts,
                onIndexChanged = { onIntent(FeedIntent.PageChanged(it)) },
                orientation = StackedCardOrientation.Horizontal,
                resetSignal = state.selectedTopic,
                emptyContent = { EmptyState(modifier = Modifier.fillMaxSize()) },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) { post ->
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in state.pendingBookmarkToggles,
                    onBookmarkClick = { onIntent(FeedIntent.BookmarkTapped(post.id)) },
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
        FeedScreen(state = FeedState(posts = previewPosts), onIntent = {}, onReadSourceClick = {}, onCardClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun FeedScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        FeedScreen(state = FeedState(posts = previewPosts), onIntent = {}, onReadSourceClick = {}, onCardClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun FeedScreenLoadingPreview() {
    SkimbyteTheme {
        FeedScreen(state = FeedState(isLoadingInitial = true), onIntent = {}, onReadSourceClick = {}, onCardClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun FeedScreenEmptyPreview() {
    SkimbyteTheme {
        FeedScreen(state = FeedState(posts = emptyList()), onIntent = {}, onReadSourceClick = {}, onCardClick = {})
    }
}
