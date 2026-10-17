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
import io.redigit.skimbyte.nav.ui.SkimbyteFabMenu
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SkimbyteFabMenuCatalogContent() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var expanded by remember { mutableStateOf(false) }
        SkimbyteFabMenu(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            isBookmarked = false,
            isBookmarkLoading = false,
            onBookmarkClick = {},
            onShareClick = {},
            onOpenSourceClick = {},
        )
        var expandedLoading by remember { mutableStateOf(true) }
        SkimbyteFabMenu(
            expanded = expandedLoading,
            onExpandedChange = { expandedLoading = it },
            isBookmarked = true,
            isBookmarkLoading = true,
            onBookmarkClick = {},
            onShareClick = {},
            onOpenSourceClick = {},
        )
    }
}

@Preview
@Composable
private fun SkimbyteFabMenuCatalogPreview() {
    SkimbyteTheme {
        SkimbyteFabMenuCatalogContent()
    }
}
