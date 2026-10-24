package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
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
        readCount = 142,
        savedCount = 18,
        streakDays = 14,
        weeklyReads = listOf(8, 12, 6, 14, 20, 9, 11),
        followedTopics = Topic.selectable.take(5),
    )

    private fun content(dark: Boolean) {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = dark) {
                AccountScreen(
                    state = richState,
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
