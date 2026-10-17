package io.redigit.skimbyte.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

// Set once from MainActivity.onCreate() before setContent — a narrow, contained use of a static
// reference purely so this one platform side-effect doesn't need a Context threaded everywhere.
object AndroidContextHolder {
    lateinit var appContext: Context
}

// Chrome Custom Tabs keeps the post's source link inside the app's task (no app-switch to a
// separate browser), falling back to a plain ACTION_VIEW if no Custom Tabs provider is installed.
actual fun openUrl(url: String) {
    val intent = CustomTabsIntent.Builder().build()
    intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.launchUrl(AndroidContextHolder.appContext, Uri.parse(url))
}
