package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The feed's error state used to be a dead end: a transient network failure on app open left the
 * reader with no way back except relaunching the app. Pull-to-refresh can't rescue them either -
 * `PullToRefreshBox` needs a scrollable child and the error state is a plain `Box` - so the retry
 * button is the only recovery, and these tests hold it in place.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalTestApi::class)
class FeedScreenTest {

    private fun feedState(feed: TopicFeed) = FeedState(
        availableTopics = listOf(Topic.All, Topic.Kotlin),
        selectedTopic = Topic.All,
        feeds = mapOf(Topic.All to feed),
    )

    @Test
    fun errorState_offersRetry_whichRequestsARefetch() = runComposeUiTest {
        val intents = mutableListOf<FeedIntent>()
        setContent {
            SkimbyteTheme {
                FeedScreen(
                    state = feedState(TopicFeed(error = FeedError.Network)),
                    onIntent = { intents += it },
                    onReadSourceClick = {},
                    onCardClick = {},
                )
            }
        }
        onNodeWithTag("statusPromptAction").assertIsDisplayed()
        onNodeWithText("Try again").performClick()
        waitForIdle()
        assertEquals(listOf<FeedIntent>(FeedIntent.Refresh), intents)
    }

    /** An empty feed is not a failure, so it gets an explanation but no action to take. */
    @Test
    fun emptyState_hasNoAction() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                FeedScreen(
                    state = feedState(TopicFeed(posts = emptyList())),
                    onIntent = {},
                    onReadSourceClick = {},
                    onCardClick = {},
                )
            }
        }
        onNodeWithText("No posts yet").assertIsDisplayed()
        onNodeWithTag("statusPromptAction").assertDoesNotExist()
    }
}
