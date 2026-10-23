package io.redigit.skimbyte.ui.components

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

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AvatarWithEditBadgeScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun noAvatar() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = false, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun uploading() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, isUploading = true, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
