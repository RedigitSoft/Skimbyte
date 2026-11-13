package io.redigit.skimbyte.ui.screens.auth

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
class LoginSheetScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onAppleSignInClick = {},
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun errorState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(error = "Google sign-in failed"),
                    onAppleSignInClick = {},
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
