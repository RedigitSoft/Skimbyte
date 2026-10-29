package io.redigit.skimbyte

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    // The macOS menu bar / dock name comes from AWT, which defaults to the main class's package
    // name when launched via `gradle :desktopApp:run`; it must be set before AWT initializes.
    // Packaged builds get the name from nativeDistributions.packageName instead.
    System.setProperty("apple.awt.application.name", "Skimbyte")
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Skimbyte",
            icon = painterResource("icon.png"),
        ) {
            App()
        }
    }
}