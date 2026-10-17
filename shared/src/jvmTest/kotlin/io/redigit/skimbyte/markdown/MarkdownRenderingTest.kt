package io.redigit.skimbyte.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.mikepenz.markdown.m3.Markdown
import io.redigit.skimbyte.util.decodeHtmlEntities
import kotlin.test.Test

/**
 * The Markdown renderer does not decode HTML character references itself (confirmed by testing),
 * so real feed content (plain-prose blog summaries contain things like "&#8217;") is decoded at
 * the data layer (Post.toDomain) before it ever reaches this renderer — this test exercises that
 * full pipeline end to end rather than just the decoder in isolation.
 */
class MarkdownRenderingTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun decodedHtmlEntities_renderCorrectly() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Markdown("It&#8217;s here".decodeHtmlEntities())
            }
        }

        onNodeWithText("It’s here", substring = true).assertExists()
    }
}
