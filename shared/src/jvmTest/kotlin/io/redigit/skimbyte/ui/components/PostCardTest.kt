package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PostCardTest {

    private val samplePost = Post(
        id = "post-1",
        topic = "Kotlin",
        title = "Kotlin 2.4.21 released",
        summaryMarkdown = "Summary text",
        sourceName = "Kotlin Releases",
        sourceUrl = "https://example.com/post-1",
        imageUrl = null,
        digestDate = "2026-10-13",
        publishedAt = Instant.parse("2026-10-12T18:00:00Z"),
        isBookmarked = false,
    )

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun bookmarkClick_invokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                PostCard(post = samplePost, isBookmarkLoading = false, onBookmarkClick = { clicked = true }, onReadSourceClick = {}, onCardClick = {})
            }
        }
        onNodeWithTag("bookmarkButton").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun readSourceClick_invokesCallbackWithSourceUrl() = runComposeUiTest {
        var clickedUrl: String? = null
        setContent {
            SkimbyteTheme {
                PostCard(post = samplePost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = { clickedUrl = it }, onCardClick = {})
            }
        }
        onNodeWithText("Read full source").performClick()
        waitForIdle()
        assertEquals(samplePost.sourceUrl, clickedUrl)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun cardClick_invokesCallbackWithPostId() = runComposeUiTest {
        var clickedId: String? = null
        setContent {
            SkimbyteTheme {
                PostCard(post = samplePost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = { clickedId = it })
            }
        }
        onNodeWithText(samplePost.title).performClick()
        waitForIdle()
        assertEquals(samplePost.id, clickedId)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun bookmarkedState_showsRemoveBookmarkIcon() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                PostCard(post = samplePost.copy(isBookmarked = true), isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
            }
        }
        onNodeWithContentDescription("Remove bookmark").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun notBookmarkedState_showsBookmarkIcon() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                PostCard(post = samplePost, isBookmarkLoading = false, onBookmarkClick = {}, onReadSourceClick = {}, onCardClick = {})
            }
        }
        onNodeWithContentDescription("Bookmark").assertExists()
    }
}
