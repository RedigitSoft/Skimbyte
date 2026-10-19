package io.redigit.skimbyte.feed.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.SearchUiState
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.openUrl
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search titles") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when {
            uiState.query.isBlank() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type to search your loaded posts.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> StackedCardBrowser(
                items = uiState.results,
                onIndexChanged = {},
                emptyContent = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matches.", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) { post ->
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in uiState.pendingBookmarkToggles,
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onReadSourceClick = ::openUrl,
                    onCardClick = onCardClick,
                    modifier = Modifier.fillMaxSize(),
                    sharedTransition = sharedTransition,
                )
            }
        }
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
private fun SearchScreenEmptyQueryPreview() {
    SkimbyteTheme {
        SearchScreen(
            uiState = SearchUiState(),
            onQueryChanged = {},
            onBookmarkClick = {},
            onCardClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SearchScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SearchScreen(
            uiState = SearchUiState(query = "kotlin", results = previewPosts),
            onQueryChanged = {},
            onBookmarkClick = {},
            onCardClick = {},
        )
    }
}
