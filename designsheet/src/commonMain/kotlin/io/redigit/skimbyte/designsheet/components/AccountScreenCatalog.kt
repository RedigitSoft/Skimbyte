package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.auth.ui.AccountScreen
import io.redigit.skimbyte.theme.SkimbyteTheme

@Composable
fun AccountScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AccountScreen(
            isLoggedIn = true,
            onLogout = {},
            onLoginSucceeded = {},
            onSettingsClick = {},
            modifier = Modifier.height(200.dp),
        )
    }
}

@Preview
@Composable
private fun AccountScreenCatalogPreview() {
    SkimbyteTheme {
        AccountScreenCatalogContent()
    }
}
