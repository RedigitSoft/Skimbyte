package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.SourceDetailScreen
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

private val catalogPosts = listOf(
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

@Composable
fun SourceDetailScreenCatalogContent() {
    SourceDetailScreen(
        sourceName = "Kotlin Releases",
        posts = catalogPosts,
        pendingBookmarkToggles = emptySet(),
        onBookmarkClick = {},
        onReadSourceClick = {},
        onBack = {},
        modifier = Modifier.height(600.dp),
    )
}

@Preview
@Composable
private fun SourceDetailScreenCatalogPreview() {
    SkimbyteTheme {
        SourceDetailScreenCatalogContent()
    }
}
