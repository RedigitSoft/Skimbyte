package io.redigit.skimbyte.onboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.redigit.skimbyte.theme.SkimbyteTheme

/**
 * Shown only for as long as the synchronous `TopicPreferencesStore.hasCompletedOnboarding()` read
 * takes (see Task 11) — no artificial delay, no ViewModel, no logic.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Skimbyte",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SkimbyteTheme {
        SplashScreen()
    }
}

@Preview
@Composable
private fun SplashScreenLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SplashScreen()
    }
}
