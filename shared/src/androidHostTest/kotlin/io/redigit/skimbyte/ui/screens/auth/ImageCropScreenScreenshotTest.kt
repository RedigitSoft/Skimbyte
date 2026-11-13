package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.redigit.skimbyte.captureScreenshot
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.AVATAR_ASPECT
import io.redigit.skimbyte.util.BANNER_ASPECT
import io.redigit.skimbyte.util.PickedImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class ImageCropScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // There are no decodable bytes under test, so these capture the cropper chrome (circular vs
    // rectangular frame + controls) rather than a photo.
    private val blank = PickedImage(bytes = ByteArray(0), mimeType = "image/png")

    @Test
    fun avatarCircularFrame() {
        composeTestRule.setContent {
            SkimbyteTheme {
                ImageCropScreen(image = blank, aspect = AVATAR_ASPECT, circularMask = true, onCancel = {}, onConfirm = {})
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun bannerRectangularFrame() {
        composeTestRule.setContent {
            SkimbyteTheme {
                ImageCropScreen(image = blank, aspect = BANNER_ASPECT, circularMask = false, onCancel = {}, onConfirm = {})
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
