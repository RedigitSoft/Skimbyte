package io.redigit.skimbyte.feed.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BookmarkButtonTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun idle_tapInvokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme { BookmarkButton(isBookmarked = false, isLoading = false, onClick = { clicked = true }) }
        }
        onNodeWithTag("bookmarkButton").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loading_tapIsNoOp() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme { BookmarkButton(isBookmarked = false, isLoading = true, onClick = { clicked = true }) }
        }
        onNodeWithTag("bookmarkButton").performClick()
        waitForIdle()
        assertFalse(clicked)
    }
}
