package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class AppearanceScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun content(dark: Boolean, mode: ThemeMode, accent: AccentColor) {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = dark, accent = accent) {
                AppearanceScreen(
                    state = SettingsState(themeMode = mode, accentColor = accent),
                    onIntent = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun darkState() {
        content(dark = true, mode = ThemeMode.Dark, accent = AccentColor.Emerald)
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun lightState() {
        content(dark = false, mode = ThemeMode.System, accent = AccentColor.Violet)
        composeTestRule.onRoot().captureRoboImage()
    }
}
