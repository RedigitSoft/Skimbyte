package io.redigit.skimbyte.designsheet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Skimbyte Design Sheet",
    ) {
        DesignSheetApp()
    }
}
