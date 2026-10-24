package io.redigit.skimbyte.ui.screens.auth

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
@Config(sdk = [34])
class LoginSheetScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun errorState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(error = "Google sign-in failed"),
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
