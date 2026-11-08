package io.redigit.skimbyte

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import android.net.Uri
import androidx.core.view.WindowCompat
import io.redigit.skimbyte.data.remote.BackendConfig
import io.redigit.skimbyte.data.remote.GoogleAuthCallback
import io.redigit.skimbyte.theme.LocalWindowChromeListener
import io.redigit.skimbyte.theme.WindowChromeListener
import io.redigit.skimbyte.util.AndroidContextHolder
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    private val authRedirectUri = BackendConfig.GOOGLE_REDIRECT_URI.toUri()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContextHolder.appContext = applicationContext
        handleAuthRedirect(intent)

        setContent {
            // Drive the system bar icon appearance from the *app's* resolved theme, not the OS dark
            // setting enableEdgeToEdge() keys off — otherwise light-theme-over-dark-OS leaves white
            // status-bar icons on the app's light bar. SkimbyteTheme reports the resolved theme here.
            CompositionLocalProvider(
                LocalWindowChromeListener provides WindowChromeListener { darkTheme, _ ->
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                },
            ) {
                App()
            }
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