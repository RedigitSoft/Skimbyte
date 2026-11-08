package io.redigit.skimbyte.ui.screens.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class AppearanceScreenContentTest {

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.appearance(
        onIntent: (SettingsIntent) -> Unit = {},
    ) {
        setContent {
            SkimbyteTheme {
                AppearanceScreen(
                    state = SettingsState(themeMode = ThemeMode.System, accentColor = AccentColor.Flame),
                    onIntent = onIntent,
                    onBack = {},
                )
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectingThemeMode_emitsIntent() = runComposeUiTest {
        var intent: SettingsIntent? = null
        appearance(onIntent = { intent = it })
        onNodeWithText("Light").performClick()
        waitForIdle()
        assertEquals(SettingsIntent.ThemeModeSelected(ThemeMode.Light), intent)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectingAccent_emitsIntent() = runComposeUiTest {
        var intent: SettingsIntent? = null
        appearance(onIntent = { intent = it })
        // Each swatch exposes its accent name as its content description.
        onNodeWithContentDescription("Violet").performClick()
        waitForIdle()
        assertEquals(SettingsIntent.AccentColorSelected(AccentColor.Violet), intent)
    }
}
