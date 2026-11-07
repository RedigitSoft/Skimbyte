package io.redigit.skimbyte.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.SkimbyteStrings

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopicsScreen(
    state: TopicsState,
    onIntent: (TopicsIntent) -> Unit,
    modifier: Modifier = Modifier,
    // Non-null when pushed as a screen you can back out of (editing interests from Settings);
    // null in the first-run onboarding flow, which has no "back".
    onBack: (() -> Unit)? = null,
) {
    Column(
        // Owns its insets: hosted both full-screen (onboarding, where the shell provides no
        // padding) and as a nav detail pane, so the screen itself keeps content below the
        // status bar.
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Icon(SkimbyteIcons.Back, contentDescription = SkimbyteStrings.Common.BACK)
            }
        }
        Column(
            // Scrolls so long server-fed topic lists never clip the confirm button; the back bar
            // above stays pinned.
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (state.isEditing) SkimbyteStrings.Onboarding.EDIT_TOPICS_TITLE else SkimbyteStrings.Onboarding.PICK_TOPICS_TITLE,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = SkimbyteStrings.Onboarding.PICK_TOPICS_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!state.isEditing) {
                // Expressive onboarding affordance: a wavy progress line that fills as the
                // reader approaches the three-topic minimum, then settles complete.
                LinearWavyProgressIndicator(
                    progress = { (state.selected.size / 3f).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(
                    text = when {
                        state.isEditing -> SkimbyteStrings.Onboarding.SAVE
                        state.canConfirm -> SkimbyteStrings.Onboarding.GET_STARTED
                        else -> SkimbyteStrings.Onboarding.pickMore(3 - state.selected.size)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
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
        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
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
                Icon(SkimbyteIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp))
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
            onBack = {},
        )
    }
}
