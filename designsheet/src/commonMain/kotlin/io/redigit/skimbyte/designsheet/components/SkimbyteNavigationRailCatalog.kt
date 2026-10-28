package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.ui.navigation.SkimbyteNavigationRail
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteNavigationRailCatalogContent() {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
        )
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Bookmarks,
            onDestinationSelected = {},
        )
        SkimbyteNavigationRail(
            selected = TopLevelDestination.Account,
            onDestinationSelected = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteNavigationRailCatalogPreview() {
    SkimbyteTheme {
        SkimbyteNavigationRailCatalogContent()
    }
}
