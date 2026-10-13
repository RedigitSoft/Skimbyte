package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    )
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        named.forEach { entry ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = entry.shape),
                ) {}
                Text(" ${entry.name}")
            }
        }
    }
}

@Preview
@Composable
private fun ShapeTokensPreview() {
    MaterialTheme {
        ShapeTokensContent()
    }
}
