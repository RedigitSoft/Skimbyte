package io.redigit.skimbyte.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The compact-width bottom navigation chrome: a Material 3 Expressive vibrant floating toolbar
 * whose attached FAB expands/collapses it (per [FloatingToolbarDefaults]'s documented pattern),
 * rather than hosting a contextual action menu — [io.redigit.skimbyte.ui.components.PostCard] and
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
            FloatingToolbarDefaults.StandardFloatingActionButton(onClick = { onExpandedChange(!expanded) }) {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if (expanded) "Collapse navigation" else "Expand navigation",
                )
            }
        },
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
        modifier = modifier,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            NavDestinationItem(
                destination = destination,
                selected = destination == selected,
                onClick = { onDestinationSelected(destination) },
            )
        }
    }
}

/**
 * A single nav destination: icon above a label, with a pill indicator behind the icon when
 * [selected] (accented with the primary color), mirroring the Skimbyte floating-dock design.
 */
@Composable
private fun NavDestinationItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        ) {
            Icon(
                destination.icon(),
                contentDescription = destination.name,
                tint = contentColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        Text(
            text = destination.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
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
