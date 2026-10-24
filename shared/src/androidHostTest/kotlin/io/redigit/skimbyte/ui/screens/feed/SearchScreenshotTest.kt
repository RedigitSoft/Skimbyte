package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h640dp")
class SearchScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyQuery() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SearchScreen(state = SearchState(), onIntent = {}, onCardClick = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun emptyQueryLight() {
        composeTestRule.setContent {
            SkimbyteTheme(darkTheme = false) {
                SearchScreen(state = SearchState(), onIntent = {}, onCardClick = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun noMatches() {
        composeTestRule.setContent {
            SkimbyteTheme {
                SearchScreen(state = SearchState(query = "zzz", results = emptyList()), onIntent = {}, onCardClick = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
