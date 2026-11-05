package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountScreenContentTest {

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingAvatar_invokesOnAvatarClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = { clicked = true },
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithContentDescription("View avatar").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingAvatarEditBadge_invokesOnAvatarEditClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = { clicked = true },
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithContentDescription("Change avatar").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingBanner_invokesOnBannerClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = { clicked = true },
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithContentDescription("View banner").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun avatarError_isShownToTheUser() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true, avatarError = "Could not update avatar"),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithText("Could not update avatar").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun bannerError_isShownToTheUser() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true, bannerError = "Could not update banner"),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithText("Could not update banner").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingSettingsRow_invokesOnSettingsClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = { clicked = true },
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithText("Settings").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingLogoutRow_showsConfirmationDialog_withoutInvokingOnIntent() = runComposeUiTest {
        var capturedIntent: AccountIntent? = null
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = { capturedIntent = it },
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("logoutRow").performClick()
        waitForIdle()
        onNodeWithText("Log out?").assertExists()
        assertNull(capturedIntent)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun confirmingLogoutDialog_invokesOnIntentWithLogout() = runComposeUiTest {
        var capturedIntent: AccountIntent? = null
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = { capturedIntent = it },
                    onSignInClick = {}, onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("logoutRow").performClick()
        waitForIdle()
        onNodeWithTag("confirmLogout").performClick()
        waitForIdle()
        assertEquals(AccountIntent.Logout, capturedIntent)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun dismissingLogoutDialog_doesNotInvokeOnIntent() = runComposeUiTest {
        var capturedIntent: AccountIntent? = null
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = { capturedIntent = it },
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("logoutRow").performClick()
        waitForIdle()
        onNodeWithText("Cancel").performClick()
        waitForIdle()
        onNodeWithText("Log out?").assertDoesNotExist()
        assertNull(capturedIntent)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingDeleteDataRow_showsConfirmationDialog_withoutInvokingOnIntent() = runComposeUiTest {
        var capturedIntent: AccountIntent? = null
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = { capturedIntent = it },
                    onSignInClick = {}, onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("deleteDataRow").performClick()
        waitForIdle()
        onNodeWithText("Delete my data?").assertExists()
        assertNull(capturedIntent)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun confirmingDeleteDialog_invokesOnIntentWithDeleteDataConfirmed() = runComposeUiTest {
        var capturedIntent: AccountIntent? = null
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true),
                    onIntent = { capturedIntent = it },
                    onSignInClick = {}, onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("deleteDataRow").performClick()
        waitForIdle()
        onNodeWithTag("confirmDeleteData").performClick()
        waitForIdle()
        assertEquals(AccountIntent.DeleteDataConfirmed, capturedIntent)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun deleteDataError_isShownToTheUser() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = true, deleteDataError = "Could not delete your data"),
                    onIntent = {},
                    onSignInClick = {}, onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
                )
            }
        }
        onNodeWithText("Could not delete your data").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_deleteDataRow_isNotShown() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = false),
                    onIntent = {},
                    onSignInClick = {}, onSettingsClick = {}, onAvatarClick = {}, onAvatarEditClick = {}, onBannerClick = {}, onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("deleteDataRow").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_showsSignedOutState() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = false),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithText("You're not signed in").assertExists()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_settingsStaysAvailable_logoutDoesNot() = runComposeUiTest {
        var settingsClicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = false),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = { settingsClicked = true },
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        // Settings are device-local preferences: logging out must not lock the user out of them.
        // The signed-out prompt pushes the row below the fold, so bring it into view first.
        onNodeWithText("Settings").performScrollTo().performClick()
        assertEquals(true, settingsClicked)
        onNodeWithTag("logoutRow").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_signInButton_invokesOnSignInClick() = runComposeUiTest {
        var signInClicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = false),
                    onIntent = {},
                    onSignInClick = { signInClicked = true },
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        onNodeWithTag("statusPromptAction").performClick()
        assertEquals(true, signInClicked)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_bannerAndAvatar_areNotInteractive() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    state = AccountState(isLoggedIn = false),
                    onIntent = {},
                    onSignInClick = {},
                    onSettingsClick = {},
                    onAvatarClick = {},
                    onAvatarEditClick = {},
                    onBannerClick = {},
                    onBannerEditClick = {},
                )
            }
        }
        // Signed out there is nothing to view or upload: no viewer tap targets, no camera badges.
        onNodeWithContentDescription("View banner").assertDoesNotExist()
        onNodeWithContentDescription("View avatar").assertDoesNotExist()
        onNodeWithContentDescription("Change banner").assertDoesNotExist()
        onNodeWithContentDescription("Change avatar").assertDoesNotExist()
    }
}
