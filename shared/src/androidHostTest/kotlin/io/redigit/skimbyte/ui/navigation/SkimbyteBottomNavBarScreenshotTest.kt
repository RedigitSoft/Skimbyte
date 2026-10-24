package io.redigit.skimbyte.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
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
@Config(sdk = [34], qualifiers = "w420dp-h200dp")
class SkimbyteBottomNavBarScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun feedSelected() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SkimbyteBottomNavBar(
                    selected = TopLevelDestination.Feed,
                    onDestinationSelected = {},
                    expanded = true,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun bookmarksSelectedLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                SkimbyteBottomNavBar(
                    selected = TopLevelDestination.Bookmarks,
                    onDestinationSelected = {},
                    expanded = true,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
