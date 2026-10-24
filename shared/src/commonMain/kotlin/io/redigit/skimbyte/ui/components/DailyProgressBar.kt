package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.math.roundToInt

/**
 * Maps a [read]/[total] progress ratio onto at most [maxSegments] boolean "filled" flags, so the
 * segmented bar stays readable whether the feed holds 6 stories or 60. The number of segments is
 * `min(total, maxSegments)`; the filled count is the proportion of segments covered by [read],
 * rounded to the nearest segment. Returns an empty list when there's nothing to show ([total] <= 0).
 */
internal fun progressSegments(read: Int, total: Int, maxSegments: Int = 12): List<Boolean> {
    if (total <= 0 || maxSegments <= 0) return emptyList()
    val size = minOf(total, maxSegments)
    val clampedRead = read.coerceIn(0, total)
    val filled = ((clampedRead.toDouble() / total) * size).roundToInt().coerceIn(0, size)
    return List(size) { index -> index < filled }
}

/**
 * The feed's "daily brief" indicator: an eyebrow label, a `read of total` counter, and a segmented
 * capsule strip showing how far the reader has moved through the current feed. Stateless - callers
 * pass the already-computed [read] and [total] counts.
 */
@Composable
fun DailyProgressBar(
    read: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String = "Daily brief",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$read of $total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val segments = progressSegments(read, total)
        if (segments.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val filledColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                segments.forEach { filled ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (filled) filledColor else trackColor),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DailyProgressBarPreview() {
    SkimbyteTheme {
        DailyProgressBar(read = 3, total = 8)
    }
}

@Preview
@Composable
private fun DailyProgressBarLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        DailyProgressBar(read = 3, total = 8)
    }
}

@Preview
@Composable
private fun DailyProgressBarCompletePreview() {
    SkimbyteTheme {
        DailyProgressBar(read = 8, total = 8)
    }
}

@Preview
@Composable
private fun DailyProgressBarManyPreview() {
    SkimbyteTheme {
        DailyProgressBar(read = 14, total = 40)
    }
}
