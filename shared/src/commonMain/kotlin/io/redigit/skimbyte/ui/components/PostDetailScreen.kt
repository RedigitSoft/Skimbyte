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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
 * Full-page view of a single [Post]. Unlike [PostCard], its markdown body is not height-capped -
 * the whole point of a dedicated detail page is that content isn't truncated.
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
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                // Unlike PostCard's heightIn(max = 240.dp), the detail page lets the markdown body
                // grow to its full height - the Column it's in owns the screen's only scroll, so
                // there's no risk of the renderer's internal layout collapsing sibling content.
                Markdown(content = post.summaryMarkdown)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { onReadSourceClick(post.sourceUrl) }) {
                        Text("View source")
                    }
                }
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
