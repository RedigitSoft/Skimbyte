package io.redigit.skimbyte.auth.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.SkimbyteTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LoginSheetContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun emptyFields_submitIsDisabled() = runComposeUiTest {
        var submitted = false
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetUiState(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSubmit = { submitted = true },
                )
            }
        }
        onNodeWithText("Log in").performClick()
        waitForIdle()
        assertFalse(submitted)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun filledFields_submitInvokesCallback() = runComposeUiTest {
        var submitted = false
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetUiState(email = "demo@skimbyte.app", password = "demo"),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSubmit = { submitted = true },
                )
            }
        }
        onNodeWithText("Log in").performClick()
        waitForIdle()
        assertEquals(true, submitted)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun errorState_showsErrorMessage() = runComposeUiTest {
        setContent {
            SkimbyteTheme {
                LoginSheetContent(
                    state = LoginSheetUiState(error = "Invalid email or password"),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onSubmit = {},
                )
            }
        }
        onNodeWithText("Invalid email or password").assertExists()
    }
}
