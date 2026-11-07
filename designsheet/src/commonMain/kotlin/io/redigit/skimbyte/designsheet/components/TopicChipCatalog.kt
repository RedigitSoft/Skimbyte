package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.TopicChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicChipCatalogContent() {
    CatalogPage {
        CatalogSection(title = "Interactive", description = "Tap to select — the feed's filter form.") {
            var selected by remember { mutableStateOf(Topic.Kotlin) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Topic.defaults.forEach { topic ->
                    TopicChip(
                        label = topic.displayLabel,
                        selected = topic == selected,
                        onClick = { selected = topic },
                    )
                }
            }
        }

        // The account screen's followed-topics list: same chip, nothing to tap, so no touch-target
        // floor and no action handed to assistive technology.
        CatalogSection(
            title = "Read-only",
            description = "Listing topics, not filtering by them: no touch-target floor, no action for assistive technology.",
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Topic.defaults.take(4).forEach { topic ->
                    TopicChip(label = topic.displayLabel)
                }
            }
        }
    }
}

@Preview
@Composable
private fun TopicChipCatalogPreview() {
    SkimbyteTheme {
        TopicChipCatalogContent()
    }
}

@Preview
@Composable
private fun TopicChipCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicChipCatalogContent()
    }
}
