package io.redigit.skimbyte.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.redigit.skimbyte.captureScreenshot
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h700dp")
class StatusPromptScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                StatusPrompt(
                    icon = Icons.Filled.AccountCircle,
                    title = "You're not signed in",
                    message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
                    action = StatusPromptAction("Sign in") {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun bookmarksStateLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                StatusPrompt(
                    icon = Icons.Filled.Bookmark,
                    title = "Your bookmarks live here",
                    message = "Sign in to save stories from the feed and pick them up on any device.",
                    action = StatusPromptAction("Sign in") {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    /** The actionless variant: no button, and no gap left where one would have been. */
    @Test
    fun noActionState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                StatusPrompt(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "No bookmarks yet",
                    message = "Tap the bookmark icon on any story in the feed to save it here.",
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
