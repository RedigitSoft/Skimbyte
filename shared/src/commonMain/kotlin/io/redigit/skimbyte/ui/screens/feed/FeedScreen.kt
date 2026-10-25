package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.DailyProgressBar
import io.redigit.skimbyte.ui.components.PostCard
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.ui.components.TopicFilterRow
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            EyebrowText("Feed")
            Text("For You", style = MaterialTheme.typography.titleLarge)
        }
        TopicFilterRow(
            topics = state.availableTopics,
            selectedTopic = state.selectedTopic,
            onTopicSelected = { onIntent(FeedIntent.TopicSelected(it)) },
        )
        val currentFeed = state.currentFeed
        if (currentFeed.posts.isNotEmpty()) {
            val brief = dailyBrief(currentFeed.posts, currentFeed.currentIndex)
            DailyProgressBar(
                read = brief.read,
                total = brief.total,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(FeedIntent.Refresh) },
            modifier = Modifier.weight(1f),
        ) {
            TopicFeedPager(
                state = state,
                onIntent = onIntent,
                onReadSourceClick = onReadSourceClick,
                onCardClick = onCardClick,
                sharedTransition = sharedTransition,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Horizontal pager across topics; each page is that topic's vertical card pager. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TopicFeedPager(
    state: FeedState,
    onIntent: (FeedIntent) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    sharedTransition: SharedElementContext?,
    modifier: Modifier = Modifier,
) {
    val topics = state.availableTopics
    if (topics.isEmpty()) return
    val selectedPage = topics.indexOf(state.selectedTopic).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = selectedPage) { topics.size }

    // Tapping a filter chip (external selection) animates the pager to that topic.
    LaunchedEffect(state.selectedTopic) {
        val target = topics.indexOf(state.selectedTopic)
        if (target >= 0 && target != pagerState.currentPage) pagerState.animateScrollToPage(target)
    }
    // Swiping the pager to rest on a new topic reports it back up.
    LaunchedEffect(pagerState.settledPage) {
        topics.getOrNull(pagerState.settledPage)?.let {
            if (it != state.selectedTopic) onIntent(FeedIntent.TopicSelected(it))
        }
    }

    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize()) { page ->
        val topic = topics[page]
        TopicPostsPager(
            feed = state.feeds[topic] ?: TopicFeed(),
            isActive = topic == state.selectedTopic,
            onPageChanged = { onIntent(FeedIntent.PageChanged(it)) },
            pendingBookmarkToggles = state.pendingBookmarkToggles,
            onBookmarkClick = { onIntent(FeedIntent.BookmarkTapped(it)) },
            onReadSourceClick = onReadSourceClick,
            onCardClick = onCardClick,
            sharedTransition = sharedTransition,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        )
    }
}

/** A single topic's posts as a vertical, one-card-at-a-time pager. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TopicPostsPager(
    feed: TopicFeed,
    isActive: Boolean,
    onPageChanged: (Int) -> Unit,
    pendingBookmarkToggles: Set<String>,
    onBookmarkClick: (String) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    sharedTransition: SharedElementContext?,
    modifier: Modifier = Modifier,
) {
    when {
        feed.isLoadingInitial && feed.posts.isEmpty() -> LoadingState(modifier)
        feed.error != null && feed.posts.isEmpty() -> ErrorState(modifier)
        feed.posts.isEmpty() -> EmptyState(modifier)
        else -> {
            val postsState = rememberPagerState(
                initialPage = feed.currentIndex.coerceIn(0, feed.posts.lastIndex),
            ) { feed.posts.size }
            // Only the visible topic reports its scroll position, so off-screen pages don't
            // overwrite the selected topic's index.
            if (isActive) {
                LaunchedEffect(postsState.currentPage) { onPageChanged(postsState.currentPage) }
            }
            VerticalPager(
                state = postsState,
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                pageSpacing = 12.dp,
                modifier = modifier.fillMaxSize(),
            ) { page ->
                val post = feed.posts[page]
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in pendingBookmarkToggles,
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

private fun previewState(feed: TopicFeed) = FeedState(
    availableTopics = listOf(Topic.All, Topic.Kotlin, Topic.Android),
    selectedTopic = Topic.All,
    feeds = mapOf(Topic.All to feed),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedScreenHarness(feed: TopicFeed, dark: Boolean = true) {
    SkimbyteTheme(darkTheme = dark) {
        FeedScreen(state = previewState(feed), onIntent = {}, onReadSourceClick = {}, onCardClick = {})
    }
}

@Preview
@Composable
private fun FeedScreenPreview() = FeedScreenHarness(TopicFeed(posts = previewPosts))

@Preview
@Composable
private fun FeedScreenLightPreview() = FeedScreenHarness(TopicFeed(posts = previewPosts), dark = false)

@Preview
@Composable
private fun FeedScreenLoadingPreview() = FeedScreenHarness(TopicFeed(isLoadingInitial = true))

@Preview
@Composable
private fun FeedScreenEmptyPreview() = FeedScreenHarness(TopicFeed(posts = emptyList()))
