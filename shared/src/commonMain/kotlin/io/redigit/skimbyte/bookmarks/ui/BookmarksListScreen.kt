package io.redigit.skimbyte.bookmarks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.bookmarks.BookmarksUiState
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.auth.LoginSheet
import kotlin.time.Instant

@Composable
fun BookmarksListScreen(
    uiState: BookmarksUiState,
    onPostClick: (Post) -> Unit,
    onLoginSucceeded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Bookmarks") }) },
    ) { padding ->
        when {
            uiState.isLoggedOut -> {
                var showLogin by remember { mutableStateOf(true) }
                if (showLogin) {
                    LoginSheet(
                        onDismiss = { showLogin = false },
                        onLoginSuccess = {
                            showLogin = false
                            onLoginSucceeded()
                        },
                    )
                }
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Log in to see your bookmarks", style = MaterialTheme.typography.bodyLarge)
                }
            }
            uiState.isLoadingInitial -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            uiState.posts.isEmpty() -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No bookmarks yet.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.posts) { post ->
                    ListItem(
                        headlineContent = { Text(post.title) },
                        supportingContent = { Text("${post.topic} · ${post.sourceName}") },
                        modifier = Modifier.clickable { onPostClick(post) },
                    )
                    HorizontalDivider()
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

@Preview
@Composable
private fun BookmarksListScreenPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            uiState = BookmarksUiState(posts = listOf(previewPost)),
            onPostClick = {},
            onLoginSucceeded = {},
        )
    }
}

@Preview
@Composable
private fun BookmarksListScreenLoggedOutPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            uiState = BookmarksUiState(isLoggedOut = true),
            onPostClick = {},
            onLoginSucceeded = {},
        )
    }
}

@Preview
@Composable
private fun BookmarksListScreenEmptyPreview() {
    SkimbyteTheme {
        BookmarksListScreen(
            uiState = BookmarksUiState(),
            onPostClick = {},
            onLoginSucceeded = {},
        )
    }
}
