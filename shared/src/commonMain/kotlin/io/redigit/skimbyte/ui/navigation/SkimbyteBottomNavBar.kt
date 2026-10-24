package io.redigit.skimbyte.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The compact-width bottom navigation chrome: a Material 3 floating pill dock, always expanded. The
 * selected destination shows an accent-outlined pill with its icon and label inline, while the
 * others are icon-only and muted. The label is single-line so the dock keeps a constant height
 * regardless of which (variable-length) destination is selected.
 */
@Composable
fun SkimbyteBottomNavBar(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
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
 * A single destination that animates between two states: selected is an accent-outlined pill with
 * the icon and (single-line) label side by side; unselected collapses to the icon alone, muted. The
 * label expands/shrinks horizontally and the pill's container, border, and content colors cross-fade
 * as selection changes.
 */
@Composable
private fun NavDestinationItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        label = "navItemContainer",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
        label = "navItemBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navItemContent",
    )
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(destination.icon(), contentDescription = destination.label)
            // In a RowScope this fades + expands/shrinks horizontally by default, giving the
            // selected pill its expand/collapse animation.
            AnimatedVisibility(visible = selected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
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

/** Human-readable Camel Case label for the destination (enum names already read this way). */
internal val TopLevelDestination.label: String
    get() = name

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Feed, onDestinationSelected = {})
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarFeedLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Feed, onDestinationSelected = {})
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarBookmarksPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Bookmarks, onDestinationSelected = {})
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarAccountPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBar(selected = TopLevelDestination.Account, onDestinationSelected = {})
    }
}
