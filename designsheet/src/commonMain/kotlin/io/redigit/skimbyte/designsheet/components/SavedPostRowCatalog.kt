package io.redigit.skimbyte.designsheet.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.SavedPostRow
import kotlin.time.Instant

private val sample = Post(
    id = "catalog-1",
    topic = "Kotlin",
    title = "Kotlin 2.4.21 released with faster Wasm compilation",
    summaryMarkdown = "Summary",
    sourceName = "Kotlin Releases",
    sourceUrl = "https://example.com",
    imageUrl = null,
    digestDate = "2026-10-13",
    publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
    isBookmarked = true,
)

private val longTitle = sample.copy(
    id = "catalog-2",
    title = "A much longer saved headline that wraps to two lines and then gets ellipsized for the rest",
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SavedPostRowCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Variants",
            description = "Default, a wrapping headline that ellipsizes past two lines, and a row without the remove action.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SavedPostRow(post = sample, onClick = {}, onRemoveClick = {})
                SavedPostRow(post = longTitle, onClick = {}, onRemoveClick = {})
                SavedPostRow(post = sample.copy(id = "catalog-3"), onClick = {})
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SavedPostRowCatalogPreview() {
    SkimbyteTheme {
        SavedPostRowCatalogContent()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun SavedPostRowCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SavedPostRowCatalogContent()
    }
}
