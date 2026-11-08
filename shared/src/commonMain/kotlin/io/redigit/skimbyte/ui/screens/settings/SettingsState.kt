package io.redigit.skimbyte.ui.screens.settings

import io.redigit.skimbyte.theme.AccentColor
import io.redigit.skimbyte.theme.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AccentColor = AccentColor.Default,
)
