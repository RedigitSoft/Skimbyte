package io.redigit.skimbyte.util

import android.content.Intent
import android.net.Uri

// ACTION_SENDTO with a mailto: URI targets only email apps (unlike ACTION_VIEW), so the chooser
// never offers a browser. FLAG_ACTIVITY_NEW_TASK is required because we launch from the
// application context rather than an Activity.
actual fun openEmail(address: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { AndroidContextHolder.appContext.startActivity(intent) }
}