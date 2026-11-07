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
import io.redigit.skimbyte.ui.SkimbyteStrings
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
                    title = SkimbyteStrings.Account.SIGNED_OUT_TITLE,
                    message = SkimbyteStrings.Account.SIGNED_OUT_MESSAGE,
                    action = StatusPromptAction(SkimbyteStrings.Common.SIGN_IN) {},
                )
                StatusPrompt(
                    icon = SkimbyteIcons.BookmarkFilled,
                    title = SkimbyteStrings.Bookmarks.SIGNED_OUT_TITLE,
                    message = SkimbyteStrings.Bookmarks.SIGNED_OUT_MESSAGE,
                    action = StatusPromptAction(SkimbyteStrings.Common.SIGN_IN) {},
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
                    title = SkimbyteStrings.Feed.LOAD_FAILED_TITLE,
                    message = SkimbyteStrings.Feed.LOAD_FAILED_NETWORK,
                    action = StatusPromptAction(SkimbyteStrings.Common.TRY_AGAIN) {},
                )
                StatusPrompt(
                    icon = SkimbyteIcons.Error,
                    title = SkimbyteStrings.Feed.LOAD_FAILED_TITLE,
                    message = SkimbyteStrings.Feed.LOAD_FAILED_UNKNOWN,
                    action = StatusPromptAction(SkimbyteStrings.Common.TRY_AGAIN) {},
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
                    title = SkimbyteStrings.Feed.EMPTY_TITLE,
                    message = SkimbyteStrings.Feed.EMPTY_MESSAGE,
                )
                StatusPrompt(
                    icon = SkimbyteIcons.BookmarkOutline,
                    title = SkimbyteStrings.Bookmarks.EMPTY_TITLE,
                    message = SkimbyteStrings.Bookmarks.EMPTY_MESSAGE,
                )
                StatusPrompt(
                    icon = SkimbyteIcons.Document,
                    title = SkimbyteStrings.Story.NO_STORY_SELECTED_TITLE,
                    message = SkimbyteStrings.Story.NO_STORY_SELECTED_MESSAGE,
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
