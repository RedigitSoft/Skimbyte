package io.redigit.skimbyte.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ktor.http.URLBuilder
import io.redigit.skimbyte.auth.GoogleAuthCallback
import io.redigit.skimbyte.data.remote.ApiConfig
import io.redigit.skimbyte.data.remote.BackendConfig
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.util.openUrl
import org.koin.compose.viewmodel.koinViewModel

// AuthConfig.GOOGLE_REDIRECT_URI is validated server-side against its allowlisted mobile redirect
// URIs (auth.oauth.allowedMobileRedirectUris) and forwarded back with the issued tokens — captured
// on Android via the matching intent-filter in AndroidManifest.xml (also sourced from
// local.properties, via androidApp's manifestPlaceholders).
private fun googleSignInStartUrl(): String =
    URLBuilder("${ApiConfig.AUTH_BASE_URL}/auth/oauth/google/start").apply {
        parameters.append("redirect_uri", BackendConfig.GOOGLE_REDIRECT_URI)
        parameters.append("product_id", ApiConfig.PRODUCT_ID)
    }.buildString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSheet(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginSheetViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    LaunchedEffect(Unit) {
        GoogleAuthCallback.redirects.collect { uri -> viewModel.onGoogleRedirect(uri, onLoginSuccess) }
    }
    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetForDismiss()
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        LoginSheetContent(
            state = uiState,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onToggleMode = viewModel::onToggleMode,
            onSubmit = { viewModel.onSubmit(onLoginSuccess) },
            onGoogleSignInClick = {
                viewModel.onGoogleSignInStarted()
                openUrl(googleSignInStartUrl())
            },
        )
    }
}

// Factored out from the ModalBottomSheet container so it can be rendered standalone (e.g. in the
// designsheet catalog) without needing sheet/scrim machinery.
@Composable
fun LoginSheetContent(
    state: LoginSheetUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            if (state.isRegisterMode) "Create an account to bookmark posts" else "Log in to bookmark posts",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedButton(onClick = onGoogleSignInClick, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Google")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "OR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting && state.email.isNotBlank() && state.password.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (state.isRegisterMode) "Sign up" else "Log in")
            }
        }
        TextButton(onClick = onToggleMode, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (state.isRegisterMode) "Already have an account? Log in" else "Don't have an account? Sign up",
            )
        }
    }
}

@Preview
@Composable
private fun LoginSheetContentEmptyPreview() {
    SkimbyteTheme {
        LoginSheetContent(
            state = LoginSheetUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onToggleMode = {},
            onSubmit = {},
            onGoogleSignInClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginSheetContentErrorPreview() {
    SkimbyteTheme {
        LoginSheetContent(
            state = LoginSheetUiState(email = "demo@skimbyte.app", password = "wrong", error = "Invalid email or password"),
            onEmailChanged = {},
            onPasswordChanged = {},
            onToggleMode = {},
            onSubmit = {},
            onGoogleSignInClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginSheetContentRegisterModePreview() {
    SkimbyteTheme {
        LoginSheetContent(
            state = LoginSheetUiState(isRegisterMode = true),
            onEmailChanged = {},
            onPasswordChanged = {},
            onToggleMode = {},
            onSubmit = {},
            onGoogleSignInClick = {},
        )
    }
}
