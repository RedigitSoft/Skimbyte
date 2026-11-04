package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

/**
 * A compact saved-story row for the bookmarks list: a thumbnail, the headline, and a topic/source
 * line, with an optional remove action. Stateless - taps surface via [onClick] and [onRemoveClick].
 *
 * When [sharedTransition] is supplied, the thumbnail and headline are shared elements keyed to match
 * [PostDetailScreen]'s hero image and title, so opening a bookmark animates continuously into its
 * detail page - the same transition the feed's [PostCard] gets.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SavedPostRow(
    post: Post,
    onClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
    onRemoveClick: (() -> Unit)? = null,
    sharedTransition: SharedElementContext? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        // surfaceContainer, matching PostCard: a saved row is a card, so it sits at elevation
        // level 2 rather than on the screen floor.
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth().clickable { onClick(post) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val thumbBase = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium)
            val thumbModifier = if (sharedTransition != null) {
                with(sharedTransition.scope) {
                    thumbBase.sharedElement(
                        rememberSharedContentState(key = "post-image-${post.id}"),
                        sharedTransition.animatedVisibilityScope,
                    )
                }
            } else {
                thumbBase
            }
            Box(modifier = thumbModifier) {
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = titleModifier,
                )
                Text(
                    text = "${post.topic} · ${post.sourceName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onRemoveClick != null) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Remove bookmark",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private val previewPost = Post(
    id = "preview-1",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released with faster Wasm compilation",
    summaryMarkdown = "Summary",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = true,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SavedPostRowPreview() {
    SkimbyteTheme {
        SavedPostRow(post = previewPost, onClick = {}, onRemoveClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SavedPostRowLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SavedPostRow(post = previewPost, onClick = {}, onRemoveClick = {})
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SavedPostRowLongTitlePreview() {
    SkimbyteTheme {
        SavedPostRow(
            post = previewPost.copy(
                title = "A much longer saved headline that wraps to two lines and then gets ellipsized for the rest",
            ),
            onClick = {},
            onRemoveClick = {},
        )
    }
}
