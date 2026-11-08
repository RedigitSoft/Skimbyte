package io.redigit.skimbyte.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.FrameWindowScope

/**
 * Keeps the macOS title bar on the theme's surface color instead of the stock white strip.
 *
 * AWT offers no way to recolor the title bar directly; what works on stock OpenJDK (12+) is making
 * it transparent via the `apple.awt.transparentTitleBar` root-pane property — the bar then shows
 * the NSWindow background — and steering the window background to the resolved surface color
 * through [LocalWindowChromeListener].
 *
 * The title text and traffic-light glyphs take their colour from the window's NSAppearance. Left to
 * the *system* appearance (`apple.awt.application.appearance`) they wash out — white on a light bar —
 * whenever the in-app theme is the opposite of the macOS one. So the listener also pins the window's
 * appearance to the resolved [ThemeMode] via the per-window `apple.awt.windowAppearance` root-pane
 * property (JDK-8265447), which updates the live window; a theme toggle re-fires it and the bar
 * follows.
 *
 * On Windows/Linux the window manager owns the bar, so this just runs [content] unchanged.
 */
@Composable
fun FrameWindowScope.ThemeAwareWindowChrome(content: @Composable () -> Unit) {
    val isMacOs = remember { System.getProperty("os.name").orEmpty().startsWith("Mac") }
    if (!isMacOs) {
        content()
        return
    }
    LaunchedEffect(window) {
        window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
    }
    CompositionLocalProvider(
        LocalWindowChromeListener provides WindowChromeListener { darkTheme, surface ->
            window.background = java.awt.Color(surface.toArgb())
            window.rootPane.putClientProperty(
                "apple.awt.windowAppearance",
                if (darkTheme) "NSAppearanceNameDarkAqua" else "NSAppearanceNameAqua",
            )
        },
        content = content,
    )
}