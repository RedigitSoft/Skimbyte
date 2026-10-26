package io.redigit.skimbyte.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.data.models.Topic
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NewTopicsDialogTest {

    private val ios = Topic(id = "ios-swift", displayLabel = "iOS / Swift", apiValue = "iOS / Swift")
    private val rn = Topic(id = "react-native", displayLabel = "React Native", apiValue = "React Native")

    @Test
    fun singleTopic_showsSingularTitleAndLabel() = runComposeUiTest {
        setContent {
            SkimbyteTheme { NewTopicsDialog(topics = listOf(ios), onDismiss = {}) }
        }

        onNodeWithText("New topic added").assertExists()
        onNodeWithText("iOS / Swift", substring = true).assertExists()
    }

    @Test
    fun multipleTopics_showsPluralTitleAndAllLabels() = runComposeUiTest {
        setContent {
            SkimbyteTheme { NewTopicsDialog(topics = listOf(ios, rn), onDismiss = {}) }
        }

        onNodeWithText("New topics added").assertExists()
        onNodeWithText("iOS / Swift", substring = true).assertExists()
        onNodeWithText("React Native", substring = true).assertExists()
    }

    @Test
    fun gotIt_dismisses() = runComposeUiTest {
        var dismissed = false
        setContent {
            SkimbyteTheme { NewTopicsDialog(topics = listOf(ios), onDismiss = { dismissed = true }) }
        }

        onNodeWithText("Got it").performClick()

        assertTrue(dismissed)
    }
}
