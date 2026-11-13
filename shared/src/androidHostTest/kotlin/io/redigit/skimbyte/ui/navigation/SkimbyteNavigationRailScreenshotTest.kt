package io.redigit.skimbyte.ui.navigation

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
@Config(sdk = [34], qualifiers = "w200dp-h640dp")
class SkimbyteNavigationRailScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feedSelected() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SkimbyteNavigationRail(selected = TopLevelDestination.Feed, onDestinationSelected = {})
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun bookmarksSelectedLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                SkimbyteNavigationRail(selected = TopLevelDestination.Bookmarks, onDestinationSelected = {})
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
