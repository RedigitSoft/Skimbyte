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
 * through [LocalWindowChromeListener]. The title text follows the *system* appearance (see the
 * `apple.awt.application.appearance` property set in each entry point), so it can wash out when the
 * in-app theme is forced opposite to the OS; the window keeps the right surface color regardless.
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
        LocalWindowChromeListener provides WindowChromeListener { _, surface ->
            window.background = java.awt.Color(surface.toArgb())
        },
        content = content,
    )
}