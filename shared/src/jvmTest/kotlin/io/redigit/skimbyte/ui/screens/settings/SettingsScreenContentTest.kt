package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
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
        onAppearanceClick: () -> Unit = {},
        onEditInterestsClick: () -> Unit = {},
        onPrivacyPolicyClick: () -> Unit = {},
        onLicensesClick: () -> Unit = {},
        isLoggedIn: Boolean = false,
        onLogout: () -> Unit = {},
        onDeleteData: () -> Unit = {},
        deleteError: String? = null,
    ) {
        setContent {
            SkimbyteTheme {
                SettingsScreen(
                    state = SettingsState(themeMode = ThemeMode.System),
                    onIntent = {},
                    onAppearanceClick = onAppearanceClick,
                    onEditInterestsClick = onEditInterestsClick,
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onLicensesClick = onLicensesClick,
                    onBack = {},
                    isLoggedIn = isLoggedIn,
                    onLogout = onLogout,
                    onDeleteData = onDeleteData,
                    deleteError = deleteError,
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
    fun tappingAppearanceRow_invokesCallback() = runComposeUiTest {
        var clicked = false
        settings(onAppearanceClick = { clicked = true })
        onNodeWithText("Theme & accent").performScrollTo().performClick()
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun accountActions_hiddenWhenSignedOut() = runComposeUiTest {
        settings(isLoggedIn = false)
        onNodeWithTag("logoutRow").assertDoesNotExist()
        onNodeWithTag("deleteDataRow").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingLogoutRow_showsDialog_andConfirmInvokesCallback() = runComposeUiTest {
        var loggedOut = false
        settings(isLoggedIn = true, onLogout = { loggedOut = true })
        onNodeWithTag("logoutRow").performScrollTo().performClick()
        waitForIdle()
        onNodeWithText("Log out?").assertExists()
        assertEquals(false, loggedOut) // dialog shown, nothing fired yet
        onNodeWithTag("confirmLogout").performClick()
        waitForIdle()
        assertEquals(true, loggedOut)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingDeleteRow_showsDialog_andConfirmInvokesCallback() = runComposeUiTest {
        var deleted = false
        settings(isLoggedIn = true, onDeleteData = { deleted = true })
        onNodeWithTag("deleteDataRow").performScrollTo().performClick()
        waitForIdle()
        onNodeWithText("Delete my data?").assertExists()
        assertEquals(false, deleted)
        onNodeWithTag("confirmDeleteData").performClick()
        waitForIdle()
        assertEquals(true, deleted)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deleteError_isShownToTheUser() = runComposeUiTest {
        settings(isLoggedIn = true, deleteError = "Could not delete your data")
        onNodeWithText("Could not delete your data").performScrollTo().assertExists()
    }
}
