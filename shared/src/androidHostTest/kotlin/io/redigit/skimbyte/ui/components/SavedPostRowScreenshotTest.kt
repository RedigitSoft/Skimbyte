package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h200dp")
class SavedPostRowScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val post = Post(
        id = "screenshot-1",
        topic = "Kotlin",
        title = "Kotlin 2.4.21 released with faster Wasm compilation",
        summaryMarkdown = "Summary",
        sourceName = "Kotlin Releases",
        sourceUrl = "https://example.com",
        imageUrl = null,
        digestDate = "2026-10-13",
        publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
        isBookmarked = true,
    )

    @Test
    fun darkState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SavedPostRow(post = post, onClick = {}, onRemoveClick = {}, modifier = Modifier.padding(16.dp))
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun lightState() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                SavedPostRow(post = post, onClick = {}, onRemoveClick = {}, modifier = Modifier.padding(16.dp))
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
