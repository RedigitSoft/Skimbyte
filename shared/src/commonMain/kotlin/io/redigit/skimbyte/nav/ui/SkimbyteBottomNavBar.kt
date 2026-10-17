package io.redigit.skimbyte.nav.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteBottomNavBar(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    onSearchClick: (() -> Unit)?,
    fabMenu: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.padding(16.dp),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            IconButton(onClick = { onDestinationSelected(destination) }) {
                Icon(destination.icon(), contentDescription = destination.name)
            }
        }
        if (onSearchClick != null) {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
        fabMenu?.invoke()
    }
}

private fun TopLevelDestination.icon() = when (this) {
    TopLevelDestination.Feed -> Icons.Filled.Home
    TopLevelDestination.Bookmarks -> Icons.Filled.Bookmark
    TopLevelDestination.Account -> Icons.Filled.Person
}

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            onSearchClick = {},
            fabMenu = null,
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarBookmarksPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Bookmarks,
            onDestinationSelected = {},
            onSearchClick = null,
            fabMenu = null,
        )
    }
}
