package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.screens.onboarding.TopicsState
import io.redigit.skimbyte.ui.screens.onboarding.TopicsIntent
import io.redigit.skimbyte.ui.screens.onboarding.TopicsScreen
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TopicsScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var selected by remember { mutableStateOf(setOf(Topic.Kotlin, Topic.Android)) }
        TopicsScreen(
            state = TopicsState(selected = selected),
            onIntent = { intent ->
                if (intent is TopicsIntent.TopicToggled) {
                    val topic = intent.topic
                    selected = if (topic in selected) selected - topic else selected + topic
                }
            },
        )
    }
}

@Preview
@Composable
private fun TopicsScreenCatalogPreview() {
    SkimbyteTheme {
        TopicsScreenCatalogContent()
    }
}
