package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.redigit.skimbyte.captureScreenshot
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h1000dp")
class AccountScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val richState = AccountState(
        isLoggedIn = true,
        displayName = "Alex Rivera",
        handle = "@alexrivera",
        bio = "Staff Android & Systems Engineer. Writing KMP, Rust microservices, and local LLMs.",
        isVerified = true,
        savedCount = 18,
        followedTopics = Topic.selectable.take(5),
    )

    private fun content(dark: Boolean, state: AccountState = richState) {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = dark) {
                AccountScreen(
                    state = state,
                    onIntent = {},
                    onSignInClick = {},
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
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun loggedInLight() {
        content(dark = false)
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun signedOutDark() {
        content(dark = true, state = AccountState(isLoggedIn = false))
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun signedOutLight() {
        content(dark = false, state = AccountState(isLoggedIn = false))
        composeTestRule.onRoot().captureScreenshot()
    }
}
