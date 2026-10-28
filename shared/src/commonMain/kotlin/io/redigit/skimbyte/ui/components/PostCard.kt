package io.redigit.skimbyte.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Bundles the pieces Navigation 3's shared-element transition between [PostCard] and
 * [PostDetailScreen] needs: the enclosing [SharedTransitionScope] and the per-entry
 * [AnimatedVisibilityScope] that scopes the transition's enter/exit animation. Nullable and
 * optional everywhere it's consumed, so `@Preview`s and designsheet call sites - which have
 * neither a `SharedTransitionLayout` nor an `AnimatedVisibilityScope` available - keep compiling
 * unchanged by simply omitting it.
 */
@ExperimentalSharedTransitionApi
data class SharedElementContext(
    val scope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

/**
 * The feed's primary story card: a hero image, a topic pill and reading-time estimate, the
 * headline, a Markdown summary, an attributed source line, and a bookmark / read-source action
 * bar. Stateless - bookmark and read-source intents are pushed up via [onBookmarkClick] and
 * [onReadSourceClick], and tapping the card body surfaces [onCardClick] (used for the shared-element
 * transition into [PostDetailScreen]). When [sharedTransition] is supplied, the hero image and the
 * headline are shared elements that animate into their counterparts on the detail screen.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PostCard(
    post: Post,
    isBookmarkLoading: Boolean,
    onBookmarkClick: () -> Unit,
    onReadSourceClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransition: SharedElementContext? = null,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .clickable { onCardClick(post.id) },
    ) {
        val imageModifier = if (sharedTransition != null) {
            with(sharedTransition.scope) {
                Modifier.fillMaxWidth().height(300.dp).sharedElement(
                    rememberSharedContentState(key = "post-image-${post.id}"),
                    sharedTransition.animatedVisibilityScope,
                )
            }
        } else {
            Modifier.fillMaxWidth().height(300.dp)
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = titleModifier,
            )
            // Bounded height is required here: the renderer's internal layout otherwise expands
            // to consume all remaining space in the Column, collapsing the rows below it to zero
            // height (confirmed by inspecting the composed layout tree).
            Markdown(content = post.summaryMarkdown, modifier = Modifier.heightIn(max = 240.dp))
            SourceAttribution(sourceName = post.sourceName, publishedAt = post.publishedAt)
            StoryActionBar(
                isBookmarked = post.isBookmarked,
                isBookmarkLoading = isBookmarkLoading,
                onBookmarkClick = onBookmarkClick,
                onReadSourceClick = { onReadSourceClick(post.sourceUrl) },
            )
        }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostCardPreview() {
    SkimbyteTheme {
        PostCard(
            post = previewPost,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostCardLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        PostCard(
            post = previewPost,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostCardWithImagePreview() {
    SkimbyteTheme {
        PostCard(
            post = previewPost.copy(imageUrl = "https://picsum.photos/seed/skimbyte/800/400"),
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostCardBookmarkedPreview() {
    SkimbyteTheme {
        PostCard(
            post = previewPost.copy(isBookmarked = true),
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun PostCardLongTitlePreview() {
    SkimbyteTheme {
        PostCard(
            post = previewPost.copy(
                title = "A much longer headline that wraps across multiple lines to check card layout behavior",
                summaryMarkdown = "A longer multi-sentence summary so the reading-time estimate climbs past a single minute. " +
                    List(220) { "word" }.joinToString(" "),
            ),
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}
