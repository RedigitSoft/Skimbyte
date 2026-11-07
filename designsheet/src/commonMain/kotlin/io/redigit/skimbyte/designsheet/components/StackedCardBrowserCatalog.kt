package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.StackedCardBrowser

@Composable
fun StackedCardBrowserCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Interactive",
            description = "Drag or swipe the top card to page through the deck.",
        ) {
            StackedCardBrowser(
                items = listOf("First card", "Second card", "Third card", "Fourth card"),
                onIndexChanged = {},
                modifier = Modifier.height(500.dp),
            ) { label ->
                Card(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StackedCardBrowserCatalogPreview() {
    SkimbyteTheme {
        StackedCardBrowserCatalogContent()
    }
}

@Preview
@Composable
private fun StackedCardBrowserCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        StackedCardBrowserCatalogContent()
    }
}
