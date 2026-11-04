package io.redigit.skimbyte.designsheet.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A role and the on-color that is drawn against it.
 *
 * Pairing them is the point. The previous version of this catalog listed 12 bare swatches and
 * omitted every `surfaceContainer*` role - which is exactly why it showed a clean warm palette
 * while the running app drew violet card containers. A role that the theme never sets can only
 * hide here if nothing renders it, so every role the theme defines gets a swatch, and every
 * swatch draws its paired content color on top.
 */
private data class RoleSwatch(
    val name: String,
    val container: Color,
    val content: Color,
)

private fun Color.hex(): String {
    val argb = toArgb()
    val digits = "0123456789ABCDEF"
    val out = StringBuilder("#")
    for (shift in intArrayOf(16, 8, 0)) {
        val byte = (argb shr shift) and 0xFF
        out.append(digits[byte shr 4]).append(digits[byte and 0x0F])
    }
    return out.toString()
}

/**
 * Every role in the scheme, grouped by family, in the order the spec's role table states them.
 * Roles that are themselves content rather than containers (`outline`, `scrim`, `surfaceTint`,
 * `inversePrimary`) are drawn against `surface`, which is where they are actually used.
 */
private fun ColorScheme.roleGroups(): List<Pair<String, List<RoleSwatch>>> = listOf(
    "Primary" to listOf(
        RoleSwatch("primary", primary, onPrimary),
        RoleSwatch("primaryContainer", primaryContainer, onPrimaryContainer),
        RoleSwatch("inversePrimary", inversePrimary, surface),
    ),
    "Secondary" to listOf(
        RoleSwatch("secondary", secondary, onSecondary),
        RoleSwatch("secondaryContainer", secondaryContainer, onSecondaryContainer),
    ),
    "Tertiary" to listOf(
        RoleSwatch("tertiary", tertiary, onTertiary),
        RoleSwatch("tertiaryContainer", tertiaryContainer, onTertiaryContainer),
    ),
    "Surfaces" to listOf(
        RoleSwatch("background", background, onBackground),
        RoleSwatch("surface", surface, onSurface),
        RoleSwatch("surfaceDim", surfaceDim, onSurface),
        RoleSwatch("surfaceBright", surfaceBright, onSurface),
        RoleSwatch("surfaceVariant", surfaceVariant, onSurfaceVariant),
        RoleSwatch("surfaceTint", surfaceTint, surface),
    ),
    // Elevation levels 0-4. These are the roles the old catalog omitted entirely, and the role
    // CardDefaults reaches for by default lives in here.
    "Surface containers" to listOf(
        RoleSwatch("surfaceContainerLowest", surfaceContainerLowest, onSurface),
        RoleSwatch("surfaceContainerLow", surfaceContainerLow, onSurface),
        RoleSwatch("surfaceContainer", surfaceContainer, onSurface),
        RoleSwatch("surfaceContainerHigh", surfaceContainerHigh, onSurface),
        RoleSwatch("surfaceContainerHighest", surfaceContainerHighest, onSurface),
    ),
    "Outlines and inverse" to listOf(
        RoleSwatch("outline", outline, surface),
        RoleSwatch("outlineVariant", outlineVariant, surface),
        RoleSwatch("inverseSurface", inverseSurface, inverseOnSurface),
        RoleSwatch("scrim", scrim, Color.White),
    ),
    "Error" to listOf(
        RoleSwatch("error", error, onError),
        RoleSwatch("errorContainer", errorContainer, onErrorContainer),
    ),
    // Scheme-independent by definition: these should look identical in light and dark.
    "Fixed accents" to listOf(
        RoleSwatch("primaryFixed", primaryFixed, onPrimaryFixed),
        RoleSwatch("primaryFixedDim", primaryFixedDim, onPrimaryFixedVariant),
        RoleSwatch("secondaryFixed", secondaryFixed, onSecondaryFixed),
        RoleSwatch("secondaryFixedDim", secondaryFixedDim, onSecondaryFixedVariant),
        RoleSwatch("tertiaryFixed", tertiaryFixed, onTertiaryFixed),
        RoleSwatch("tertiaryFixedDim", tertiaryFixedDim, onTertiaryFixedVariant),
    ),
)

@Composable
fun ColorTokensContent() {
    val groups = MaterialTheme.colorScheme.roleGroups()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        groups.forEach { (groupName, swatches) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            items(swatches) { swatch ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(swatch.container),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Drawn in the paired on-color: an unreadable label is a failed pairing.
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.labelMedium,
                            color = swatch.content,
                        )
                    }
                    Text(
                        text = swatch.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = swatch.container.hex(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ColorTokensPreview() {
    MaterialTheme {
        ColorTokensContent()
    }
}
