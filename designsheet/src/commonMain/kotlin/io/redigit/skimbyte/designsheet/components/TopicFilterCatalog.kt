package io.redigit.skimbyte.designsheet.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.TopicFilterRow

@Composable
fun TopicFilterCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Interactive",
            description = "The feed's topic selector; the row scrolls horizontally when topics overflow.",
        ) {
            var selected by remember { mutableStateOf(Topic.All) }
            TopicFilterRow(topics = Topic.defaults, selectedTopic = selected, onTopicSelected = { selected = it })
        }
    }
}

@Preview
@Composable
private fun TopicFilterCatalogPreview() {
    SkimbyteTheme {
        TopicFilterCatalogContent()
    }
}

@Preview
@Composable
private fun TopicFilterCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicFilterCatalogContent()
    }
}
