package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.PostDetailScreen
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

private val catalogPost = Post(
    id = "catalog-post-detail",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released",
    summaryMarkdown = "Faster **Wasm** compilation and reflection fixes.",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = "https://picsum.photos/seed/skimbyte/800/400",
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = false,
)

@Composable
fun PostDetailScreenCatalogContent() {
    PostDetailScreen(
        post = catalogPost,
        isBookmarkLoading = false,
        onBookmarkClick = {},
        onReadSourceClick = {},
        onBack = {},
        modifier = Modifier.height(700.dp),
    )
}

@Preview
@Composable
private fun PostDetailScreenCatalogPreview() {
    SkimbyteTheme {
        PostDetailScreenCatalogContent()
    }
}
