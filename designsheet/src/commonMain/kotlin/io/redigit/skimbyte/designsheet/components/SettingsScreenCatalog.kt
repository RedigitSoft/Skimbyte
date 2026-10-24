package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.ui.screens.settings.SettingsState
import io.redigit.skimbyte.ui.screens.settings.SettingsScreen
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode

@Composable
fun SettingsScreenCatalogContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.System),
            onIntent = {},
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
