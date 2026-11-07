package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Tokens" -> SkimbyteIcons.CategoryTokens
    "Components" -> SkimbyteIcons.CategoryComponents
    "Feed" -> SkimbyteIcons.CategoryFeed
    "Navigation" -> SkimbyteIcons.CategoryNavigation
    "Bookmarks" -> SkimbyteIcons.BookmarkOutline
    "Account" -> SkimbyteIcons.CategoryAccount
    else -> SkimbyteIcons.CategoryOnboarding
}

@Composable
fun CatalogListScreen(
    items: List<CatalogItem>,
    onItemClick: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(DesignSheetStrings.TITLE) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items.groupBy { it.category }.forEach { (category, categoryItems) ->
                item(key = "header-$category") {
                    CategoryHeader(
                        category = category,
                        count = categoryItems.size,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp),
                    )
                }
                item(key = "group-$category") {
                    CategoryCard(items = categoryItems, onItemClick = onItemClick)
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        EyebrowText(category)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun CategoryCard(
    items: List<CatalogItem>,
    onItemClick: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            items.forEachIndexed { index, catalogItem ->
                ListItem(
                    headlineContent = { Text(catalogItem.title) },
                    supportingContent = {
                        Text(
                            text = catalogItem.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onItemClick(catalogItem) },
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CatalogListScreenPreview() {
    SkimbyteTheme {
        CatalogListScreen(items = catalogItems, onItemClick = {})
    }
}

@Preview
@Composable
private fun CatalogListScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        CatalogListScreen(items = catalogItems, onItemClick = {})
    }
}
