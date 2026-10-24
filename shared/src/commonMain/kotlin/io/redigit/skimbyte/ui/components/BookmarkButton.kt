package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, enabled = !isLoading, modifier = modifier.testTag("bookmarkButton")) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else if (isBookmarked) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Remove bookmark")
        } else {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
        }
    }
}

@Preview
@Composable
private fun BookmarkButtonIdlePreview() {
    SkimbyteTheme {
        BookmarkButton(isBookmarked = false, isLoading = false, onClick = {})
    }
}

@Preview
@Composable
private fun BookmarkButtonBookmarkedPreview() {
    SkimbyteTheme {
        BookmarkButton(isBookmarked = true, isLoading = false, onClick = {})
    }
}

@Preview
@Composable
private fun BookmarkButtonLoadingPreview() {
    SkimbyteTheme {
        BookmarkButton(isBookmarked = false, isLoading = true, onClick = {})
    }
}
