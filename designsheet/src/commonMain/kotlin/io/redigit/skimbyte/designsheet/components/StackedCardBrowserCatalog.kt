package io.redigit.skimbyte.designsheet.components

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
import io.redigit.skimbyte.feed.ui.StackedCardBrowser
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun StackedCardBrowserCatalogContent() {
    StackedCardBrowser(
        items = listOf("First card", "Second card", "Third card", "Fourth card"),
        onIndexChanged = {},
        modifier = Modifier.height(500.dp),
    ) { label ->
        Card(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.headlineSmall)
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
