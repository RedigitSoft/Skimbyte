package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.material3.Surface
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
@Config(sdk = [34], qualifiers = "w400dp-h400dp")
class ImageEditSheetScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dark() {
        composeTestRule.setContent {
            SkimbyteTheme {
                Surface {
                    ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
                }
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun light() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                Surface {
                    ImageEditSheetContent(onTakePhoto = {}, onChooseFromGallery = {}, onRemove = {})
                }
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
