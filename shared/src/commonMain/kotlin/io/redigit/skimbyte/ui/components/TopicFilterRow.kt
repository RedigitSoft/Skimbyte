package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TopicFilterRow(
    topics: List<Topic>,
    selectedTopic: Topic,
    onTopicSelected: (Topic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // Whether the selection changed by chip tap or by swiping the feed pager, bring the selected
    // chip to the front of the row so the active topic is always the one the header leads with.
    LaunchedEffect(selectedTopic, topics) {
        val index = topics.indexOf(selectedTopic)
        if (index >= 0) listState.animateScrollToItem(index)
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(topics, key = { it.id }) { topic ->
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
        TopicFilterRow(topics = Topic.defaults, selectedTopic = Topic.Kotlin, onTopicSelected = {})
    }
}

@Preview
@Composable
private fun TopicFilterRowLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicFilterRow(topics = Topic.defaults, selectedTopic = Topic.Kotlin, onTopicSelected = {})
    }
}

@Preview
@Composable
private fun TopicFilterRowAllSelectedPreview() {
    SkimbyteTheme {
        TopicFilterRow(topics = Topic.defaults, selectedTopic = Topic.All, onTopicSelected = {})
    }
}
