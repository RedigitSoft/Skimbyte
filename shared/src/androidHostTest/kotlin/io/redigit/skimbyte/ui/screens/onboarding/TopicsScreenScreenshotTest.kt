package io.redigit.skimbyte.ui.screens.onboarding

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

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp")
class TopicsScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboardingPartialSelectionDark() {
        composeTestRule.setContent {
            SkimbyteTheme {
                TopicsScreen(
                    state = TopicsState(selected = setOf(Topic.Kotlin, Topic.Android)),
                    onIntent = {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun editingLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                TopicsScreen(
                    state = TopicsState(
                        selected = setOf(Topic.Kotlin, Topic.Android, Topic.Kmp),
                        isEditing = true,
                    ),
                    onIntent = {},
                )
            }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
