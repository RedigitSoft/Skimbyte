package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private data class NamedColor(val name: String, val color: Color)

private fun ColorScheme.swatches(): List<NamedColor> = listOf(
    NamedColor("primary", primary),
    NamedColor("onPrimary", onPrimary),
    NamedColor("primaryContainer", primaryContainer),
    NamedColor("secondary", secondary),
    NamedColor("secondaryContainer", secondaryContainer),
    NamedColor("tertiary", tertiary),
    NamedColor("tertiaryContainer", tertiaryContainer),
    NamedColor("surface", surface),
    NamedColor("surfaceVariant", surfaceVariant),
    NamedColor("background", background),
    NamedColor("error", error),
    NamedColor("errorContainer", errorContainer),
)

@Composable
fun ColorTokensContent() {
    val swatches = MaterialTheme.colorScheme.swatches()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(swatches) { swatch ->
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(swatch.color),
                ) {}
                Text(swatch.name, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview
@Composable
private fun ColorTokensPreview() {
    MaterialTheme {
        ColorTokensContent()
    }
}
