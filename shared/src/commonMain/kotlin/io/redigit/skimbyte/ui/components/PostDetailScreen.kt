package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mikepenz.markdown.m3.Markdown
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.estimateReadingMinutes
import kotlin.time.Instant

/**
 * Full-page view of a single [Post], sharing the story-card visual language of [PostCard] (topic
 * pill, reading-time, attributed source, action bar) so navigating between them feels continuous.
 * Unlike [PostCard], its markdown body is not height-capped - the whole point of a dedicated detail
 * page is that content isn't truncated. When [sharedTransition] is supplied, the hero image and the
 * headline animate in from their counterparts on the originating card.
 *
 * Deliberately does not reuse [PostCard] internally: embedding a tappable card inside the screen
 * it navigates *to* would make the card clickable into itself.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostDetailScreen(
    post: Post,
    isBookmarkLoading: Boolean,
    onBookmarkClick: () -> Unit,
    onReadSourceClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                expandedHeight = 48.dp,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            val imageModifier = if (sharedTransition != null) {
                with(sharedTransition.scope) {
                    Modifier.fillMaxWidth().height(280.dp).sharedElement(
                        rememberSharedContentState(key = "post-image-${post.id}"),
                        sharedTransition.animatedVisibilityScope,
                    )
                }
            } else {
                Modifier.fillMaxWidth().height(280.dp)
            }
            Box(modifier = imageModifier) {
                if (post.imageUrl != null) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        clipToBounds = true,
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TopicPill(post.topic)
                    ReadingTimeLabel(minutes = estimateReadingMinutes(post.summaryMarkdown))
                }
                val titleModifier = if (sharedTransition != null) {
                    with(sharedTransition.scope) {
                        Modifier.sharedBounds(
                            rememberSharedContentState(key = "post-title-${post.id}"),
                            sharedTransition.animatedVisibilityScope,
                        )
                    }
                } else {
                    Modifier
                }
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = titleModifier,
                )
                SourceAttribution(sourceName = post.sourceName, publishedAt = post.publishedAt)
                // Unlike PostCard's heightIn(max = 240.dp), the detail page lets the markdown body
                // grow to its full height - the Column it's in owns the screen's only scroll, so
                // there's no risk of the renderer's internal layout collapsing sibling content.
                Markdown(content = post.summaryMarkdown)
                StoryActionBar(
                    isBookmarked = post.isBookmarked,
                    isBookmarkLoading = isBookmarkLoading,
                    onBookmarkClick = onBookmarkClick,
                    onReadSourceClick = { onReadSourceClick(post.sourceUrl) },
                    readSourceLabel = "View source",
                )
            }
        }
    }
}

@Composable
fun PostDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a post to see its details", style = MaterialTheme.typography.bodyLarge)
    }
}

private val previewPost = Post(
    id = "preview-1",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released with faster Wasm compilation",
    summaryMarkdown = "This release improves **Kotlin/Wasm** compile times and fixes several reflection bugs.",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = false,
)

private val longMarkdownPost = previewPost.copy(
    id = "preview-long",
    title = "A deep dive into everything that changed in this release",
    summaryMarkdown = buildString {
        append("## Overview\n\n")
        repeat(12) { index ->
            append(
                "This is paragraph $index of a very long summary, included to confirm the detail " +
                    "screen's markdown body is not height-capped the way `PostCard`'s is.\n\n",
            )
        }
    },
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostDetailScreenPreview() {
    SkimbyteTheme {
        PostDetailScreen(
            post = previewPost,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostDetailScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        PostDetailScreen(
            post = previewPost.copy(imageUrl = "https://picsum.photos/seed/skimbyte/800/400"),
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostDetailScreenLongMarkdownPreview() {
    SkimbyteTheme {
        PostDetailScreen(
            post = longMarkdownPost,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun PostDetailPlaceholderPreview() {
    SkimbyteTheme {
        PostDetailPlaceholder()
    }
}
