package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ButtonsCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(true, false).forEach { enabled ->
            Text(if (enabled) "Enabled" else "Disabled", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, enabled = enabled) { Text("Filled") }
                ElevatedButton(onClick = {}, enabled = enabled) { Text("Elevated") }
                FilledTonalButton(onClick = {}, enabled = enabled) { Text("Tonal") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {}, enabled = enabled) { Text("Outlined") }
                TextButton(onClick = {}, enabled = enabled) { Text("Text") }
            }
        }
    }
}

@Preview
@Composable
private fun ButtonsCatalogPreview() {
    MaterialTheme {
        ButtonsCatalogContent()
    }
}
