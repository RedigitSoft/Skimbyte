package io.redigit.skimbyte

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.redigit.skimbyte.theme.ThemeAwareWindowChrome

fun main() {
    // The macOS menu bar / dock name comes from AWT, which defaults to the main class's package
    // name when launched via `gradle :desktopApp:run`; it must be set before AWT initializes.
    // Packaged builds get the name from nativeDistributions.packageName instead.
    System.setProperty("apple.awt.application.name", "Skimbyte")
    // Without this, AWT windows are always light Aqua, so the title text over the themed title bar
    // (ThemeAwareWindowChrome) would stay black even when macOS — and the app — are in dark mode.
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Skimbyte",
            icon = painterResource("icon.png"),
        ) {
            ThemeAwareWindowChrome {
                App()
            }
        }
    }
}