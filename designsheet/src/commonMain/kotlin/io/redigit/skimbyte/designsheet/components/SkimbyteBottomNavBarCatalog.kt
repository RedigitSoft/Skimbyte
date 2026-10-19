package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteBottomNavBarCatalogContent() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            fabMenu = null,
        )
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Bookmarks,
            onDestinationSelected = {},
            fabMenu = null,
        )
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Search,
            onDestinationSelected = {},
            fabMenu = null,
        )
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Account,
            onDestinationSelected = {},
            fabMenu = null,
        )
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarCatalogPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBarCatalogContent()
    }
}
