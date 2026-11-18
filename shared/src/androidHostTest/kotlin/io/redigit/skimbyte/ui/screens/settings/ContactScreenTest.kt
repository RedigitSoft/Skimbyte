package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ContactScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsTheContactEmailAsVisibleText() {
        composeRule.setContent {
            SkimbyteTheme {
                ContactScreen(
                    onBack = {},
                    onSendEmail = {},
                    onWebsiteClick = {},
                    onIssuesClick = {},
                    email = "hello@example.com",
                )
            }
        }
        // The Play News policy hinges on the address being shown in-app, not just behind a mailto.
        composeRule.onNodeWithTag("contactEmail").assertTextEquals("hello@example.com")
    }

    @Test
    fun eachActionInvokesItsCallback() {
        var sent = 0
        var website = 0
        var issues = 0
        var back = 0
        composeRule.setContent {
            SkimbyteTheme {
                ContactScreen(
                    onBack = { back++ },
                    onSendEmail = { sent++ },
                    onWebsiteClick = { website++ },
                    onIssuesClick = { issues++ },
                )
            }
        }

        // Rows live in a vertical scroll container and may start below the fold — scroll each into
        // view before clicking. The back button is in the (always-visible) top bar.
        composeRule.onNodeWithTag("sendEmailButton").performScrollTo().performClick()
        composeRule.onNodeWithTag("contactWebsiteRow").performScrollTo().performClick()
        composeRule.onNodeWithTag("contactIssuesRow").performScrollTo().performClick()
        composeRule.onNodeWithTag("contactBack").performClick()

        assertEquals(1, sent)
        assertEquals(1, website)
        assertEquals(1, issues)
        assertEquals(1, back)
    }

    @Test
    fun settingsAboutSectionLinksToContact() {
        var contactOpened = false
        composeRule.setContent {
            SkimbyteTheme {
                SettingsScreen(
                    state = SettingsState(),
                    onIntent = {},
                    onAppearanceClick = {},
                    onEditInterestsClick = {},
                    onContactClick = { contactOpened = true },
                    onPrivacyPolicyClick = {},
                    onLicensesClick = {},
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithTag("contactRow").performClick()
        assertTrue(contactOpened)
    }
}