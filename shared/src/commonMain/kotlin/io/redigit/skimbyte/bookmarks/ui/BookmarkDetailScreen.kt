package io.redigit.skimbyte.bookmarks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.feed.ui.PostCard
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.openUrl
import kotlin.time.Instant

@Composable
fun BookmarkDetailScreen(post: Post, onUnbookmark: (String) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(post.title) }) },
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PostCard(
                post = post,
                isBookmarkLoading = false,
                onBookmarkClick = { onUnbookmark(post.id) },
                onReadSourceClick = ::openUrl,
                onSourceClick = {},
            )
        }
    }
}

@Composable
fun BookmarkDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a bookmark from the list", style = MaterialTheme.typography.bodyLarge)
    }
}

private val previewPost = Post(
    id = "1",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released",
    summaryMarkdown = "Faster **Wasm** compilation and reflection fixes.",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = true,
)

@Preview
@Composable
private fun BookmarkDetailScreenPreview() {
    SkimbyteTheme {
        BookmarkDetailScreen(post = previewPost, onUnbookmark = {})
    }
}

@Preview
@Composable
private fun BookmarkDetailPlaceholderPreview() {
    SkimbyteTheme {
        BookmarkDetailPlaceholder()
    }
}
