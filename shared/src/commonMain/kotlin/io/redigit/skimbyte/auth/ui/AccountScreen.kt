package io.redigit.skimbyte.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.auth.AuthRepository
import io.redigit.skimbyte.theme.SkimbyteTheme
import org.koin.compose.koinInject

@Composable
fun AccountScreen(
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onLoginSucceeded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isLoggedIn) {
        LoginSheet(onDismiss = {}, onLoginSuccess = onLoginSucceeded)
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoggedIn) {
                Text("You're logged in", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onLogout) { Text("Log out") }
            } else {
                Text("Log in to manage your account", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun AccountRoute(authRepository: AuthRepository = koinInject(), modifier: Modifier = Modifier) {
    val session by authRepository.session.collectAsState()
    AccountScreen(
        isLoggedIn = session != null,
        onLogout = authRepository::logout,
        onLoginSucceeded = {},
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AccountScreenLoggedOutPreview() {
    SkimbyteTheme {
        AccountScreen(isLoggedIn = false, onLogout = {}, onLoginSucceeded = {})
    }
}

@Preview
@Composable
private fun AccountScreenLoggedInPreview() {
    SkimbyteTheme {
        AccountScreen(isLoggedIn = true, onLogout = {}, onLoginSucceeded = {})
    }
}

@Preview
@Composable
private fun AccountScreenLoggedInLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AccountScreen(isLoggedIn = true, onLogout = {}, onLoginSucceeded = {})
    }
}
