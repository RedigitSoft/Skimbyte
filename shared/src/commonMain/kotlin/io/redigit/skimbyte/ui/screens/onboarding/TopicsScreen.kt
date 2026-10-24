package io.redigit.skimbyte.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicsScreen(
    state: TopicsState,
    onIntent: (TopicsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (state.isEditing) "Edit your topics" else "Pick your topics",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Choose 3 or 4 topics to personalize your feed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.available.forEach { topic ->
                TopicChip(
                    topic = topic,
                    isSelected = topic in state.selected,
                    onClick = { onIntent(TopicsIntent.TopicToggled(topic)) },
                )
            }
        }
        Button(
            onClick = { onIntent(TopicsIntent.Confirmed) },
            enabled = state.canConfirm,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isEditing) "Save" else "Get started")
        }
    }
}

/**
 * A rounded, selectable topic chip. Selected: filled accent pill with a leading check. Unselected:
 * an outlined surface pill. Container, border and content colors animate between the two states.
 */
@Composable
private fun TopicChip(
    topic: Topic,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "topicChipContainer",
    )
    val content by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "topicChipContent",
    )
    val border by animateColorAsState(
        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        label = "topicChipBorder",
    )
    Surface(
        shape = CircleShape,
        color = container,
        contentColor = content,
        border = BorderStroke(1.dp, border),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text = topic.displayLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview
@Composable
private fun TopicsScreenOnboardingPreview() {
    SkimbyteTheme {
        TopicsScreen(
            state = TopicsState(selected = setOf(Topic.Kotlin, Topic.Android)),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun TopicsScreenOnboardingLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicsScreen(
            state = TopicsState(selected = setOf(Topic.Kotlin, Topic.Android)),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun TopicsScreenEditingPreview() {
    SkimbyteTheme {
        TopicsScreen(
            state = TopicsState(
                selected = setOf(Topic.Kotlin, Topic.Android, Topic.Kmp),
                isEditing = true,
            ),
            onIntent = {},
        )
    }
}
