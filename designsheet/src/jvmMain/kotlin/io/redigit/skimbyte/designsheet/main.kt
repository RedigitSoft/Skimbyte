package io.redigit.skimbyte.designsheet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.redigit.skimbyte.theme.ThemeAwareWindowChrome

fun main() {
    // Same window-chrome setup as the desktop app: title text follows the macOS appearance so it
    // stays readable over the theme-colored (ThemeAwareWindowChrome) title bar.
    System.setProperty("apple.awt.application.name", "Skimbyte Design Sheet")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Skimbyte Design Sheet",
        ) {
            ThemeAwareWindowChrome {
                DesignSheetApp()
            }
        }
    }
}