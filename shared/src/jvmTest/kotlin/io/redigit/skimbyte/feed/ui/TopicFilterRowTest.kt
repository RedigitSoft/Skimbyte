package io.redigit.skimbyte.feed.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.posts.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class TopicFilterRowTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingChip_invokesCallback_withThatTopic() = runComposeUiTest {
        var selected: Topic? = null
        setContent {
            SkimbyteTheme {
                TopicFilterRow(selectedTopic = Topic.All, onTopicSelected = { selected = it })
            }
        }
        onNodeWithText("Kotlin").performClick()
        waitForIdle()
        assertEquals(Topic.Kotlin, selected)
    }
}
