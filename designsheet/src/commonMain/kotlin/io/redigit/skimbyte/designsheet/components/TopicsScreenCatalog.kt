package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.screens.onboarding.TopicsIntent
import io.redigit.skimbyte.ui.screens.onboarding.TopicsScreen
import io.redigit.skimbyte.ui.screens.onboarding.TopicsState

@Composable
fun TopicsScreenCatalogContent() {
    CatalogPage {
        ScreenFrame {
            var selected by remember { mutableStateOf(setOf(Topic.Kotlin, Topic.Android)) }
            TopicsScreen(
                state = TopicsState(selected = selected),
                onIntent = { intent ->
                    if (intent is TopicsIntent.TopicToggled) {
                        val topic = intent.topic
                        selected = if (topic in selected) selected - topic else selected + topic
                    }
                },
                // Bounded so the screen's internal grid can scroll inside the page column.
                modifier = Modifier.height(640.dp),
            )
        }
    }
}

@Preview
@Composable
private fun TopicsScreenCatalogPreview() {
    SkimbyteTheme {
        TopicsScreenCatalogContent()
    }
}

@Preview
@Composable
private fun TopicsScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicsScreenCatalogContent()
    }
}
