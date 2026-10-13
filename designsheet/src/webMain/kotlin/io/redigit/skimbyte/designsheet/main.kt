package io.redigit.skimbyte.designsheet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        DesignSheetApp()
    }
}
