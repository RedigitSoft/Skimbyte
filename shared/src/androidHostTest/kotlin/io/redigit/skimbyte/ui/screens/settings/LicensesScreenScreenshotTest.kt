package io.redigit.skimbyte.ui.screens.settings

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
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class LicensesScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun content(dark: Boolean) {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = dark) {
                // The fake Libs previews use — the real aboutlibraries.json would make goldens
                // churn on every dependency bump.
                LicensesScreen(libraries = previewLibs(), onBack = {})
            }
        }
    }

    @Test
    fun darkState() {
        content(dark = true)
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun lightState() {
        content(dark = false)
        composeTestRule.onRoot().captureScreenshot()
    }
}
