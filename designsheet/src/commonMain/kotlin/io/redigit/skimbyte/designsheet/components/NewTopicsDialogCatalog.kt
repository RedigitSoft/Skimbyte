package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.NewTopicsDialog

private val ios = Topic(id = "ios-swift", displayLabel = "iOS / Swift", apiValue = "iOS / Swift")
private val reactNative = Topic(id = "react-native", displayLabel = "React Native", apiValue = "React Native")

// A dialog can't render inline, so the catalog launches the real component over the page in its
// two copy variants (singular and plural).
@Composable
fun NewTopicsDialogCatalogContent() {
    var shownTopics by remember { mutableStateOf<List<Topic>?>(null) }
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Shown once per catalogue sync that discovers topics the client has never seen " +
                "(TopicsRepository.pendingNewTopics). Informational only - new topics start unfollowed.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = { shownTopics = listOf(ios) }) { Text("Single topic") }
        Button(onClick = { shownTopics = listOf(ios, reactNative) }) { Text("Multiple topics") }
    }
    shownTopics?.let { topics ->
        NewTopicsDialog(topics = topics, onDismiss = { shownTopics = null })
    }
}

@Preview
@Composable
private fun NewTopicsDialogCatalogPreview() {
    SkimbyteTheme {
        NewTopicsDialogCatalogContent()
    }
}
