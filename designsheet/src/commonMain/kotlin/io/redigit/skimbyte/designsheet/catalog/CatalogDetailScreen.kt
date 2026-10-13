package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CatalogDetailScreen(item: CatalogItem, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(item.title) }) },
    ) { padding ->
        // Each catalog entry owns its own scrolling (some are lazy containers, which must not be
        // nested inside another scrollable), so this host only provides bounded size + padding.
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            item.content()
        }
    }
}

@Composable
fun CatalogDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select an item from the list", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun CatalogDetailScreenPreview() {
    MaterialTheme {
        CatalogDetailScreen(item = catalogItems.first())
    }
}

@Preview
@Composable
private fun CatalogDetailPlaceholderPreview() {
    MaterialTheme {
        CatalogDetailPlaceholder()
    }
}
