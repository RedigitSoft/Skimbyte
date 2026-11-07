package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun SelectionControlsCatalogContent() {
    CatalogPage {
        CatalogSection(title = "Checkbox") {
            var checked by remember { mutableStateOf(true) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                Text("Checkbox")
            }
        }

        CatalogSection(title = "Switch") {
            var switched by remember { mutableStateOf(true) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = switched, onCheckedChange = { switched = it })
                Text("Switch")
            }
        }

        CatalogSection(title = "Radio buttons") {
            var selected by remember { mutableStateOf(0) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("One", "Two").forEachIndexed { index, label ->
                    RadioButton(selected = selected == index, onClick = { selected = index })
                    Text(label)
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectionControlsCatalogPreview() {
    SkimbyteTheme {
        SelectionControlsCatalogContent()
    }
}

@Preview
@Composable
private fun SelectionControlsCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SelectionControlsCatalogContent()
    }
}
