package io.redigit.skimbyte.auth.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

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
                    onLoginSucceeded = {},
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
                    onLoginSucceeded = {},
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
                    onLoginSucceeded = {},
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
}
