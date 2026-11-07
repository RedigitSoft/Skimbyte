package io.redigit.skimbyte.theme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WindowChromeListenerTest {

    @Test
    fun darkTheme_reportsTheDarkSurface() = runComposeUiTest {
        var reported: Pair<Boolean, Color>? = null
        setContent {
            CompositionLocalProvider(
                LocalWindowChromeListener provides WindowChromeListener { dark, surface ->
                    reported = dark to surface
                },
            ) {
                SkimbyteTheme(darkTheme = true) {}
            }
        }
        waitForIdle()
        assertEquals(true to SkimbyteDarkColors.surface, reported)
    }

    @Test
    fun lightTheme_reportsTheLightSurface() = runComposeUiTest {
        var reported: Pair<Boolean, Color>? = null
        setContent {
            CompositionLocalProvider(
                LocalWindowChromeListener provides WindowChromeListener { dark, surface ->
                    reported = dark to surface
                },
            ) {
                SkimbyteTheme(darkTheme = false) {}
            }
        }
        waitForIdle()
        assertEquals(false to SkimbyteLightColors.surface, reported)
    }

    @Test
    fun switchingTheme_reportsEachResolvedSurfaceInOrder() = runComposeUiTest {
        val reported = mutableListOf<Pair<Boolean, Color>>()
        var dark by mutableStateOf(true)
        setContent {
            CompositionLocalProvider(
                LocalWindowChromeListener provides WindowChromeListener { isDark, surface ->
                    reported += isDark to surface
                },
            ) {
                SkimbyteTheme(darkTheme = dark) {}
            }
        }
        waitForIdle()
        dark = false
        waitForIdle()
        assertEquals(
            listOf(true to SkimbyteDarkColors.surface, false to SkimbyteLightColors.surface),
            reported,
        )
    }

    @Test
    fun nestedTheme_isConsumedByTheOutermostOne() = runComposeUiTest {
        // The designsheet catalog nests SkimbyteTheme(darkTheme = false) sections inside the dark
        // app theme; only the outermost theme may drive the host window's chrome.
        val reported = mutableListOf<Pair<Boolean, Color>>()
        setContent {
            CompositionLocalProvider(
                LocalWindowChromeListener provides WindowChromeListener { dark, surface ->
                    reported += dark to surface
                },
            ) {
                SkimbyteTheme(darkTheme = true) {
                    SkimbyteTheme(darkTheme = false) {}
                }
            }
        }
        waitForIdle()
        assertEquals(listOf(true to SkimbyteDarkColors.surface), reported)
    }
}