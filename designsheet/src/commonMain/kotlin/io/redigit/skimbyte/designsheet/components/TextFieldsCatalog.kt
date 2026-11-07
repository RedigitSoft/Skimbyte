package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.fillMaxWidth
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
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TextFieldsCatalogContent() {
    CatalogPage {
        CatalogSection(title = "Filled") {
            var filledValue by remember { mutableStateOf("") }
            TextField(
                value = filledValue,
                onValueChange = { filledValue = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        CatalogSection(title = "Outlined") {
            var outlinedValue by remember { mutableStateOf("") }
            OutlinedTextField(
                value = outlinedValue,
                onValueChange = { outlinedValue = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun TextFieldsCatalogPreview() {
    SkimbyteTheme {
        TextFieldsCatalogContent()
    }
}

@Preview
@Composable
private fun TextFieldsCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TextFieldsCatalogContent()
    }
}
