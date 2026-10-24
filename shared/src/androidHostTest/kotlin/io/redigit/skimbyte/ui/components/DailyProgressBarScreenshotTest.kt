package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
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
@Config(sdk = [34], qualifiers = "w400dp-h200dp")
class DailyProgressBarScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun partial() {
        composeTestRule.setContent {
            SkimbyteTheme {
                DailyProgressBar(read = 3, total = 8, modifier = Modifier.padding(16.dp))
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun complete() {
        composeTestRule.setContent {
            SkimbyteTheme {
                DailyProgressBar(read = 8, total = 8, modifier = Modifier.padding(16.dp))
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun partialLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                DailyProgressBar(read = 3, total = 8, modifier = Modifier.padding(16.dp))
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
