package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/**
 * The feed's terminal states, which had no golden coverage at all - which is how the error state
 * shipped without a retry button. [errorState] is the regression guard for that retry.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h700dp")
class FeedScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun content(feed: TopicFeed) {
        composeTestRule.setContent {
            SkimbyteTheme {
                // The app shell (App.kt MainShell) hands the feed a Scaffold, so capture it over
                // the themed background rather than the test host's bare white root.
                Surface(color = MaterialTheme.colorScheme.background) {
                    FeedScreen(
                        state = FeedState(
                            availableTopics = listOf(Topic.All, Topic.Kotlin, Topic.Android),
                            selectedTopic = Topic.All,
                            feeds = mapOf(Topic.All to feed),
                        ),
                        onIntent = {},
                        onReadSourceClick = {},
                        onCardClick = {},
                    )
                }
            }
        }
    }

    @Test
    fun errorState() {
        content(TopicFeed(error = FeedError.Network))
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun emptyState() {
        content(TopicFeed(posts = emptyList()))
        composeTestRule.onRoot().captureRoboImage()
    }
}
