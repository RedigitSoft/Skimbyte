package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.navigation.SkimbyteBottomNavBar
import io.redigit.skimbyte.ui.navigation.TopLevelDestination

@Composable
fun SkimbyteBottomNavBarCatalogContent() {
    CatalogPage {
        // One dock per selected destination so the catalog shows how the active-item pill expands
        // for each tab.
        CatalogSection(
            title = "Selected states",
            description = "One dock per destination, showing the active-item pill expanding for each tab.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TopLevelDestination.entries.forEach { destination ->
                    SkimbyteBottomNavBar(
                        selected = destination,
                        onDestinationSelected = {},
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarCatalogPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBarCatalogContent()
    }
}

@Preview
@Composable
private fun SkimbyteBottomNavBarCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SkimbyteBottomNavBarCatalogContent()
    }
}
