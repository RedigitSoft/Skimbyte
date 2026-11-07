package io.redigit.skimbyte.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Receives the theme [SkimbyteTheme] actually resolved (after [ThemeMode.System] is decided), so
 * hosts that paint native chrome outside the Compose canvas — the macOS title bar on desktop — can
 * keep it in step with the app's surface color.
 */
fun interface WindowChromeListener {
    fun onThemeResolved(darkTheme: Boolean, surface: Color)
}

internal val NoOpWindowChromeListener = WindowChromeListener { _, _ -> }

/**
 * Only the outermost [SkimbyteTheme] reports to this: the theme consumes the listener by
 * re-providing the no-op to its content, so the nested light/dark sections that previews and the
 * designsheet catalog render can't repaint the host window.
 */
val LocalWindowChromeListener = staticCompositionLocalOf { NoOpWindowChromeListener }