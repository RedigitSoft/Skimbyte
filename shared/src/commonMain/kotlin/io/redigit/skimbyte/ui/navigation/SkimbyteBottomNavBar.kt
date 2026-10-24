package io.redigit.skimbyte.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The compact-width bottom navigation chrome: a Material 3 floating pill dock. The selected
 * destination expands into an accent-outlined pill showing its icon and label inline, while the
 * others stay icon-over-label and muted. [expanded] drives the toolbar's collapse-on-scroll (wired
 * in `MainShell` via `FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll`).
 */
@Composable
fun SkimbyteBottomNavBar(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = expanded,
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
 * Selected: an accent pill with the icon and label side by side. Unselected: the icon above its
 * label, in the muted on-surface-variant color.
 */
@Composable
private fun NavDestinationItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            contentColor = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(destination.icon(), contentDescription = destination.name)
                Text(
                    destination.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                destination.icon(),
                contentDescription = destination.name,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
        SkimbyteBottomNavBar(selected = TopLevelDestination.Feed, onDestinationSelected = {}, expanded = true)
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Feed, onDestinationSelected = {}, expanded = true)
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarBookmarksPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Bookmarks, onDestinationSelected = {}, expanded = true)
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarAccountPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Account, onDestinationSelected = {}, expanded = true)
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarCollapsedPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Feed, onDestinationSelected = {}, expanded = false)
    }
}
