package io.redigit.skimbyte.ui.screens.bookmarks

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.SavedPostRow
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.ui.components.SignedOutPrompt
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlinx.coroutines.launch
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
                // The sheet opens on demand from the prompt's button (the Account tab already
                // auto-prompts once per session; nagging again on every Bookmarks visit was worse
                // than a clear call to action).
                var showLogin by remember { mutableStateOf(false) }
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
                    SignedOutPrompt(
                        icon = Icons.Filled.Bookmark,
                        title = "Your bookmarks live here",
                        message = "Sign in to save stories from the feed and pick them up on any device.",
                        onSignInClick = { showLogin = true },
                    )
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
                    var pendingDelete by remember { mutableStateOf<Post?>(null) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            DismissibleBookmarkRow(
                                post = post,
                                onPostClick = onPostClick,
                                onRemoveClick = { onIntent(BookmarksIntent.Unbookmark(post.id)) },
                                onDeleteRequested = { pendingDelete = post },
                                sharedTransition = sharedTransition,
                            )
                        }
                    }
                    pendingDelete?.let { post ->
                        AlertDialog(
                            onDismissRequest = { pendingDelete = null },
                            title = { Text("Delete bookmark?") },
                            text = { Text("Do you want to delete “${post.title}” from your bookmarks?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        onIntent(BookmarksIntent.Unbookmark(post.id))
                                        pendingDelete = null
                                    },
                                ) { Text("Delete") }
                            },
                            dismissButton = {
                                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * A [SavedPostRow] that can be swiped left (end-to-start) to request deletion, revealing a delete
 * affordance behind it. The swipe never dismisses directly: [onDeleteRequested] opens a
 * confirmation dialog and the row settles back, so an accidental flick can't destroy data.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DismissibleBookmarkRow(
    post: Post,
    onPostClick: (Post) -> Unit,
    onRemoveClick: () -> Unit,
    onDeleteRequested: () -> Unit,
    sharedTransition: SharedElementContext?,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        // The swipe never deletes directly: request confirmation and settle the row back, so an
        // accidental flick can't destroy data.
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDeleteRequested()
            scope.launch { dismissState.reset() }
        },
        modifier = modifier,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete bookmark",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp),
                )
            }
        },
    ) {
        SavedPostRow(
            post = post,
            onClick = onPostClick,
            onRemoveClick = onRemoveClick,
            sharedTransition = sharedTransition,
        )
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
