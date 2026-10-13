package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data object Spacing {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

private val namedSpacing = listOf(
    "none" to Spacing.none,
    "extraSmall" to Spacing.extraSmall,
    "small" to Spacing.small,
    "medium" to Spacing.medium,
    "large" to Spacing.large,
    "extraLarge" to Spacing.extraLarge,
)

@Composable
fun SpacingTokensContent() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        namedSpacing.forEach { (name, value) ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .width(value.coerceAtLeast(2.dp))
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primary),
                ) {}
                Text(" $name = $value")
            }
        }
    }
}

@Preview
@Composable
private fun SpacingTokensPreview() {
    MaterialTheme {
        SpacingTokensContent()
    }
}
