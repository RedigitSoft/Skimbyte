package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonsCatalogContent() {
    CatalogPage {
        listOf(true, false).forEach { enabled ->
            CatalogSection(
                title = if (enabled) "Enabled" else "Disabled",
                description = "Filled, elevated, tonal, outlined, and text emphases.",
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = {}, enabled = enabled) { Text("Filled") }
                    ElevatedButton(onClick = {}, enabled = enabled) { Text("Elevated") }
                    FilledTonalButton(onClick = {}, enabled = enabled) { Text("Tonal") }
                    OutlinedButton(onClick = {}, enabled = enabled) { Text("Outlined") }
                    TextButton(onClick = {}, enabled = enabled) { Text("Text") }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ButtonsCatalogPreview() {
    SkimbyteTheme {
        ButtonsCatalogContent()
    }
}

@Preview
@Composable
private fun ButtonsCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ButtonsCatalogContent()
    }
}
