package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.BookmarkButton

private data class ButtonState(val label: String, val isBookmarked: Boolean, val isLoading: Boolean)

private val states = listOf(
    ButtonState("Idle", isBookmarked = false, isLoading = false),
    ButtonState("Bookmarked", isBookmarked = true, isLoading = false),
    ButtonState("Loading", isBookmarked = false, isLoading = true),
)

@Composable
fun BookmarkButtonCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "States",
            description = "The loading state covers the round-trip while a bookmark toggle is in flight.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                states.forEach { state ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BookmarkButton(isBookmarked = state.isBookmarked, isLoading = state.isLoading, onClick = {})
                        Text(state.label)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BookmarkButtonCatalogPreview() {
    SkimbyteTheme {
        BookmarkButtonCatalogContent()
    }
}

@Preview
@Composable
private fun BookmarkButtonCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        BookmarkButtonCatalogContent()
    }
}
