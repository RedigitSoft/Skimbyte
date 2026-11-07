package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.PostCard
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

private val longReadPost = shortPost.copy(
    id = "catalog-long-read",
    title = "A deep dive that takes a few minutes to read",
    summaryMarkdown = "A longer multi-paragraph summary that pushes the reading-time estimate past a single minute. " +
        List(240) { "word" }.joinToString(" "),
)

private data class CardVariant(val title: String, val description: String?, val post: Post)

private val variants = listOf(
    CardVariant("Default", "Short headline, no image, not bookmarked.", shortPost),
    CardVariant("Bookmarked", "The filled bookmark marks a saved story.", bookmarkedPost),
    CardVariant("Long headline", "Wrapping across multiple lines to check card layout behavior.", longTitlePost),
    CardVariant("Long read", "A summary long enough to push the reading-time estimate past a minute.", longReadPost),
    CardVariant("With image", "A story image replaces the per-topic placeholder once it loads.", imagePost),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostCardCatalogContent() {
    CatalogPage {
        variants.forEach { variant ->
            CatalogSection(title = variant.title, description = variant.description) {
                PostCard(
                    post = variant.post,
                    isBookmarkLoading = false,
                    onBookmarkClick = {},
                    onReadSourceClick = {},
                    onCardClick = {},
                )
            }
        }
    }
}

@Preview
@Composable
private fun PostCardCatalogPreview() {
    SkimbyteTheme {
        PostCardCatalogContent()
    }
}

@Preview
@Composable
private fun PostCardCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        PostCardCatalogContent()
    }
}
