package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.SignedOutPrompt

// The shared signed-out empty state used by the Account and Bookmarks tabs: icon, title,
// explanation, and a sign-in button so a dismissed login sheet is never a dead end.
@Composable
fun SignedOutPromptCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SignedOutPrompt(
            icon = Icons.Filled.AccountCircle,
            title = "You're not signed in",
            message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
            onSignInClick = {},
        )
        SignedOutPrompt(
            icon = Icons.Filled.Bookmark,
            title = "Your bookmarks live here",
            message = "Sign in to save stories from the feed and pick them up on any device.",
            onSignInClick = {},
        )
    }
}

@Preview
@Composable
private fun SignedOutPromptCatalogPreview() {
    SkimbyteTheme {
        SignedOutPromptCatalogContent()
    }
}
