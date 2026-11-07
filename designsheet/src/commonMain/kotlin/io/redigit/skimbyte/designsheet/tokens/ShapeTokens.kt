package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteShapeTokens
import io.redigit.skimbyte.theme.SkimbyteTheme

private data class NamedShape(val name: String, val shape: Shape)

@Composable
fun ShapeTokensContent() {
    val shapes = MaterialTheme.shapes
    val named = listOf(
        NamedShape("extraSmall", shapes.extraSmall),
        NamedShape("small", shapes.small),
        NamedShape("medium", shapes.medium),
        NamedShape("large", shapes.large),
        NamedShape("extraLarge", shapes.extraLarge),
        // Not an M3 Shapes slot — a true capsule for chips, which used to borrow extraLarge.
        NamedShape("pill", SkimbyteShapeTokens.pill),
    )
    CatalogPage {
        CatalogSection(title = "Shape scale") {
            Column {
                named.forEach { entry ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Wider than tall so `pill`'s 50% radius reads as the capsule it is rather
                        // than as a circle, which is all a square swatch could ever show.
                        Row(
                            modifier = Modifier
                                .size(width = 72.dp, height = 40.dp)
                                .background(color = MaterialTheme.colorScheme.primary, shape = entry.shape),
                        ) {}
                        Text(" ${entry.name}")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ShapeTokensPreview() {
    SkimbyteTheme {
        ShapeTokensContent()
    }
}

@Preview
@Composable
private fun ShapeTokensLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        ShapeTokensContent()
    }
}
