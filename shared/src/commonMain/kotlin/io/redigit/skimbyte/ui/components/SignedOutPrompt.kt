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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * Shared empty-state for signed-out screens: an icon inside a tonal circle, a title, a short
 * explanation, and a prominent sign-in button. Used by the Account and Bookmarks tabs so the
 * signed-out experience looks and behaves the same everywhere — and always offers a way back
 * into the login flow (a dismissed login sheet must never be a dead end).
 */
@Composable
fun SignedOutPrompt(
    icon: ImageVector,
    title: String,
    message: String,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSignInClick,
            modifier = Modifier.widthIn(min = 200.dp).testTag("signedOutPromptSignIn"),
        ) {
            Text("Sign in")
        }
    }
}

@Preview
@Composable
private fun SignedOutPromptPreview() {
    SkimbyteTheme {
        SignedOutPrompt(
            icon = Icons.Filled.AccountCircle,
            title = "You're not signed in",
            message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
            onSignInClick = {},
        )
    }
}

@Preview
@Composable
private fun SignedOutPromptLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SignedOutPrompt(
            icon = Icons.Filled.Bookmark,
            title = "Your bookmarks live here",
            message = "Sign in to save stories and pick them up on any device.",
            onSignInClick = {},
        )
    }
}