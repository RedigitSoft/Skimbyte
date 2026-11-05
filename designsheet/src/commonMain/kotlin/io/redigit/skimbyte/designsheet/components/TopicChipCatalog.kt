package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.EyebrowText
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.TopicChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicChipCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EyebrowText("Interactive — tap to select")
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

        // The account screen's followed-topics list: same chip, nothing to tap, so no touch-target
        // floor and no action handed to assistive technology.
        EyebrowText("Read-only — listing topics, not filtering by them")
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
