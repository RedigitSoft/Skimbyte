package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldsCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        var filledValue by remember { mutableStateOf("") }
        Text("Filled")
        TextField(
            value = filledValue,
            onValueChange = { filledValue = it },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth(),
        )

        var outlinedValue by remember { mutableStateOf("") }
        Text("Outlined")
        OutlinedTextField(
            value = outlinedValue,
            onValueChange = { outlinedValue = it },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun TextFieldsCatalogPreview() {
    MaterialTheme {
        TextFieldsCatalogContent()
    }
}
