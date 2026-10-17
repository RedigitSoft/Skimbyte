package io.redigit.skimbyte.nav.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isBookmarked: Boolean,
    isBookmarkLoading: Boolean,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenSourceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(checked = expanded, onCheckedChange = onExpandedChange) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.MoreVert,
                    contentDescription = if (expanded) "Close post actions" else "Post actions",
                )
            }
        },
        modifier = modifier,
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                if (!isBookmarkLoading) {
                    onBookmarkClick()
                    onExpandedChange(false)
                }
            },
            icon = {
                if (isBookmarkLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else if (isBookmarked) {
                    Icon(Icons.Filled.Bookmark, contentDescription = null)
                } else {
                    Icon(Icons.Outlined.BookmarkBorder, contentDescription = null)
                }
            },
            text = { Text(if (isBookmarked) "Remove bookmark" else "Bookmark") },
        )
        FloatingActionButtonMenuItem(
            onClick = { onShareClick(); onExpandedChange(false) },
            icon = { Icon(Icons.Filled.Share, contentDescription = null) },
            text = { Text("Share") },
        )
        FloatingActionButtonMenuItem(
            onClick = { onOpenSourceClick(); onExpandedChange(false) },
            icon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
            text = { Text("Open source") },
        )
    }
}

@Preview
@Composable
private fun SkimbyteFabMenuCollapsedPreview() {
    SkimbyteTheme {
        SkimbyteFabMenu(
            expanded = false,
            onExpandedChange = {},
            isBookmarked = false,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onShareClick = {},
            onOpenSourceClick = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteFabMenuExpandedPreview() {
    SkimbyteTheme {
        var expanded by remember { mutableStateOf(true) }
        SkimbyteFabMenu(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            isBookmarked = true,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onShareClick = {},
            onOpenSourceClick = {},
        )
    }
}
