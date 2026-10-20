package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.nav.TopLevelDestination
import io.redigit.skimbyte.nav.ui.SkimbyteBottomNavBar
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteBottomNavBarCatalogContent() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // The FAB toggles this live, so the catalog demonstrates the real expand/collapse
        // interaction rather than a frozen snapshot.
        var expanded by remember { mutableStateOf(true) }
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Feed,
            onDestinationSelected = {},
            expanded = expanded,
            onExpandedChange = { expanded = it },
        )
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Bookmarks,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
        )
        SkimbyteBottomNavBar(
            selected = TopLevelDestination.Search,
            onDestinationSelected = {},
            expanded = true,
            onExpandedChange = {},
        )
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
private fun SkimbyteBottomNavBarCatalogPreview() {
    SkimbyteTheme {
        SkimbyteBottomNavBarCatalogContent()
    }
}
