package io.redigit.skimbyte.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.SkimbyteStrings

/**
 * Announces topics newly discovered by the catalogue sync (see `TopicsRepository.pendingNewTopics`).
 * Purely informational — new topics start unfollowed; the user adds them from Account → Interests.
 */
@Composable
fun NewTopicsDialog(
    topics: List<Topic>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = topics.joinToString { it.displayLabel }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (topics.size == 1) SkimbyteStrings.Onboarding.NEW_TOPIC_ADDED else SkimbyteStrings.Onboarding.NEW_TOPICS_ADDED) },
        text = {
            Text(
                text = SkimbyteStrings.Onboarding.newTopicsBody(labels, plural = topics.size > 1),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(SkimbyteStrings.Common.GOT_IT) }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun NewTopicsDialogPreview() {
    SkimbyteTheme(darkTheme = false) {
        NewTopicsDialog(
            topics = listOf(Topic(id = "ios-swift", displayLabel = "iOS / Swift", apiValue = "iOS / Swift")),
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun NewTopicsDialogMultipleDarkPreview() {
    SkimbyteTheme(darkTheme = true) {
        NewTopicsDialog(
            topics = listOf(
                Topic(id = "ios-swift", displayLabel = "iOS / Swift", apiValue = "iOS / Swift"),
                Topic(id = "react-native", displayLabel = "React Native", apiValue = "React Native"),
            ),
            onDismiss = {},
        )
    }
}
