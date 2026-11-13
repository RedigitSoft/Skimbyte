package io.redigit.skimbyte.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.redigit.skimbyte.captureScreenshot
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class TopicChipScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statesDark() {
        composeTestRule.setContent { SkimbyteTheme { ChipStates() } }
        composeTestRule.onRoot().captureScreenshot()
    }

    @Test
    fun statesLight() {
        composeTestRule.setContent { SkimbyteTheme(darkTheme = false) { ChipStates() } }
        composeTestRule.onRoot().captureScreenshot()
    }

    /** Every state side by side, so the selected/unselected/read-only trio is locked as a set. */
    @Composable
    private fun ChipStates() {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TopicChip(label = "Selected", selected = true, onClick = {})
            TopicChip(label = "Unselected", selected = false, onClick = {})
            TopicChip(label = "Listed")
        }
    }
}
