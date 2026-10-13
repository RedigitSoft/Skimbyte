package io.redigit.skimbyte

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Pinned below compileSdk 37: Espresso's reflection-based input injection doesn't yet recognize
// this bleeding-edge simulated API level. Bump once Espresso/Robolectric catch up.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AppScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun app_defaultState() {
        composeTestRule.setContent {
            App()
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun app_contentRevealed() {
        composeTestRule.setContent {
            App()
        }
        composeTestRule.onNodeWithText("Click me!").performClick()
        composeTestRule.onRoot().captureRoboImage()
    }
}
