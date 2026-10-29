package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.TopicImagePlaceholder

// The branded story-image stand-in rendered under every AsyncImage (PostCard, PostDetail):
// a stable per-topic tint + the topic initial, covering loading, fetch failure, and
// image-less posts. One row per seed topic shows the hue coding.
@Composable
fun TopicImagePlaceholderCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf("Kotlin", "Android", "KMP / Compose Multiplatform", "Tooling & Products").forEach { topic ->
            Text(topic, style = MaterialTheme.typography.labelMedium)
            TopicImagePlaceholder(
                topic = topic,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.large),
            )
        }
    }
}

@Preview
@Composable
private fun TopicImagePlaceholderCatalogPreview() {
    SkimbyteTheme {
        TopicImagePlaceholderCatalogContent()
    }
}
