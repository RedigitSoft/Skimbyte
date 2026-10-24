package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
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

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class AccountScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun content(dark: Boolean) {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = dark) {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
    }

    @Test
    fun loggedInDark() {
        content(dark = true)
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loggedInLight() {
        content(dark = false)
        composeTestRule.onRoot().captureRoboImage()
    }
}
