package io.redigit.skimbyte.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

@Composable
fun SearchScreen(
    posts: List<Post>,
    pendingBookmarkToggles: Set<String>,
    onBookmarkClick: (String) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onSourceClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(posts, query) {
        if (query.isBlank()) emptyList() else posts.filter { it.title.contains(query, ignoreCase = true) }
    }
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search titles") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
        }
        when {
            query.isBlank() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type to search your loaded posts.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> StackedCardBrowser(
                items = results,
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
                    isBookmarkLoading = post.id in pendingBookmarkToggles,
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onReadSourceClick = onReadSourceClick,
                    onSourceClick = onSourceClick,
                    modifier = Modifier.fillMaxSize(),
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

@Preview
@Composable
private fun SearchScreenEmptyQueryPreview() {
    SkimbyteTheme {
        SearchScreen(
            posts = previewPosts,
            pendingBookmarkToggles = emptySet(),
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SearchScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SearchScreen(
            posts = previewPosts,
            pendingBookmarkToggles = emptySet(),
            onBookmarkClick = {},
            onReadSourceClick = {},
            onSourceClick = {},
            onBack = {},
        )
    }
}
