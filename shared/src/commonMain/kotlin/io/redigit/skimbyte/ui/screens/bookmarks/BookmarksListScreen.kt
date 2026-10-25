package io.redigit.skimbyte.ui.screens.bookmarks

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.SavedPostRow
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarksListScreen(
    state: BookmarksState,
    onPostClick: (Post) -> Unit,
    onIntent: (BookmarksIntent) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Bookmarks") }, expandedHeight = 48.dp) },
    ) { padding ->
        when {
            state.isLoggedOut -> {
                var showLogin by remember { mutableStateOf(true) }
                if (showLogin) {
                    LoginSheet(
                        onDismiss = { showLogin = false },
                        onLoginSuccess = {
                            showLogin = false
                            onIntent(BookmarksIntent.LoginSucceeded)
                        },
                    )
                }
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Log in to see your bookmarks", style = MaterialTheme.typography.bodyLarge)
                }
            }
            state.isLoadingInitial -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(BookmarksIntent.Refresh) },
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) {
                if (state.posts.isEmpty()) {
                    // A scroll container so the pull-to-refresh gesture still registers when empty.
                    Box(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No bookmarks yet.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            SavedPostRow(
                                post = post,
                                onClick = onPostClick,
                                onRemoveClick = { onIntent(BookmarksIntent.Unbookmark(post.id)) },
                                sharedTransition = sharedTransition,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val previewPost = Post(
    id = "1",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released",
    summaryMarkdown = "Summary",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = true,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BookmarksListScreenPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            state = BookmarksState(posts = listOf(previewPost)),
            onPostClick = {},
            onIntent = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BookmarksListScreenLoggedOutPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            state = BookmarksState(isLoggedOut = true),
            onPostClick = {},
            onIntent = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun BookmarksListScreenEmptyPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            state = BookmarksState(),
            onPostClick = {},
            onIntent = {},
        )
    }
}
