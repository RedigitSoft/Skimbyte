package io.redigit.skimbyte.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme

private data class TutorialStep(val icon: ImageVector, val title: String, val description: String)

private val tutorialSteps = listOf(
    TutorialStep(Icons.Filled.KeyboardArrowUp, "Swipe up for the next story", "The feed shows one brief at a time - swipe up to move to the next."),
    TutorialStep(Icons.Filled.SwapHoriz, "Swipe sideways for topics", "Swipe left or right, or tap a chip, to switch between your topics."),
    TutorialStep(Icons.Filled.Bookmark, "Save what matters", "Tap the bookmark on a story to keep it in your Bookmarks."),
    TutorialStep(Icons.Filled.Person, "Make it yours", "Open Profile to set your digest time, summary length and more."),
)

/**
 * First-run coaching overlay: a scrim with a stepped card explaining the core gestures. Advancing
 * past the last step (or Skip) calls [onDismiss], which the caller uses to mark the tutorial seen.
 */
@Composable
fun TutorialOverlay(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var stepIndex by remember { mutableStateOf(0) }
    val step = tutorialSteps[stepIndex]
    val isLast = stepIndex == tutorialSteps.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            // Absorb taps so the content behind the scrim isn't interactable.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(32.dp).widthIn(max = 360.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)) {
                    Icon(
                        step.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp).size(32.dp),
                    )
                }
                Text(step.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(
                    step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tutorialSteps.indices.forEach { index ->
                        val active = index == stepIndex
                        Box(
                            modifier = Modifier
                                .size(if (active) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    },
                                ),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) { Text("Skip") }
                    Button(onClick = { if (isLast) onDismiss() else stepIndex++ }) {
                        Text(if (isLast) "Got it" else "Next")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TutorialOverlayPreview() {
    SkimbyteTheme { TutorialOverlay(onDismiss = {}) }
}

@Preview
@Composable
private fun TutorialOverlayLightPreview() {
    SkimbyteTheme(darkTheme = false) { TutorialOverlay(onDismiss = {}) }
}
