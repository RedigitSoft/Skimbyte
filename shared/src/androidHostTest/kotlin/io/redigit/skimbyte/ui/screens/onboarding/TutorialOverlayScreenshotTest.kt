package io.redigit.skimbyte.ui.screens.onboarding

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
class TutorialOverlayScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun firstStepDark() {
        composeTestRule.setContent { SkimbyteTheme { TutorialOverlay(onDismiss = {}) } }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun firstStepLight() {
        composeTestRule.setContent { SkimbyteTheme(darkTheme = false) { TutorialOverlay(onDismiss = {}) } }
        composeTestRule.onRoot().captureRoboImage()
    }
}
