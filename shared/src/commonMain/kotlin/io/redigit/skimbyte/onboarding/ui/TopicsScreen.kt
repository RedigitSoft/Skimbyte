package io.redigit.skimbyte.onboarding.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.onboarding.TopicsUiState
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun TopicsScreen(
    uiState: TopicsUiState,
    onTopicToggled: (Topic) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (uiState.isEditing) "Edit your topics" else "Pick your topics",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Choose 3 or 4 topics to personalize your feed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        uiState.available.chunked(2).forEach { rowTopics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTopics.forEach { topic ->
                    TopicChip(
                        topic = topic,
                        isSelected = topic in uiState.selected,
                        onClick = { onTopicToggled(topic) },
                    )
                }
            }
        }
        Button(
            onClick = onConfirm,
            enabled = uiState.canConfirm,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isEditing) "Save" else "Get started")
        }
    }
}

@Composable
private fun TopicChip(
    topic: Topic,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    if (isSelected) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onClick),
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
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Preview
@Composable
private fun TopicsScreenOnboardingPreview() {
    SkimbyteTheme {
        TopicsScreen(
            uiState = TopicsUiState(selected = setOf(Topic.Kotlin, Topic.Android)),
            onTopicToggled = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun TopicsScreenOnboardingLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicsScreen(
            uiState = TopicsUiState(selected = setOf(Topic.Kotlin, Topic.Android)),
            onTopicToggled = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun TopicsScreenEditingPreview() {
    SkimbyteTheme {
        TopicsScreen(
            uiState = TopicsUiState(
                selected = setOf(Topic.Kotlin, Topic.Android, Topic.Kmp),
                isEditing = true,
            ),
            onTopicToggled = {},
            onConfirm = {},
        )
    }
}

@Preview
@Composable
private fun TopicsScreenEditingLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicsScreen(
            uiState = TopicsUiState(
                selected = setOf(Topic.Kotlin, Topic.Android, Topic.Kmp),
                isEditing = true,
            ),
            onTopicToggled = {},
            onConfirm = {},
        )
    }
}
