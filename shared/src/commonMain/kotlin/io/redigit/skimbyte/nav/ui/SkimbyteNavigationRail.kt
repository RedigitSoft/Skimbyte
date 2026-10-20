package io.redigit.skimbyte.nav.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteNavigationRail(
    selected: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = destination == selected,
                onClick = { onDestinationSelected(destination) },
                icon = { Icon(destination.icon(), contentDescription = destination.name) },
            )
        }
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
            selected = TopLevelDestination.Search,
            onDestinationSelected = {},
        )
    }
}
