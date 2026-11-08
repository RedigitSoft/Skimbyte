package io.redigit.skimbyte.designsheet.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.redigit.skimbyte.designsheet.catalog.CatalogPage
import io.redigit.skimbyte.designsheet.catalog.ScreenFrame
import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.SkimbyteTheme
import io.redigit.skimbyte.theme.ThemeMode
import io.redigit.skimbyte.ui.screens.settings.AppearanceScreen
import io.redigit.skimbyte.ui.screens.settings.SettingsIntent
import io.redigit.skimbyte.ui.screens.settings.SettingsState

@Composable
fun AppearanceScreenCatalogContent() {
    // Local state so the swatches and theme toggle are live in the catalog (the real screen's
    // selection is persisted by SettingsViewModel; here it only needs to react).
    var state by remember { mutableStateOf(SettingsState(themeMode = ThemeMode.System, accentColor = AccentColor.Emerald)) }
    CatalogPage {
        ScreenFrame {
            AppearanceScreen(
                state = state,
                onIntent = { intent ->
                    state = when (intent) {
                        is SettingsIntent.ThemeModeSelected -> state.copy(themeMode = intent.mode)
                        is SettingsIntent.AccentColorSelected -> state.copy(accentColor = intent.accent)
                    }
                },
                onBack = {},
                modifier = Modifier.height(560.dp),
            )
        }
    }
}

@Preview
@Composable
private fun AppearanceScreenCatalogPreview() {
    SkimbyteTheme {
        AppearanceScreenCatalogContent()
    }
}

@Preview
@Composable
private fun AppearanceScreenCatalogLightPreview() {
    SkimbyteTheme(darkTheme = false) {
        AppearanceScreenCatalogContent()
    }
}
