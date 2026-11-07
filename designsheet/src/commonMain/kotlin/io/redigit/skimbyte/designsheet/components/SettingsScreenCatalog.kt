package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.ui.screens.settings.SettingsScreen
import io.redigit.skimbyte.ui.screens.settings.SettingsState

@Composable
fun SettingsScreenCatalogContent() {
    CatalogPage {
        ScreenFrame {
            SettingsScreen(
                state = SettingsState(themeMode = ThemeMode.System),
                onIntent = {},
                onEditInterestsClick = {},
                onPrivacyPolicyClick = {},
                onLicensesClick = {},
                onBack = {},
                modifier = Modifier.height(560.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenCatalogPreview() {
    SkimbyteTheme {
        SettingsScreenCatalogContent()
    }
}

@Preview
@Composable
private fun SettingsScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        SettingsScreenCatalogContent()
    }
}
