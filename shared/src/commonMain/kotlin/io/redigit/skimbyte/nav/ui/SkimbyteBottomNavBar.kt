package io.redigit.skimbyte.nav.ui

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The compact-width bottom navigation chrome: a Material 3 Expressive vibrant floating toolbar
 * whose attached FAB expands/collapses it (per [FloatingToolbarDefaults]'s documented pattern),
 * rather than hosting a contextual action menu — [io.redigit.skimbyte.feed.ui.PostCard] and
 * `PostDetailScreen` already expose bookmark/share/source actions directly, so there's nothing
 * else for a FAB here to do. Pair with [Modifier] from
 * `FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll` on the scrollable content above
 * this toolbar so it collapses on scroll and expands when scrolling back up.
 */
@Composable
fun SkimbyteBottomNavBar(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = expanded,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = { onExpandedChange(!expanded) }) {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (expanded) "Collapse navigation" else "Expand navigation",
                )
            }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            IconButton(
                onClick = { onDestinationSelected(destination) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
            ) {
                Icon(destination.icon(), contentDescription = destination.name)
            }
        }
    }
}

internal fun TopLevelDestination.icon() = when (this) {
    TopLevelDestination.Feed -> Icons.Filled.Home
    TopLevelDestination.Bookmarks -> Icons.Filled.Bookmark
    TopLevelDestination.Search -> Icons.Filled.Search
    TopLevelDestination.Account -> Icons.Filled.Person
}

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
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
            expanded = true,
            onExpandedChange = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarSearchPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Search,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarAccountPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Account,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarCollapsedPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            expanded = false,
            onExpandedChange = {},
        )
    }
}
