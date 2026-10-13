package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CatalogListScreen(
    items: List<CatalogItem>,
    onItemClick: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Design sheet") }) },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items.groupBy { it.category }.forEach { (category, categoryItems) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                    )
                }
                items(categoryItems) { catalogItem ->
                    ListItem(
                        headlineContent = { Text(catalogItem.title) },
                        modifier = Modifier.clickable { onItemClick(catalogItem) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun CatalogListScreenPreview() {
    MaterialTheme {
        CatalogListScreen(items = catalogItems, onItemClick = {})
    }
}
