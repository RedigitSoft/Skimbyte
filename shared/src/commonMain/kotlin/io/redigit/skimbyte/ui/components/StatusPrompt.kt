package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme

/** The button on a [StatusPrompt] that recovers from the state the prompt describes. */
data class StatusPromptAction(val label: String, val onClick: () -> Unit)

/**
 * The one composition for every terminal state in the app - empty, error, signed-out,
 * nothing-selected: an icon inside a tonal circle, a title, a short explanation, and an optional
 * button that gets the user out of the state.
 *
 * Every terminal state goes through here so they look and read the same everywhere, instead of
 * each screen inventing its own centred sentence. Supply an [action] whenever the state is
 * recoverable - a dismissed login sheet or a failed fetch must never be a dead end.
 */
@Composable
fun StatusPrompt(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: StatusPromptAction? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = action.onClick,
                modifier = Modifier.widthIn(min = 200.dp).testTag("statusPromptAction"),
            ) {
                Text(action.label)
            }
        }
    }
}

@Preview
@Composable
private fun StatusPromptPreview() {
    SkimbyteTheme {
        StatusPrompt(
            icon = SkimbyteIcons.AccountCircle,
            title = "You're not signed in",
            message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
            action = StatusPromptAction("Sign in") {},
        )
    }
}

@Preview
@Composable
private fun StatusPromptErrorLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        StatusPrompt(
            icon = SkimbyteIcons.Offline,
            title = "Couldn't load posts",
            message = "Check your connection and try again.",
            action = StatusPromptAction("Try again") {},
        )
    }
}

/** A terminal state with nothing to act on still gets an icon, a title, and an explanation. */
@Preview
@Composable
private fun StatusPromptNoActionPreview() {
    SkimbyteTheme {
        StatusPrompt(
            icon = SkimbyteIcons.EmptyInbox,
            title = "No posts yet",
            message = "New stories for this topic will show up here.",
        )
    }
}
