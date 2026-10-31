package io.redigit.skimbyte.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The expanded-width (tablet / desktop / unfolded) counterpart to [SkimbyteBottomNavBar]. Uses the
 * same visual language: a primary-accented pill indicator behind the selected destination with its
 * icon and label in the primary color, so navigation reads consistently across window sizes. The
 * items are vertically centered in the rail rather than pinned to the top edge.
 */
@Composable
fun SkimbyteNavigationRail(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        TopLevelDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = destination == selected,
                onClick = { onDestinationSelected(destination) },
                icon = { Icon(destination.icon(), contentDescription = destination.name) },
                label = { Text(destination.name) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun SkimbyteNavigationRailFeedPreview() {
    SkimbyteTheme {
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteNavigationRailBookmarksPreview() {
    SkimbyteTheme {
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Bookmarks,
            onDestinationSelected = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteNavigationRailSearchPreview() {
    SkimbyteTheme {
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Account,
            onDestinationSelected = {},
        )
    }
}
