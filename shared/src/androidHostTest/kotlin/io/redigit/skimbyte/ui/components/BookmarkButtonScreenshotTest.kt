package io.redigit.skimbyte.ui.components

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
@Config(sdk = [34])
class BookmarkButtonScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun idleState() {
        composeTestRule.setContent {
            SkimbyteTheme { BookmarkButton(isBookmarked = false, isLoading = false, onClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun bookmarkedState() {
        composeTestRule.setContent {
            SkimbyteTheme { BookmarkButton(isBookmarked = true, isLoading = false, onClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun loadingState() {
        composeTestRule.setContent {
            SkimbyteTheme { BookmarkButton(isBookmarked = false, isLoading = true, onClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
