package io.redigit.skimbyte.designsheet.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun CatalogDetailScreen(item: CatalogItem, onBack: () -> Unit, modifier: Modifier = Modifier) {
    // Page-local theme override, so an entry can be inspected in either scheme without leaving the
    // page. Nesting SkimbyteTheme is safe for the window chrome: only the outermost theme reports
    // to LocalWindowChromeListener.
    var previewDark by rememberSaveable { mutableStateOf(true) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                // Always shown, matching the app's detail screens: in the two-pane layout it just
                // clears the selection, but in a narrow (single-pane) window it is the only way
                // back on desktop/web, which have no system back gesture.
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { previewDark = !previewDark }) {
                        Icon(
                            imageVector = if (previewDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = if (previewDark) "Preview in light theme" else "Preview in dark theme",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                )
            }
            // Each catalog entry owns its own scrolling (some are lazy containers, which must not
            // be nested inside another scrollable), so this host only provides bounded size.
            SkimbyteTheme(darkTheme = previewDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    item.content()
                }
            }
        }
    }
}

@Composable
fun CatalogDetailPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select an item from the list", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun CatalogDetailScreenPreview() {
    SkimbyteTheme {
        CatalogDetailScreen(item = catalogItems.first(), onBack = {})
    }
}

@Preview
@Composable
private fun CatalogDetailScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        CatalogDetailScreen(item = catalogItems.first(), onBack = {})
    }
}

@Preview
@Composable
private fun CatalogDetailPlaceholderPreview() {
    SkimbyteTheme {
        CatalogDetailPlaceholder()
    }
}
