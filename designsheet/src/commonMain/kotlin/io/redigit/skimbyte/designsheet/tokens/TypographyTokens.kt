package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme

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
    CatalogPage {
        CatalogSection(title = "Type scale") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                styles.forEach { named ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${named.name} · ${named.style.fontSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Design, meet code.", style = named.style)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TypographyTokensPreview() {
    SkimbyteTheme {
        TypographyTokensContent()
    }
}

@Preview
@Composable
private fun TypographyTokensLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TypographyTokensContent()
    }
}
