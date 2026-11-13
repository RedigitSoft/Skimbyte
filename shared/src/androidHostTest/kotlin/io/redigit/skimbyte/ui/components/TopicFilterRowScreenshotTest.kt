package io.redigit.skimbyte.ui.components

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
@Config(sdk = [34])
class TopicFilterRowScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allSelected() {
        composeTestRule.setContent {
            SkimbyteTheme { TopicFilterRow(topics = Topic.defaults, selectedTopic = Topic.All, onTopicSelected = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun oneTopicSelected() {
        composeTestRule.setContent {
            SkimbyteTheme { TopicFilterRow(topics = Topic.defaults, selectedTopic = Topic.Kotlin, onTopicSelected = {}) }
        }
        composeTestRule.onRoot().captureScreenshot()
    }
}
