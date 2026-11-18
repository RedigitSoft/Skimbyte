package io.redigit.skimbyte.util

import java.awt.Desktop
import java.net.URI

// Desktop.mail() is the mailto-aware counterpart to browse() (which only opens web URLs); guard on
// support so a headless or unusual desktop environment degrades to a no-op rather than throwing.
actual fun openEmail(address: String) {
    runCatching {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            desktop.mail(URI("mailto:$address"))
        }
    }
}