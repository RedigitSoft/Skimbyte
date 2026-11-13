package io.redigit.skimbyte.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ktor.http.URLBuilder
import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.ui.SkimbyteStrings
import io.redigit.skimbyte.util.authRedirectUri
import io.redigit.skimbyte.util.openUrl
import org.koin.compose.viewmodel.koinViewModel

// AuthConfig.GOOGLE_REDIRECT_URI is validated server-side against its allowlisted mobile redirect
// URIs (auth.oauth.allowedMobileRedirectUris) and forwarded back with the issued tokens — captured
// on Android via the matching intent-filter in AndroidManifest.xml (also sourced from
// local.properties, via androidApp's manifestPlaceholders).
private fun googleSignInStartUrl(): String = oauthStartUrl("google")

// GitHub OAuth uses the same server-side shape as Google (a provider-specific /start that redirects
// back to the shared mobile redirect URI with tokens), so the start URLs differ only by provider
// and the redirect is handled by the one GoogleAuthCallback path. Gated by ApiConfig.GITHUB_SIGN_IN_ENABLED.
private fun githubSignInStartUrl(): String = oauthStartUrl("github")

// Sign in with Apple also goes through the server (the /start endpoint drives Apple's web OAuth
// flow and redirects back to the shared mobile redirect URI), so no Apple SDK is needed in the
// client on any platform. Gated by ApiConfig.APPLE_SIGN_IN_ENABLED.
private fun appleSignInStartUrl(): String = oauthStartUrl("apple")

private fun oauthStartUrl(provider: String): String =
    URLBuilder("${ApiConfig.AUTH_BASE_URL}/auth/oauth/$provider/start").apply {
        // Platform-aware: mobile passes the deep link, desktop its RFC 8252 loopback listener
        // (see authRedirectUri) — each must be in the server's exact-match allowlist.
        parameters.append("redirect_uri", authRedirectUri())
        parameters.append("product_id", ApiConfig.PRODUCT_ID)
    }.buildString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSheet(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginSheetViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    // Redirect completion is app-level (AuthRedirectHandler, launched in App) — the sheet only
    // listens for the outcome via its view model, so it works whether or not it's open.
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginSheetEffect.LoginSucceeded -> onLoginSuccess()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(LoginSheetIntent.Dismissed)
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        LoginSheetContent(
            state = state,
            // Unlike GitHub (live imminently, shows "coming soon" meanwhile), Apple sign-in is
            // deliberately parked until the App Store release needs it — hide the button entirely
            // rather than shipping one that stays dead for months.
            isAppleSignInAvailable = ApiConfig.APPLE_SIGN_IN_ENABLED,
            onAppleSignInClick = {
                viewModel.onIntent(LoginSheetIntent.AppleSignInStarted)
                if (ApiConfig.APPLE_SIGN_IN_ENABLED) openUrl(appleSignInStartUrl())
            },
            onGithubSignInClick = {
                viewModel.onIntent(LoginSheetIntent.GithubSignInStarted)
                if (ApiConfig.GITHUB_SIGN_IN_ENABLED) openUrl(githubSignInStartUrl())
            },
            onGoogleSignInClick = {
                viewModel.onIntent(LoginSheetIntent.GoogleSignInStarted)
                openUrl(googleSignInStartUrl())
            },
            onNotNowClick = {
                viewModel.onIntent(LoginSheetIntent.Dismissed)
                onDismiss()
            },
        )
    }
}

// Factored out from the ModalBottomSheet container so it can be rendered standalone (e.g. in the
// designsheet catalog) without needing sheet/scrim machinery.
@Composable
fun LoginSheetContent(
    state: LoginSheetState,
    onAppleSignInClick: () -> Unit,
    onGithubSignInClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNotNowClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Defaults to true so previews/catalog document the full sheet; the live call site passes
    // ApiConfig.APPLE_SIGN_IN_ENABLED so release builds hide the button until the flow exists.
    isAppleSignInAvailable: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            SkimbyteStrings.Login.TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            SkimbyteStrings.Login.SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        // GitHub is the visually-primary provider (a developer-news app's natural login);
        // the others are outlined. Each carries its brand mark — logo-less provider buttons
        // read as placeholders. GitHub/Apple marks tint to the button's content color; Google's
        // multi-color G must not be tinted (brand guidelines), hence Image over Icon.
        Button(
            onClick = onGithubSignInClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Icon(ProviderLogos.GitHub, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(SkimbyteStrings.Login.CONTINUE_WITH_GITHUB)
        }
        OutlinedButton(
            onClick = onGoogleSignInClick,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Image(ProviderLogos.Google, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(SkimbyteStrings.Login.CONTINUE_WITH_GOOGLE)
            }
        }
        if (isAppleSignInAvailable) {
            OutlinedButton(
                onClick = onAppleSignInClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(ProviderLogos.Apple, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(SkimbyteStrings.Login.CONTINUE_WITH_APPLE)
            }
        }
        state.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onNotNowClick, modifier = Modifier.fillMaxWidth()) {
            Text(SkimbyteStrings.Login.NOT_NOW)
        }
        Text(
            SkimbyteStrings.Login.PRIVACY_NOTE,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun LoginSheetContentPreview() {
    SkimbyteTheme {
        LoginSheetContent(
            state = LoginSheetState(),
            onAppleSignInClick = {},
            onGithubSignInClick = {},
            onGoogleSignInClick = {},
            onNotNowClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginSheetContentLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        LoginSheetContent(
            state = LoginSheetState(),
            onAppleSignInClick = {},
            onGithubSignInClick = {},
            onGoogleSignInClick = {},
            onNotNowClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginSheetContentErrorPreview() {
    SkimbyteTheme {
        LoginSheetContent(
            state = LoginSheetState(error = SkimbyteStrings.Login.GOOGLE_FAILED),
            onAppleSignInClick = {},
            onGithubSignInClick = {},
            onGoogleSignInClick = {},
            onNotNowClick = {},
        )
    }
}
