package io.redigit.skimbyte.ui.screens.feed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertTrue

class SearchScreenContentTest {

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun clearButton_emitsEmptyQuery() = runComposeUiTest {
        val intents = mutableListOf<SearchIntent>()
        setContent {
            SkimbyteTheme {
                SearchScreen(
                    state = SearchState(query = "kotlin", results = emptyList()),
                    onIntent = { intents += it },
                    onCardClick = {},
                )
            }
        }
        onNodeWithContentDescription("Clear search").performClick()
        waitForIdle()
        assertTrue(intents.any { it is SearchIntent.QueryChanged && it.query == "" })
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun blankQuery_showsHint() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                SearchScreen(state = SearchState(), onIntent = {}, onCardClick = {})
            }
        }
        onNodeWithText("Search your loaded briefs by title.").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun noResults_showsNoMatchesHint() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                SearchScreen(
                    state = SearchState(query = "zzz", results = emptyList()),
                    onIntent = {},
                    onCardClick = {},
                )
            }
        }
        onNodeWithText("No matches for \"zzz\".").assertExists()
    }
}
