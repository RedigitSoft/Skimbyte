package io.redigit.skimbyte.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TopicChipTest {

    @Test
    fun interactiveChip_reportsSelectionState() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                TopicChip(label = "Kotlin", selected = true, onClick = {})
                TopicChip(label = "Android", selected = false, onClick = {})
            }
        }
        onNodeWithText("Kotlin").assertIsSelected()
        onNodeWithText("Android").assertIsNotSelected()
    }

    /**
     * The defect this component was created to fix: the hand-rolled chips were roughly 36dp tall,
     * under the 48dp minimum, and the whole chip — not just the text inside it — has to be tappable.
     *
     * The 48dp is written out rather than read from `MinTouchTarget`, so that lowering the
     * component's own constant fails this test instead of moving the goalposts with it. A short
     * label is used deliberately: it is the case where the width floor, not the text, decides the
     * chip's size.
     */
    @Test
    fun interactiveChip_meetsMinimumTouchTarget() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                TopicChip(label = "AI", selected = false, onClick = {})
            }
        }
        onNodeWithText("AI").apply {
            assertHasClickAction()
            assertHeightIsAtLeast(48.dp)
            assertWidthIsAtLeast(48.dp)
        }
    }

    @Test
    fun interactiveChip_tapInvokesCallback() = runComposeUiTest {
        var taps = 0
        setContent {
            SkimbyteTheme {
                TopicChip(label = "Kotlin", selected = false, onClick = { taps++ })
            }
        }
        onNodeWithText("Kotlin").performClick()
        waitForIdle()
        assertEquals(1, taps)
    }

    /**
     * A read-only chip must not offer assistive technology an action that does nothing, and must
     * not claim a touch target it has no use for.
     */
    @Test
    fun readOnlyChip_hasNoClickAction() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                TopicChip(label = "Kotlin")
            }
        }
        onNodeWithText("Kotlin").assertHasNoClickAction()
    }
}
