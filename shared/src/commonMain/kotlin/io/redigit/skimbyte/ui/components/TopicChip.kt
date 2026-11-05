package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteShapeTokens
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * The minimum edge length of anything the user is expected to tap, per WCAG 2.5.5 (Target Size) and
 * the Material 3 accessibility guidance.
 *
 * TODO(RED-5): move to the shared `Dimens`/token layer once it lands, so this stops being the only
 *  place in the app that knows the number.
 */
internal val MinTouchTarget = 48.dp

/**
 * A topic rendered as a chip — the single way Skimbyte draws one. The feed's [TopicFilterRow] and
 * the account screen's followed-topics list both come through here, so a topic looks and is
 * announced the same way wherever it appears.
 *
 * This interactive overload is built on [FilterChip] rather than a hand-rolled `Surface` +
 * `clickable`, for two reasons:
 *
 * - **Selection semantics come for free.** A raw `clickable` carries no selected state, so a screen
 *   reader reads a filter row as a run of identical buttons and never says which topic is active.
 *   [FilterChip] exposes real selectable semantics.
 * - **Both states get a container.** The unselected state used to be bare text with no container at
 *   all, leaving the row reading as one filled pill adrift in loose text rather than as a set;
 *   [FilterChip] gives the unselected state an outline.
 *
 * The chip is floored at [MinTouchTarget]. The floor is a constraint on the chip itself, so the
 * chip's own clickable surface grows to fill it and the whole padded pill is tappable, not just the
 * text inside it.
 */
@Composable
fun TopicChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        shape = SkimbyteShapeTokens.pill,
        colors = topicChipColors(),
        modifier = modifier.defaultMinSize(minHeight = MinTouchTarget, minWidth = MinTouchTarget),
    )
}

/**
 * A topic chip with nothing to tap, for listing topics rather than filtering by them — the account
 * screen shows what the user follows and has nowhere to send a tap.
 *
 * Deliberately not a [FilterChip]: a chip with a no-op `onClick` would hand assistive technology an
 * action that does nothing and would claim a 48dp touch target it has no use for, and a *disabled*
 * chip would say these topics are unavailable, which is not what is being shown. It keeps the
 * chip's shape and type so the two still read as the same object.
 */
@Composable
fun TopicChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = SkimbyteShapeTokens.pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * [FilterChip]'s defaults, except that the selected chip keeps Skimbyte's accent. M3 fills a
 * selected filter chip with `secondaryContainer`, a role this app's dark scheme does not define —
 * taking the default would swap the brand coral for Material's baseline violet-gray. The unselected
 * container, label and outline are left on the M3 roles so they pick up the rest of the palette as
 * RED-13 completes it.
 */
@Composable
private fun topicChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
)

@Preview
@Composable
private fun TopicChipSelectedPreview() {
    SkimbyteTheme {
        TopicChip(label = "Kotlin", selected = true, onClick = {})
    }
}

@Preview
@Composable
private fun TopicChipUnselectedPreview() {
    SkimbyteTheme {
        TopicChip(label = "Kotlin", selected = false, onClick = {})
    }
}

@Preview
@Composable
private fun TopicChipSelectedLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicChip(label = "Kotlin", selected = true, onClick = {})
    }
}

@Preview
@Composable
private fun TopicChipUnselectedLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicChip(label = "Kotlin", selected = false, onClick = {})
    }
}

@Preview
@Composable
private fun TopicChipReadOnlyPreview() {
    SkimbyteTheme {
        TopicChip(label = "Kotlin")
    }
}

@Preview
@Composable
private fun TopicChipReadOnlyLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        TopicChip(label = "Kotlin")
    }
}
