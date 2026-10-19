package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.PostCard
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

private val shortPost = Post(
    id = "catalog-short",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released",
    summaryMarkdown = "Faster **Wasm** compilation and reflection fixes.",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = false,
)

private val longTitlePost = shortPost.copy(
    id = "catalog-long-title",
    title = "A much longer headline that wraps across multiple lines to check card layout behavior",
)

private val bookmarkedPost = shortPost.copy(id = "catalog-bookmarked", isBookmarked = true)

private val imagePost = shortPost.copy(id = "catalog-image", imageUrl = "https://picsum.photos/seed/skimbyte/800/400")

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostCardCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PostCard(post = shortPost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
        PostCard(post = bookmarkedPost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
        PostCard(post = longTitlePost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
        PostCard(post = imagePost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
    }
}

@Preview
@Composable
private fun PostCardCatalogPreview() {
    SkimbyteTheme {
        PostCardCatalogContent()
    }
}
