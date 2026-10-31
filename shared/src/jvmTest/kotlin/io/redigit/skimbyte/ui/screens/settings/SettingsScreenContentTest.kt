package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsScreenContentTest {

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.settings(
        onEditInterestsClick: () -> Unit = {},
        onPrivacyPolicyClick: () -> Unit = {},
        onLicensesClick: () -> Unit = {},
    ) {
        setContent {
            SkimbyteTheme {
                SettingsScreen(
                    state = SettingsState(themeMode = ThemeMode.System),
                    onIntent = {},
                    onEditInterestsClick = onEditInterestsClick,
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onLicensesClick = onLicensesClick,
                    onBack = {},
                )
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingPrivacyPolicyRow_invokesCallback() = runComposeUiTest {
        var clicked = false
        settings(onPrivacyPolicyClick = { clicked = true })
        onNodeWithText("Privacy policy").performScrollTo().performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingLicensesRow_invokesCallback() = runComposeUiTest {
        var clicked = false
        settings(onLicensesClick = { clicked = true })
        onNodeWithText("Open source licenses").performScrollTo().performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingEditInterestsRow_invokesCallback() = runComposeUiTest {
        var clicked = false
        settings(onEditInterestsClick = { clicked = true })
        onNodeWithText("Edit interests").performScrollTo().performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }
}
