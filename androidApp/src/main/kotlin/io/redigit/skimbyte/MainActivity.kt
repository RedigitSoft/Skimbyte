package io.redigit.skimbyte

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.net.Uri
import io.redigit.skimbyte.auth.AuthConfig
import io.redigit.skimbyte.auth.GoogleAuthCallback
import io.redigit.skimbyte.util.AndroidContextHolder

class MainActivity : ComponentActivity() {
    private val authRedirectUri = Uri.parse(AuthConfig.GOOGLE_REDIRECT_URI)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContextHolder.appContext = applicationContext
        handleAuthRedirect(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    // The auth server's Google OAuth callback redirects here via the matching intent-filter in
    // AndroidManifest.xml (sourced from the same AuthConfig.GOOGLE_REDIRECT_URI), with the issued
    // tokens as query params.
    private fun handleAuthRedirect(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == authRedirectUri.scheme && uri.host == authRedirectUri.host) {
            GoogleAuthCallback.onRedirect(uri.toString())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}