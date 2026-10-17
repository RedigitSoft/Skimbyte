package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.feed.ui.BookmarkButton
import io.redigit.skimbyte.theme.SkimbyteTheme

private data class ButtonState(val label: String, val isBookmarked: Boolean, val isLoading: Boolean)

private val states = listOf(
    ButtonState("Idle", isBookmarked = false, isLoading = false),
    ButtonState("Bookmarked", isBookmarked = true, isLoading = false),
    ButtonState("Loading", isBookmarked = false, isLoading = true),
)

@Composable
fun BookmarkButtonCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

@Preview
@Composable
private fun BookmarkButtonCatalogPreview() {
    SkimbyteTheme {
        BookmarkButtonCatalogContent()
    }
}
