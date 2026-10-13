package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SelectionControlsCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        var checked by remember { mutableStateOf(true) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Text("Checkbox")
        }

        var switched by remember { mutableStateOf(true) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = switched, onCheckedChange = { switched = it })
            Text("Switch")
        }

        var selected by remember { mutableStateOf(0) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("One", "Two").forEachIndexed { index, label ->
                RadioButton(selected = selected == index, onClick = { selected = index })
                Text(label)
            }
        }
    }
}

@Preview
@Composable
private fun SelectionControlsCatalogPreview() {
    MaterialTheme {
        SelectionControlsCatalogContent()
    }
}
