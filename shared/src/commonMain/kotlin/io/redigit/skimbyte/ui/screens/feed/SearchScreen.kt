package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.PostCard
import io.redigit.skimbyte.ui.components.SharedElementContext
import io.redigit.skimbyte.ui.components.StackedCardBrowser
import io.redigit.skimbyte.util.openUrl
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    // statusBarsPadding: the shell no longer consumes the top inset (see MainShell).
    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        SearchField(
            query = state.query,
            onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
            onClear = { onIntent(SearchIntent.QueryChanged("")) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when {
            state.query.isBlank() -> HintState(
                text = "Search your loaded briefs by title.",
                modifier = Modifier.weight(1f),
            )
            state.results.isEmpty() -> HintState(
                text = "No matches for \"${state.query}\".",
                modifier = Modifier.weight(1f),
            )
            else -> {
                Text(
                    text = if (state.results.size == 1) "1 match" else "${state.results.size} matches",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                StackedCardBrowser(
                    items = state.results,
                    onIndexChanged = {},
                    emptyContent = {},
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                ) { post ->
                    PostCard(
                        post = post,
                        isBookmarkLoading = post.id in state.pendingBookmarkToggles,
                        onBookmarkClick = { onIntent(SearchIntent.BookmarkClicked(post.id)) },
                        onReadSourceClick = ::openUrl,
                        onCardClick = onCardClick,
                        modifier = Modifier.fillMaxSize(),
                        sharedTransition = sharedTransition,
                    )
                }
            }
        }
    }
}

/** A pill-shaped search field with a leading search icon and a clear action when non-empty. */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search developer briefs") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier,
    )
}

@Composable
private fun HintState(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        SearchScreen(state = SearchState(), onIntent = {}, onCardClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SearchScreenNoMatchesPreview() {
    SkimbyteTheme {
        SearchScreen(state = SearchState(query = "zzz", results = emptyList()), onIntent = {}, onCardClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SearchScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SearchScreen(state = SearchState(query = "kotlin", results = previewPosts), onIntent = {}, onCardClick = {})
    }
}
