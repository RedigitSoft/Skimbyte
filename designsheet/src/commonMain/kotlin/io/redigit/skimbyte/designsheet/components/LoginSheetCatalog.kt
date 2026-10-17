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
import io.redigit.skimbyte.auth.ui.LoginSheetContent
import io.redigit.skimbyte.auth.ui.LoginSheetUiState
import io.redigit.skimbyte.theme.SkimbyteTheme

// Renders LoginSheetContent directly rather than LoginSheet (the ModalBottomSheet container),
// since the catalog doesn't need sheet/scrim machinery — just the content/container split
// established in auth/ui/LoginSheet.kt.
@Composable
fun LoginSheetCatalogContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        LoginSheetContent(state = LoginSheetUiState(), onEmailChanged = {}, onPasswordChanged = {}, onSubmit = {})
        LoginSheetContent(
            state = LoginSheetUiState(
                email = "demo@skimbyte.app",
                password = "wrong",
                error = "Invalid email or password",
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onSubmit = {},
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
