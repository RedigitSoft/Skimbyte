package io.redigit.skimbyte.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.redigit.skimbyte.posts.Post
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.time.Instant

enum class SourceDetailViewMode { Stack, Grid }

@Composable
fun SourceDetailScreen(
    sourceName: String,
    posts: List<Post>,
    pendingBookmarkToggles: Set<String>,
    onBookmarkClick: (String) -> Unit,
    onReadSourceClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourcePosts = remember(posts, sourceName) { posts.filter { it.sourceName == sourceName } }
    var viewMode by remember { mutableStateOf(SourceDetailViewMode.Stack) }
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                EyebrowText("Source")
                Text(sourceName, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${sourcePosts.size} posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ViewModeToggleButton(
                    icon = Icons.Filled.ViewAgenda,
                    contentDescription = "Card view",
                    selected = viewMode == SourceDetailViewMode.Stack,
                    onClick = { viewMode = SourceDetailViewMode.Stack },
                )
                ViewModeToggleButton(
                    icon = Icons.Filled.GridView,
                    contentDescription = "Grid view",
                    selected = viewMode == SourceDetailViewMode.Grid,
                    onClick = { viewMode = SourceDetailViewMode.Grid },
                )
            }
        }
        when (viewMode) {
            SourceDetailViewMode.Stack -> StackedCardBrowser(
                items = sourcePosts,
                onIndexChanged = {},
                orientation = StackedCardOrientation.Horizontal,
                emptyContent = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No posts from this source yet.", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) { post ->
                PostCard(
                    post = post,
                    isBookmarkLoading = post.id in pendingBookmarkToggles,
                    onBookmarkClick = { onBookmarkClick(post.id) },
                    onReadSourceClick = onReadSourceClick,
                    onSourceClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
            SourceDetailViewMode.Grid -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(sourcePosts, key = { it.id }) { post ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.aspectRatio(1f).clickable { viewMode = SourceDetailViewMode.Stack },
                    ) {
                        if (post.imageUrl != null) {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                clipToBounds = true,
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                Text(post.title, style = MaterialTheme.typography.labelSmall, maxLines = 4)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(40.dp).clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SourceDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a post to see its source", style = MaterialTheme.typography.bodyLarge)
    }
}

private val previewPosts = listOf(
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

@Preview
@Composable
private fun SourceDetailScreenPreview() {
    SkimbyteTheme {
        SourceDetailScreen(
            sourceName = "Kotlin Releases",
            posts = previewPosts,
            pendingBookmarkToggles = emptySet(),
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SourceDetailScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SourceDetailScreen(
            sourceName = "Kotlin Releases",
            posts = previewPosts,
            pendingBookmarkToggles = emptySet(),
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun SourceDetailScreenEmptyPreview() {
    SkimbyteTheme {
        SourceDetailScreen(
            sourceName = "Unknown Source",
            posts = previewPosts,
            pendingBookmarkToggles = emptySet(),
            onBookmarkClick = {},
            onReadSourceClick = {},
            onBack = {},
        )
    }
}
