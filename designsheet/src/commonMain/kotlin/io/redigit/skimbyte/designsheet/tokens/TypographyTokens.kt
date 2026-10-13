package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private data class NamedStyle(val name: String, val style: TextStyle)

@Composable
fun TypographyTokensContent() {
    val typography = MaterialTheme.typography
    val styles = listOf(
        NamedStyle("displayLarge", typography.displayLarge),
        NamedStyle("displayMedium", typography.displayMedium),
        NamedStyle("headlineLarge", typography.headlineLarge),
        NamedStyle("headlineMedium", typography.headlineMedium),
        NamedStyle("titleLarge", typography.titleLarge),
        NamedStyle("titleMedium", typography.titleMedium),
        NamedStyle("bodyLarge", typography.bodyLarge),
        NamedStyle("bodyMedium", typography.bodyMedium),
        NamedStyle("labelLarge", typography.labelLarge),
        NamedStyle("labelMedium", typography.labelMedium),
    )
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        styles.forEach { named ->
            Text("${named.name}: Design, meet code.", style = named.style)
        }
    }
}

@Preview
@Composable
private fun TypographyTokensPreview() {
    MaterialTheme {
        TypographyTokensContent()
    }
}
