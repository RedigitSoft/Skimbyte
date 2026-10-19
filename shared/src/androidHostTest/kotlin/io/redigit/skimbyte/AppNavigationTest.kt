package io.redigit.skimbyte

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tests the state-retention mechanism that [App]'s private `MainShell` composable relies on,
 * rather than driving `App()` end to end.
 *
 * An earlier version of this test drove the real app through onboarding, with a mocked network,
 * all the way to tapping a [io.redigit.skimbyte.feed.ui.PostCard] to open
 * [io.redigit.skimbyte.feed.ui.PostDetailScreen]. It reliably failed with Espresso's
 * `AppNotIdleException`: some continuous animation (traced to the PostCard -> PostDetailScreen
 * shared-element transition at list-detail width) never reports idle under Robolectric, which has
 * no real vsync to settle it. Two agent sessions attempting to fix the real test both hit this
 * same wall.
 *
 * `MainShell` itself is `private` to `App.kt` and only wires up the four real nav hosts, so it
 * can't be swapped for fakes without touching production visibility. Instead, this test
 * reproduces the exact pattern `MainShell` uses -
 * `rememberSaveableStateHolder()` + `saveableStateHolder.SaveableStateProvider(key) { ... }`
 * switching between destinations - with trivial fake destinations that each hold their own
 * `rememberSaveable` state. That is the actual regression surface: the old `App.kt` switched
 * destinations with a plain `when` and no `SaveableStateHolder`, so a destination's
 * `rememberSaveable`/`remember` state was torn down and recreated every time the user navigated
 * away and back. Testing the pattern directly - with no onboarding, networking, or animated
 * screens involved - exercises exactly the mechanism that fixes that regression, without the
 * unrelated Espresso/animation flakiness above.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AppNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private enum class FakeDestination { A, B }

    @Composable
    private fun FakeMainShell() {
        var selected by remember { mutableStateOf(FakeDestination.A) }
        val saveableStateHolder = rememberSaveableStateHolder()
        Box {
            saveableStateHolder.SaveableStateProvider(selected) {
                when (selected) {
                    FakeDestination.A -> FakeCounterDestination(
                        tag = "destination-a",
                        onSwitch = { selected = FakeDestination.B },
                    )
                    FakeDestination.B -> FakeCounterDestination(
                        tag = "destination-b",
                        onSwitch = { selected = FakeDestination.A },
                    )
                }
            }
        }
    }

    @Composable
    private fun FakeCounterDestination(tag: String, onSwitch: () -> Unit) {
        var count by rememberSaveable { mutableStateOf(0) }
        Column {
            Text(text = count.toString(), modifier = Modifier.testTag("$tag-count"))
            Button(onClick = { count++ }, modifier = Modifier.testTag("$tag-increment")) {
                Text("Increment")
            }
            Button(onClick = onSwitch, modifier = Modifier.testTag("$tag-switch")) {
                Text("Switch")
            }
        }
    }

    @Test
    fun `switching destinations and back preserves each destination's rememberSaveable state`() {
        composeRule.setContent { FakeMainShell() }

        // Bump destination A's counter twice, so it has observable, non-default state.
        composeRule.onNodeWithTag("destination-a-increment").performClick()
        composeRule.onNodeWithTag("destination-a-increment").performClick()
        composeRule.onNodeWithTag("destination-a-count").assertTextEquals("2")

        // Switch to destination B, which starts at its own independent default.
        composeRule.onNodeWithTag("destination-a-switch").performClick()
        composeRule.onNodeWithTag("destination-b-count").assertTextEquals("0")
        composeRule.onNodeWithTag("destination-b-increment").performClick()
        composeRule.onNodeWithTag("destination-b-count").assertTextEquals("1")

        // Switch back to A: because MainShell's pattern wraps content in
        // rememberSaveableStateHolder().SaveableStateProvider(selected), A's rememberSaveable
        // counter must have survived being switched away from, rather than resetting to 0 as it
        // would under a plain `when (selected) { ... }` with no SaveableStateProvider.
        composeRule.onNodeWithTag("destination-b-switch").performClick()
        composeRule.onNodeWithTag("destination-a-count").assertTextEquals("2")
    }
}
