package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginSheetContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun emptyFields_submitIsDisabled() = runComposeUiTest {
        var submittedIntent: LoginSheetIntent? = null
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(),
                    onIntent = { submittedIntent = it },
                    onGoogleSignInClick = {},
                )
            }
        }
        onNodeWithText("Log in").performClick()
        waitForIdle()
        assertNull(submittedIntent)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun filledFields_submitInvokesOnIntentWithSubmit() = runComposeUiTest {
        var submittedIntent: LoginSheetIntent? = null
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(email = "demo@skimbyte.app", password = "demo"),
                    onIntent = { submittedIntent = it },
                    onGoogleSignInClick = {},
                )
            }
        }
        onNodeWithText("Log in").performClick()
        waitForIdle()
        assertEquals(LoginSheetIntent.Submit, submittedIntent)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetState(error = "Invalid email or password"),
                    onIntent = {},
                    onGoogleSignInClick = {},
                )
            }
        }
        onNodeWithText("Invalid email or password").assertExists()
    }
}
