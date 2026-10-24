package io.redigit.skimbyte.ui.screens.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class TutorialOverlayTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun skip_invokesDismiss() = runComposeUiTest {
        var dismissed = false
        setContent { SkimbyteTheme { TutorialOverlay(onDismiss = { dismissed = true }) } }

        onNodeWithText("Skip").performClick()
        waitForIdle()
        assertEquals(true, dismissed)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun next_advancesToSecondStep() = runComposeUiTest {
        setContent { SkimbyteTheme { TutorialOverlay(onDismiss = {}) } }

        onNodeWithText("Swipe up for the next story").assertExists()
        onNodeWithText("Next").performClick()
        waitForIdle()
        onNodeWithText("Swipe sideways for topics").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gotIt_onLastStep_invokesDismiss() = runComposeUiTest {
        var dismissed = false
        setContent { SkimbyteTheme { TutorialOverlay(onDismiss = { dismissed = true }) } }

        repeat(3) {
            onNodeWithText("Next").performClick()
            waitForIdle()
        }
        onNodeWithText("Got it").performClick()
        waitForIdle()
        assertEquals(true, dismissed)
    }
}
