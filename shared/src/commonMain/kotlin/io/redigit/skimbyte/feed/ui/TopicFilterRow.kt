package io.redigit.skimbyte.feed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.posts.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TopicFilterRow(
    topics: List<Topic>,
    selectedTopic: Topic,
    onTopicSelected: (Topic) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        topics.forEach { topic ->
            if (topic == selectedTopic) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onTopicSelected(topic) },
                ) {
                    Text(
                        text = topic.displayLabel,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                Text(
                    text = topic.displayLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable { onTopicSelected(topic) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun TopicFilterRowPreview() {
    SkimbyteTheme {
        TopicFilterRow(topics = Topic.entries, selectedTopic = Topic.Kotlin, onTopicSelected = {})
    }
}

@Preview
@Composable
private fun TopicFilterRowLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicFilterRow(topics = Topic.entries, selectedTopic = Topic.Kotlin, onTopicSelected = {})
    }
}

@Preview
@Composable
private fun TopicFilterRowAllSelectedPreview() {
    SkimbyteTheme {
        TopicFilterRow(topics = Topic.entries, selectedTopic = Topic.All, onTopicSelected = {})
    }
}
