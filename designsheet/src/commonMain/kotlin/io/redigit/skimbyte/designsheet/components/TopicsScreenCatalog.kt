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
import io.redigit.skimbyte.onboarding.TopicsUiState
import io.redigit.skimbyte.onboarding.ui.TopicsScreen
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TopicsScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var selected by remember { mutableStateOf(setOf(Topic.Kotlin, Topic.Android)) }
        TopicsScreen(
            uiState = TopicsUiState(selected = selected),
            onTopicToggled = { topic ->
                selected = if (topic in selected) selected - topic else selected + topic
            },
            onConfirm = {},
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
