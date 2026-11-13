package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
class AvatarWithEditBadgeScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun noAvatar() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Idle, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun compressing() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Compressing, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun uploading() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Uploading, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun error() {
        composeTestRule.setContent {
            SkimbyteTheme { AvatarWithEditBadge(avatarUrl = null, uploadState = ImageUploadState.Error, onAvatarClick = {}, onEditClick = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
