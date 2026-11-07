package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * Shared presentation chrome for catalog pages, so every entry reads the same way: a [CatalogPage]
 * holding [CatalogSection]s, with full-screen embeds wrapped in a [ScreenFrame].
 *
 * Pages that must own their scrolling (the color grid's `LazyVerticalGrid`) skip [CatalogPage];
 * everything else uses it, which is why screen embeds carry a fixed height — bounded children are
 * what make nesting them inside this scrollable column legal.
 */
@Composable
fun CatalogPage(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            // Capped so demos keep a readable measure on wide desktop windows instead of
            // stretching edge to edge.
            modifier = Modifier
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            content = content,
        )
    }
}

/**
 * One labeled demo within a page: eyebrow title, optional supporting copy, and the demo itself —
 * framed on a raised container by default, or unframed ([framed] = false) when the content brings
 * its own surface, like a [ScreenFrame].
 */
@Composable
fun CatalogSection(
    title: String,
    description: String? = null,
    framed: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EyebrowText(title)
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (framed) DemoCard(content = content) else content()
    }
}

/** The raised, outlined canvas a component demo sits on. */
@Composable
fun DemoCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Viewport for a full-screen embed (Account, Settings, Post detail…): an outlined, clipped frame
 * that reads as a device screen. The embed keeps its own fixed height modifier.
 */
@Composable
fun ScreenFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = content,
    )
}

@Preview
@Composable
private fun CatalogSectionPreview() {
    SkimbyteTheme {
        CatalogPage {
            CatalogSection(
                title = "Enabled",
                description = "How a framed demo section reads in the catalog.",
            ) {
                Button(onClick = {}) { Text("Filled") }
            }
        }
    }
}

@Preview
@Composable
private fun CatalogSectionLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        CatalogPage {
            CatalogSection(
                title = "Enabled",
                description = "How a framed demo section reads in the catalog.",
            ) {
                Button(onClick = {}) { Text("Filled") }
            }
        }
    }
}
