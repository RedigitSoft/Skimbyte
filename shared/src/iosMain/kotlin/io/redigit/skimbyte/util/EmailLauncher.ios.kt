package io.redigit.skimbyte.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openEmail(address: String) {
    NSURL.URLWithString("mailto:$address")?.let {
        UIApplication.sharedApplication.openURL(it, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
}