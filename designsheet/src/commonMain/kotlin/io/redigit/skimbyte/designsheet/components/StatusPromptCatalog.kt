package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.CatalogSection
import io.redigit.skimbyte.theme.SkimbyteIcons
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.components.StatusPrompt
import io.redigit.skimbyte.ui.components.StatusPromptAction

// The single composition behind every terminal state in the app: icon, title, explanation, and an
// optional button out. Every case the app actually ships is catalogued here so the whole set can be
// compared side by side - a recoverable state with no action is the bug this component prevents.
@Composable
fun StatusPromptCatalogContent() {
    CatalogPage {
        CatalogSection(
            title = "Signed out",
            description = "The Account and Bookmarks tabs when no user is signed in.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                StatusPrompt(
                    icon = SkimbyteIcons.AccountCircle,
                    title = "You're not signed in",
                    message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
                    action = StatusPromptAction("Sign in") {},
                )
                StatusPrompt(
                    icon = SkimbyteIcons.BookmarkFilled,
                    title = "Your bookmarks live here",
                    message = "Sign in to save stories from the feed and pick them up on any device.",
                    action = StatusPromptAction("Sign in") {},
                )
            }
        }

        CatalogSection(
            title = "Recoverable errors",
            description = "First-load failures. Pull-to-refresh can't register without a scrollable " +
                "child, so the button is the only way back - these states are never rendered without it.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                StatusPrompt(
                    icon = SkimbyteIcons.Offline,
                    title = "Couldn't load posts",
                    message = "Check your connection and try again.",
                    action = StatusPromptAction("Try again") {},
                )
                StatusPrompt(
                    icon = SkimbyteIcons.Error,
                    title = "Couldn't load posts",
                    message = "Something went wrong on our end. Try again in a moment.",
                    action = StatusPromptAction("Try again") {},
                )
            }
        }

        CatalogSection(
            title = "Empty states",
            description = "Terminal states with nothing to act on: no button, but still an icon and an explanation.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                StatusPrompt(
                    icon = SkimbyteIcons.EmptyInbox,
                    title = "No posts yet",
                    message = "New stories for this topic will show up here as they're published.",
                )
                StatusPrompt(
                    icon = SkimbyteIcons.BookmarkOutline,
                    title = "No bookmarks yet",
                    message = "Tap the bookmark icon on any story in the feed to save it here.",
                )
                StatusPrompt(
                    icon = SkimbyteIcons.Document,
                    title = "No story selected",
                    message = "Pick a story from the list to read it here.",
                )
            }
        }
    }
}

@Preview
@Composable
private fun StatusPromptCatalogPreview() {
    SkimbyteTheme {
        StatusPromptCatalogContent()
    }
}

@Preview
@Composable
private fun StatusPromptCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        StatusPromptCatalogContent()
    }
}
