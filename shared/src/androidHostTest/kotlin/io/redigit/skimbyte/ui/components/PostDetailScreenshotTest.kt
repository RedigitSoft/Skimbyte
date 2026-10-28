package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
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
@Config(sdk = [34], qualifiers = "w400dp-h1000dp")
class PostDetailScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val post = Post(
        id = "screenshot-1",
        topic = "Kotlin",
        title = "Kotlin 2.4.21 released with faster Wasm compilation",
        summaryMarkdown = "This release improves **Kotlin/Wasm** compile times and fixes several reflection bugs.",
        sourceName = "Kotlin Releases",
        sourceUrl = "https://example.com",
        imageUrl = null,
        digestDate = "2026-10-13",
        publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
        isBookmarked = true,
    )

    // The markdown body renders asynchronously; capture only once it's actually on screen, or the
    // golden intermittently records/verifies without the summary text.
    private fun awaitMarkdownBody() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("reflection bugs", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun darkState() {
        composeTestRule.setContent {
            SkimbyteTheme {
                PostDetailScreen(post = post, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onBack = {})
            }
        }
        awaitMarkdownBody()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun lightState() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                PostDetailScreen(post = post, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onBack = {})
            }
        }
        awaitMarkdownBody()
        composeTestRule.onRoot().captureRoboImage()
    }
}
