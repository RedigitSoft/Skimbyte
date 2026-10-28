package io.redigit.skimbyte.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h700dp")
class SignedOutPromptScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SignedOutPrompt(
                    icon = Icons.Filled.AccountCircle,
                    title = "You're not signed in",
                    message = "Sign in to sync your profile, bookmarks, and reading stats across devices.",
                    onSignInClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun bookmarksStateLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                SignedOutPrompt(
                    icon = Icons.Filled.Bookmark,
                    title = "Your bookmarks live here",
                    message = "Sign in to save stories from the feed and pick them up on any device.",
                    onSignInClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
