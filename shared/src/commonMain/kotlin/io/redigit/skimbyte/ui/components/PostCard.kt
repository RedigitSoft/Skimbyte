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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mikepenz.markdown.m3.Markdown
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.toDisplayDate
import kotlin.time.Instant

/**
 * Bundles the pieces Navigation 3's shared-element transition between [PostCard] and
 * `PostDetailScreen` needs: the enclosing [SharedTransitionScope] and the per-entry
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
        Column {
            val imageModifier = if (sharedTransition != null) {
                with(sharedTransition.scope) {
                    Modifier.fillMaxWidth().height(220.dp).sharedElement(
                        rememberSharedContentState(key = "post-image-${post.id}"),
                        sharedTransition.animatedVisibilityScope,
                    )
                }
            } else {
                Modifier.fillMaxWidth().height(220.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                ) {
                    EyebrowText("${post.topic} · ${post.publishedAt.toDisplayDate()}")
                    Text(
                        text = post.sourceName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape),
                ) {
                    BookmarkButton(
                        isBookmarked = post.isBookmarked,
                        isLoading = isBookmarkLoading,
                        onClick = onBookmarkClick,
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                // Bounded height is required here: the renderer's internal layout otherwise expands
                // to consume all remaining space in the Column, collapsing the rows below it to zero
                // height (confirmed by inspecting the composed layout tree).
                Markdown(content = post.summaryMarkdown, modifier = Modifier.heightIn(max = 240.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { onReadSourceClick(post.sourceUrl) }) {
                        Text("Read full source")
                    }
                }
            }
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
            ),
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onReadSourceClick = {},
            onCardClick = {},
        )
    }
}
