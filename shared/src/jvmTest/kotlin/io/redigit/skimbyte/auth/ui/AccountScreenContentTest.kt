package io.redigit.skimbyte.auth.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AccountScreenContentTest {

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun tappingAvatar_invokesOnAvatarClick() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = {},
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
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = {},
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
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = {},
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
                    uiState = AccountUiState(isLoggedIn = true, avatarError = "Could not update avatar"),
                    onLogout = {},
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
                    uiState = AccountUiState(isLoggedIn = true, bannerError = "Could not update banner"),
                    onLogout = {},
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
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = {},
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
    fun tappingLogoutRow_showsConfirmationDialog_withoutInvokingOnLogout() = runComposeUiTest {
        var loggedOut = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = { loggedOut = true },
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
        assertFalse(loggedOut)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun confirmingLogoutDialog_invokesOnLogout() = runComposeUiTest {
        var loggedOut = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = { loggedOut = true },
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
        onNodeWithTag("confirmLogout").performClick()
        waitForIdle()
        assertEquals(true, loggedOut)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun dismissingLogoutDialog_doesNotInvokeOnLogout() = runComposeUiTest {
        var loggedOut = false
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = true),
                    onLogout = { loggedOut = true },
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
        assertFalse(loggedOut)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalSharedTransitionApi::class)
    @Test
    fun loggedOut_showsSignedOutState() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                AccountScreen(
                    uiState = AccountUiState(isLoggedIn = false),
                    onLogout = {},
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
}
