package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.settings.SettingsUiState
import io.redigit.skimbyte.settings.ui.SettingsScreen
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode

@Composable
fun SettingsScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.System),
            onThemeModeSelected = {},
            onEditInterestsClick = {},
            onBack = {},
            modifier = Modifier.height(400.dp),
        )
    }
}

@Preview
@Composable
private fun SettingsScreenCatalogPreview() {
    SkimbyteTheme {
        SettingsScreenCatalogContent()
    }
}
