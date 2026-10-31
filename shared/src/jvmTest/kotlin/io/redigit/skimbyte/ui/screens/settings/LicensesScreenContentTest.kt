package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class LicensesScreenContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun librariesFromTheMetadata_areListed() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                LicensesScreen(libraries = previewLibs(), onBack = {})
            }
        }
        onNodeWithText("Library One").assertExists()
        onNodeWithText("Library Two").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun backButton_invokesOnBack() = runComposeUiTest {
        var backClicked = false
        setContent {
            SkimbyteTheme {
                LicensesScreen(libraries = previewLibs(), onBack = { backClicked = true })
            }
        }
        onNodeWithTag("licensesBack").performClick()
        waitForIdle()
        assertEquals(true, backClicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullLibraries_showsLoadingWithoutListingAnything() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                LicensesScreen(libraries = null, onBack = {})
            }
        }
        onNodeWithText("Library One").assertDoesNotExist()
    }
}
