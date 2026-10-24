package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.screens.auth.LoginSheetContent
import io.redigit.skimbyte.ui.screens.auth.LoginSheetState
import io.redigit.skimbyte.theme.SkimbyteTheme

// Renders LoginSheetContent directly rather than LoginSheet (the ModalBottomSheet container),
// since the catalog doesn't need sheet/scrim machinery — just the content/container split
// established in ui/screens/auth/LoginSheet.kt.
@Composable
fun LoginSheetCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        LoginSheetContent(
            state = LoginSheetState(),
            onGithubSignInClick = {},
            onGoogleSignInClick = {},
            onNotNowClick = {},
        )
        LoginSheetContent(
            state = LoginSheetState(error = "Google sign-in failed"),
            onGithubSignInClick = {},
            onGoogleSignInClick = {},
            onNotNowClick = {},
        )
    }
}

@Preview
@Composable
private fun LoginSheetCatalogPreview() {
    SkimbyteTheme {
        LoginSheetCatalogContent()
    }
}
