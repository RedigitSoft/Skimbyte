package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.navigation.SkimbyteNavigationRail
import io.redigit.skimbyte.ui.navigation.TopLevelDestination

@Composable
fun SkimbyteNavigationRailCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Selected states",
            description = "One rail per destination.",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TopLevelDestination.entries.forEach { destination ->
                    SkimbyteNavigationRail(
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
private fun SkimbyteNavigationRailCatalogPreview() {
    SkimbyteTheme {
        SkimbyteNavigationRailCatalogContent()
    }
}

@Preview
@Composable
private fun SkimbyteNavigationRailCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SkimbyteNavigationRailCatalogContent()
    }
}
