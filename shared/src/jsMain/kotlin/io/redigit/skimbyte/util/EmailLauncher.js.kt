package io.redigit.skimbyte.util

import kotlinx.browser.window

// Navigating the current tab to a mailto: hands off to the registered mail handler without opening
// a blank popup (which window.open("mailto:") does when no popup can show the handoff).
actual fun openEmail(address: String) {
    window.location.href = "mailto:$address"
}