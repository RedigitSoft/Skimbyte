package io.redigit.skimbyte.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.data.models.Post
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SavedPostRowTest {

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
        isBookmarked = true,
    )

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun rowClick_invokesCallbackWithPost() = runComposeUiTest {
        var clicked: Post? = null
        setContent {
            SkimbyteTheme {
                SavedPostRow(post = samplePost, onClick = { clicked = it })
            }
        }
        onNodeWithText(samplePost.title).performClick()
        waitForIdle()
        assertEquals(samplePost, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun removeClick_invokesCallback() = runComposeUiTest {
        var removed = false
        setContent {
            SkimbyteTheme {
                SavedPostRow(post = samplePost, onClick = {}, onRemoveClick = { removed = true })
            }
        }
        onNodeWithContentDescription("Remove bookmark").performClick()
        waitForIdle()
        assertEquals(true, removed)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun withoutRemoveCallback_hasNoRemoveButton() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                SavedPostRow(post = samplePost, onClick = {})
            }
        }
        onNodeWithContentDescription("Remove bookmark").assertDoesNotExist()
    }
}
