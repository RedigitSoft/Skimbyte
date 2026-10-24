package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginSheetContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun githubButton_invokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onGithubSignInClick = { clicked = true },
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        onNodeWithText("Continue with GitHub").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun googleButton_invokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onGithubSignInClick = {},
                    onGoogleSignInClick = { clicked = true },
                    onNotNowClick = {},
                )
            }
        }
        onNodeWithText("Continue with Google").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun notNow_invokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = { clicked = true },
                )
            }
        }
        onNodeWithText("Not now").performClick()
        waitForIdle()
        assertEquals(true, clicked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(error = "Google sign-in failed"),
                    onGithubSignInClick = {},
                    onGoogleSignInClick = {},
                    onNotNowClick = {},
                )
            }
        }
        onNodeWithText("Google sign-in failed").assertExists()
    }
}
