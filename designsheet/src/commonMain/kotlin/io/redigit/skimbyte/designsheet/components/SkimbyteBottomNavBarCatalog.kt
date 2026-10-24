package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.navigation.TopLevelDestination
import io.redigit.skimbyte.ui.navigation.SkimbyteBottomNavBar
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteBottomNavBarCatalogContent() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // One dock per selected destination so the catalog shows how the active-item pill expands
        // for each tab.
        TopLevelDestination.entries.forEach { destination ->
            SkimbyteBottomNavBar(
                selected = destination,
                onDestinationSelected = {},
            )
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
