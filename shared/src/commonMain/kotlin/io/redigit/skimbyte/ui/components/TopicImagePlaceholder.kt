package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.math.abs

/**
 * Muted, brand-adjacent hues used to tint story-image placeholders per topic. Deliberately
 * desaturated so a feed of placeholder cards reads as designed, not as a rainbow of errors.
 */
// Seven entries, not six: with Kotlin's String.hashCode, 7 is the smallest size that maps all
// four seed topics (Kotlin / Android / KMP / Tooling) to distinct hues — see TopicAccentColorTest.
private val TopicHues = listOf(
    Color(0xFFB2674C), // terracotta (brand-adjacent)
    Color(0xFF6E8B6B), // sage
    Color(0xFF5C7A99), // steel blue
    Color(0xFF9B6B8D), // plum
    Color(0xFF8D8550), // olive gold
    Color(0xFF5F8A8B), // teal slate
    Color(0xFF75719B), // dusty indigo
)

/**
 * Stable per-topic tint: the same topic always maps to the same hue, on every platform
 * (Kotlin's String.hashCode algorithm is identical across targets). Used by the placeholder
 * below; exposed so other surfaces (chips, charts) can adopt the same coding later.
 */
fun topicAccentColor(topic: String): Color = TopicHues[abs(topic.hashCode()) % TopicHues.size]

/**
 * Branded stand-in for a story image: a topic-tinted gradient with the topic's initial.
 * Rendered *underneath* [coil3.compose.AsyncImage] wherever story images appear, so the loading
 * state, a failed fetch, and a post with no image all look intentional instead of leaving a
 * flat dark void (which reads as broken).
 */
@Composable
fun TopicImagePlaceholder(topic: String, modifier: Modifier = Modifier) {
    val base = topicAccentColor(topic)
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    base.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.surfaceVariant,
                ),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = topic.firstOrNull()?.uppercase() ?: "S",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        )
    }
}

@Preview
@Composable
private fun TopicImagePlaceholderPreview() {
    SkimbyteTheme {
        Box(modifier = Modifier.width(400.dp).height(200.dp)) {
            TopicImagePlaceholder(topic = "Kotlin", modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview
@Composable
private fun TopicImagePlaceholderLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        Box(modifier = Modifier.width(400.dp).height(200.dp)) {
            TopicImagePlaceholder(topic = "Android", modifier = Modifier.fillMaxSize())
        }
    }
}
